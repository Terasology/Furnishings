// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.furnishings.logic.door;

import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.math.Side;
import org.terasology.engine.world.block.family.BlockFamily;

/**
 *
 */
public class DoorComponent implements Component {
    public BlockFamily topBlockFamily;
    public BlockFamily bottomBlockFamily;
    public Side closedSide;
    public Side openSide;
    public StaticSound openSound;
    public StaticSound closeSound;
    public Prefab doorRegionPrefab;

    public boolean isOpen;
}
