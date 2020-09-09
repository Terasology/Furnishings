// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.furnishings.logic.trunk;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.BroadcastEvent;

@BroadcastEvent
public class TrunkPlacedEvent implements Event {
    private final EntityRef instigator;

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
