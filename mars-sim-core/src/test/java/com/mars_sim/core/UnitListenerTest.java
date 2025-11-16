package com.mars_sim.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;

class UnitListenerTest extends MarsSimUnitTest {
    @Test
    void testNameChangeEvent() {
        var s = buildSettlement("Test");
        var listener = new EntityListener() {
            EntityEvent lastEvent = null;
            @Override
            public void entityUpdate(EntityEvent event) {
                lastEvent = event;
            }
        };

        s.addEntityListener(listener);

        s.setName("NewName");

        assertNotNull(listener.lastEvent, "Event received");
        assertEquals(Unit.NAME_EVENT, listener.lastEvent.getType(), "Event type is NAME_CHANGE");
        assertEquals(s, listener.lastEvent.getSource(), "Event entity is settlement");

        s.removeEntityListener(listener);
        listener.lastEvent = null;

        s.setName("New Name2");
        assertNull(listener.lastEvent, "No event received after listener removal");

    }

}
