// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.furnishings.logic.door;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.ServerEvent;

@ServerEvent
public class OpenDoorEvent implements Event {
    private final EntityRef doorEntity;

    public OpenDoorEvent() {
        doorEntity = EntityRef.NULL;
    }

    public OpenDoorEvent(EntityRef doorEntity) {
        this.doorEntity = doorEntity;
    }

    public EntityRef getDoorEntity() {
        return doorEntity;
    }
}
