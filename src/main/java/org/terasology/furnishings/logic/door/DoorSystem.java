/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.furnishings.logic.door;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.audio.AudioManager;
import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.JomlUtil;
import org.terasology.math.Side;
import org.terasology.registry.In;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.utilities.Assets;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.block.entity.placement.PlaceBlocks;
import org.terasology.world.block.family.BlockPlacementData;
import org.terasology.world.block.regions.BlockRegionComponent;

import java.util.HashMap;
import java.util.Map;

@RegisterSystem(RegisterMode.AUTHORITY)
public class DoorSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(DoorSystem.class);

    /**
     * Static "viewing direction" for placing door blocks.
     */
    private static final Vector3fc TOP = new Vector3f(Side.TOP.direction());

    @In
    private WorldProvider worldProvider;
    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private EntityManager entityManager;
    @In
    private AudioManager audioManager;
    @In
    private EntitySystemLibrary entitySystemLibrary;

    @ReceiveEvent(components = {DoorComponent.class, ItemComponent.class})
    public void placeDoor(ActivateEvent event, EntityRef entity) {
        DoorComponent door = entity.getComponent(DoorComponent.class);
        BlockComponent targetBlockComp = event.getTarget().getComponent(BlockComponent.class);
        if (targetBlockComp == null) {
            event.consume();
            return;
        }

        Vector3f horizDir =
                new Vector3f(event.getDirection())
                        .setComponent(1, 0); // set y dimension to 0
        Side facingDir = Side.inDirection(horizDir);
        if (!facingDir.isHorizontal()) {
            event.consume();
            return;
        }

        Vector3f offset =
                new Vector3f(event.getHitPosition())
                        .sub(targetBlockComp.position.x, targetBlockComp.position.y, targetBlockComp.position.z);
        Side offsetDir = Side.inDirection(offset);

        Vector3i primePos =
                JomlUtil.from(targetBlockComp.position)
                        .add(offsetDir.direction());

        Block primeBlock = worldProvider.getBlock(primePos);
        if (!primeBlock.isReplacementAllowed()) {
            event.consume();
            return;
        }

        Block belowBlock = worldProvider.getBlock(primePos.x, primePos.y - 1, primePos.z);
        Block aboveBlock = worldProvider.getBlock(primePos.x, primePos.y + 1, primePos.z);

        // Determine top and bottom blocks
        Vector3i bottomBlockPos = new Vector3i();
        Vector3i topBlockPos = new Vector3i();
        if (belowBlock.isReplacementAllowed()) {
            bottomBlockPos.set(primePos.x, primePos.y - 1, primePos.z);
            topBlockPos.set(primePos);
        } else if (aboveBlock.isReplacementAllowed()) {
            bottomBlockPos.set(primePos);
            topBlockPos.set(primePos.x, primePos.y + 1, primePos.z);
        } else {
            event.consume();
            return;
        }

        Side attachSide = determineAttachSide(facingDir, offsetDir, bottomBlockPos, topBlockPos);
        if (attachSide == null) {
            event.consume();
            return;
        }

        Side closedSide = facingDir.reverse();
        if (closedSide == attachSide || closedSide.reverse() == attachSide) {
            closedSide = attachSide.yawClockwise(1);
        }

        Block newBottomBlock = door.bottomBlockFamily.getBlockForPlacement(new BlockPlacementData(bottomBlockPos,
                closedSide, TOP));
        Block newTopBlock = door.topBlockFamily.getBlockForPlacement(new BlockPlacementData(bottomBlockPos,
                closedSide, TOP));

        Map<org.joml.Vector3i, Block> blockMap = new HashMap<>();
        blockMap.put(bottomBlockPos, newBottomBlock);
        blockMap.put(topBlockPos, newTopBlock);
        PlaceBlocks blockEvent = new PlaceBlocks(blockMap, event.getInstigator());
        worldProvider.getWorldEntity().send(blockEvent);

        if (!blockEvent.isConsumed()) {
            EntityRef newDoor = entityManager.create(door.doorRegionPrefab);
            entity.removeComponent(MeshComponent.class);

            newDoor.addComponent(new BlockRegionComponent(new BlockRegion(bottomBlockPos).union(topBlockPos)));

            Vector3fc doorCenter = new Vector3f(bottomBlockPos).add(0, 0.5f, 0);
            newDoor.addComponent(new LocationComponent(doorCenter));

            DoorComponent newDoorComp = newDoor.getComponent(DoorComponent.class);
            newDoorComp.closedSide = closedSide;
            newDoorComp.openSide = attachSide.reverse();
            newDoorComp.isOpen = false;
            newDoor.saveComponent(newDoorComp);
            newDoor.send(new PlaySoundEvent(Assets.getSound("engine:PlaceBlock").get(), 0.5f));
            logger.info("Closed Side: {}", newDoorComp.closedSide);
            logger.info("Open Side: {}", newDoorComp.openSide);
            newDoor.send(new DoorPlacedEvent(event.getInstigator()));
        }
    }

    private Side determineAttachSide(Side facingDir, Side offsetDir, Vector3i bottomBlockPos, Vector3i topBlockPos) {
        Side attachSide = null;
        if (offsetDir.isHorizontal()) {
            if (canAttachTo(topBlockPos, offsetDir.reverse()) && canAttachTo(bottomBlockPos, offsetDir.reverse())) {
                attachSide = offsetDir.reverse();
            }
        }
        if (attachSide == null) {
            Side clockwise = facingDir.yawClockwise(1);
            if (canAttachTo(topBlockPos, clockwise) && canAttachTo(bottomBlockPos, clockwise)) {
                attachSide = clockwise;
            }
        }
        if (attachSide == null) {
            Side anticlockwise = facingDir.yawClockwise(-1);
            if (canAttachTo(topBlockPos, anticlockwise) && canAttachTo(bottomBlockPos, anticlockwise)) {
                attachSide = anticlockwise;
            }
        }
        return attachSide;
    }

    private boolean canAttachTo(Vector3ic doorPos, Side side) {
        Vector3i adjacentBlockPos =
                new Vector3i(doorPos).add(side.direction());
        Block adjacentBlock = worldProvider.getBlock(adjacentBlockPos);
        return adjacentBlock.isAttachmentAllowed();
    }

    @ReceiveEvent(components = {DoorComponent.class, BlockRegionComponent.class, LocationComponent.class})
    public void onFrob(ActivateEvent event, EntityRef entity) {
        DoorComponent door = entity.getComponent(DoorComponent.class);
        if (door.isOpen) {
            event.getInstigator().send(new CloseDoorEvent(entity));
        } else {
            event.getInstigator().send(new OpenDoorEvent(entity));
        }
    }

    @ReceiveEvent
    public void closeDoor(CloseDoorEvent event, EntityRef player) {
        EntityRef entity = event.getDoorEntity();
        DoorComponent door = entity.getComponent(DoorComponent.class);
        BlockRegionComponent regionComp = entity.getComponent(BlockRegionComponent.class);

        setDoorBlocks(door, regionComp.region, door.closedSide);

        if (door.closeSound != null) {
            entity.send(new PlaySoundEvent(door.closeSound, 1f));
        }
        door.isOpen = false;
        entity.saveComponent(door);
    }

    @ReceiveEvent
    public void openDoor(OpenDoorEvent event, EntityRef player) {
        EntityRef entity = event.getDoorEntity();
        DoorComponent door = entity.getComponent(DoorComponent.class);
        BlockRegionComponent regionComp = entity.getComponent(BlockRegionComponent.class);

        setDoorBlocks(door, regionComp.region, door.openSide);

        if (door.openSound != null) {
            entity.send(new PlaySoundEvent(door.openSound, 1f));
        }
        door.isOpen = true;
        entity.saveComponent(door);
    }

    /**
     * Set both blocks that make up the door based on the door's state determined by {@code side}.
     * <p>
     * The blocks are placed as if the player was targeting the {@link Side#TOP} of the block beneath.
     *
     * @param door the door component with information about bottom and top blocks
     * @param region the block region the door covers (assumed to by of size 1x2x1)
     * @param side the state of the door, i.e., whether it is open or closed
     */
    private void setDoorBlocks(DoorComponent door, BlockRegion region, Side side) {
        Vector3i blockPos = region.getMin(new Vector3i());
        Block bottomBlock = door.bottomBlockFamily.getBlockForPlacement(new BlockPlacementData(blockPos, side, TOP));
        worldProvider.setBlock(blockPos, bottomBlock);

        region.getMax(blockPos);
        Block topBlock = door.topBlockFamily.getBlockForPlacement(new BlockPlacementData(blockPos, side, TOP));
        worldProvider.setBlock(blockPos, topBlock);
    }
}
