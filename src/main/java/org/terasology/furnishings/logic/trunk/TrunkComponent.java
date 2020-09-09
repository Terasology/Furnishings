// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.furnishings.logic.trunk;

import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.world.block.family.BlockFamily;

/**
 *
 */
public class TrunkComponent implements Component {
    public BlockFamily leftBlockFamily;
    public BlockFamily rightBlockFamily;
    public StaticSound openSound;
    public StaticSound closeSound;
    public Prefab trunkRegionPrefab;
}
