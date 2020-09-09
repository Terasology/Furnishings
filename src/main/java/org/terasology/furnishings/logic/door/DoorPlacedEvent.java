// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.furnishings.logic.door;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.BroadcastEvent;

@BroadcastEvent
public class DoorPlacedEvent implements Event {
    private final EntityRef instigator;

    public DoorPlacedEvent() {
        instigator = EntityRef.NULL;
    }

    public DoorPlacedEvent(EntityRef doorEntity) {
        this.instigator = doorEntity;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}
