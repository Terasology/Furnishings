// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.furnishings.logic.door;

import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.math.Side;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.gestalt.entitysystem.component.Component;

public class DoorComponent implements Component<DoorComponent> {
    public BlockFamily topBlockFamily;
    public BlockFamily bottomBlockFamily;
    public Side closedSide;
    public Side openSide;
    public StaticSound openSound;
    public StaticSound closeSound;
    public Prefab doorRegionPrefab;

    public boolean isOpen;

    @Override
    public void copyFrom(DoorComponent other) {
        this.topBlockFamily = other.topBlockFamily;
        this.bottomBlockFamily = other.bottomBlockFamily;
        this.closedSide = other.closedSide;
        this.openSide = other.openSide;
        this.openSound = other.openSound;
        this.closeSound = other.closeSound;
        this.doorRegionPrefab = other.doorRegionPrefab;
        this.isOpen = other.isOpen;
    }
}
