// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.furnishings.logic.trunk;

import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 */
public class TrunkComponent implements Component<TrunkComponent> {
    public BlockFamily leftBlockFamily;
    public BlockFamily rightBlockFamily;
    public StaticSound openSound;
    public StaticSound closeSound;
    public Prefab trunkRegionPrefab;

    @Override
    public void copy(TrunkComponent other) {
        this.leftBlockFamily = other.leftBlockFamily;
        this.rightBlockFamily = other.rightBlockFamily;
        this.openSound = other.openSound;
        this.closeSound = other.closeSound;
        this.trunkRegionPrefab = other.trunkRegionPrefab;
    }
}
