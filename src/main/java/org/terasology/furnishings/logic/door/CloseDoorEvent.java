// Copyright 2023 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.furnishings.logic.door;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.ServerEvent;
import org.terasology.gestalt.entitysystem.event.Event;

@ServerEvent
public class CloseDoorEvent implements Event {
    private EntityRef doorEntity;

    public CloseDoorEvent() {
        doorEntity = EntityRef.NULL;
    }

    public CloseDoorEvent(EntityRef doorEntity) {
        this.doorEntity = doorEntity;
    }

    public EntityRef getDoorEntity() {
        return doorEntity;
    }
}
