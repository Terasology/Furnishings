// Copyright 2023 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.furnishings.logic.trunk;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.BroadcastEvent;
import org.terasology.gestalt.entitysystem.event.Event;

@BroadcastEvent
public class TrunkPlacedEvent implements Event {
    private EntityRef instigator;

    public TrunkPlacedEvent() {
        instigator = EntityRef.NULL;
    }

    public TrunkPlacedEvent(EntityRef trunkEntity) {
        this.instigator = trunkEntity;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}
