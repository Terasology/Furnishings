// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.furnishings.logic.trunk;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.audio.AudioManager;
import org.terasology.engine.audio.events.PlaySoundEvent;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.math.Side;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.logic.MeshComponent;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.entity.placement.PlaceBlocks;
import org.terasology.engine.world.block.family.BlockPlacementData;
import org.terasology.engine.world.block.regions.BlockRegionComponent;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

import java.util.HashMap;
import java.util.Map;

@RegisterSystem(RegisterMode.AUTHORITY)
public class TrunkSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(TrunkSystem.class);

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

    @ReceiveEvent(components = {TrunkComponent.class, ItemComponent.class})
    public void placeTrunk(ActivateEvent event, EntityRef entity) {
        TrunkComponent trunk = entity.getComponent(TrunkComponent.class);

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

        Vector3ic blockPos = targetBlockComp.getPosition();
        Vector3f offset = event.getHitPosition().sub(blockPos.x(), blockPos.y(), blockPos.z(), new Vector3f());
        Side offsetDir = Side.inDirection(offset);
        Vector3i primePos = blockPos.add(offsetDir.direction(), new Vector3i());

        Block primeBlock = worldProvider.getBlock(primePos);
        if (!primeBlock.isReplacementAllowed()) {
            event.consume();
            return;
        }

        Block leftBlock;
        Block rightBlock;
        boolean isX = false;

        if (facingDir == Side.LEFT || facingDir == Side.RIGHT) {
            leftBlock = worldProvider.getBlock(primePos.x, primePos.y, primePos.z - 1);
            rightBlock = worldProvider.getBlock(primePos.x, primePos.y, primePos.z + 1);
        } else {
            isX = true;
            leftBlock = worldProvider.getBlock(primePos.x - 1, primePos.y, primePos.z);
            rightBlock = worldProvider.getBlock(primePos.x + 1, primePos.y, primePos.z);
        }

        // Determine top and bottom blocks
        Vector3i leftBlockPos = new Vector3i();
        Vector3i rightBlockPos = new Vector3i();
        if (leftBlock.isReplacementAllowed()) {
            leftBlockPos.set(isX ? primePos.x - 1 : primePos.x, primePos.y, isX ? primePos.z : primePos.z - 1);
            rightBlockPos.set(primePos);
        } else if (rightBlock.isReplacementAllowed()) {
            leftBlockPos.set(primePos);
            rightBlockPos.set(isX ? primePos.x + 1 : primePos.x, primePos.y, isX ? primePos.z : primePos.z + 1);
        } else {
            event.consume();
            return;
        }

        Block newBottomBlock;
        Block newTopBlock;

        if (facingDir == Side.FRONT || facingDir == Side.RIGHT) {
            newBottomBlock = trunk.rightBlockFamily.getBlockForPlacement(
                    new BlockPlacementData(leftBlockPos, Side.BOTTOM, facingDir.reverse().toDirection().asVector3f()));
            newTopBlock = trunk.leftBlockFamily.getBlockForPlacement(
                    new BlockPlacementData(rightBlockPos, Side.BOTTOM, facingDir.reverse().toDirection().asVector3f()));
        } else {
            newBottomBlock = trunk.leftBlockFamily.getBlockForPlacement(
                    new BlockPlacementData(leftBlockPos, Side.BOTTOM, facingDir.reverse().toDirection().asVector3f()));
            newTopBlock = trunk.rightBlockFamily.getBlockForPlacement(
                    new BlockPlacementData(rightBlockPos, Side.BOTTOM, facingDir.reverse().toDirection().asVector3f()));
        }
        Map<Vector3i, Block> blockMap = new HashMap<>();
        blockMap.put(leftBlockPos, newBottomBlock);
        blockMap.put(rightBlockPos, newTopBlock);
        PlaceBlocks blockEvent = new PlaceBlocks(blockMap, event.getInstigator());
        worldProvider.getWorldEntity().send(blockEvent);

        if (!blockEvent.isConsumed()) {
            EntityRef newTrunk = entityManager.create(trunk.trunkRegionPrefab);
            entity.removeComponent(MeshComponent.class);
            newTrunk.addComponent(new BlockRegionComponent(new BlockRegion(leftBlockPos).union(rightBlockPos)));

            newTrunk.addComponent(new LocationComponent(new Vector3f(rightBlockPos)));

            TrunkComponent newDoorComp = newTrunk.getComponent(TrunkComponent.class);
            newTrunk.saveComponent(newDoorComp);
            newTrunk.send(new PlaySoundEvent(Assets.getSound("engine:PlaceBlock").get(), 0.5f));
            newTrunk.send(new TrunkPlacedEvent(event.getInstigator()));
        }
    }

    @ReceiveEvent(components = {TrunkComponent.class, BlockRegionComponent.class, LocationComponent.class})
    public void onOpen(ActivateEvent event, EntityRef entity) {
        //logger.info("We got activated");
    }
}
