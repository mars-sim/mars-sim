package com.mars_sim.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

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
        assertEquals(EntityEventType.NAME_EVENT, listener.lastEvent.getType(), "Event type is NAME_CHANGE");
        assertEquals(s, listener.lastEvent.getSource(), "Event entity is settlement");

        s.removeEntityListener(listener);
        listener.lastEvent = null;

        s.setName("New Name2");
        assertNull(listener.lastEvent, "No event received after listener removal");

    }

    @Test
    void testGetEntityListeners() {
        var s = buildSettlement("Test");
        
        // Initially should have no listeners or be empty
        Set<EntityListener> listeners = s.getEntityListeners();
        assertNotNull(listeners, "Listeners set should not be null");
        assertTrue(listeners.isEmpty(), "Should start with no listeners");
        
        // Add a listener
        var listener1 = new EntityListener() {
            @Override
            public void entityUpdate(EntityEvent event) {
            }
        };
        s.addEntityListener(listener1);
        
        listeners = s.getEntityListeners();
        assertEquals(1, listeners.size(), "Should have 1 listener");
        assertTrue(listeners.contains(listener1), "Should contain the added listener");
        
        // Add another listener
        var listener2 = new EntityListener() {
            @Override
            public void entityUpdate(EntityEvent event) {
            }
        };
        s.addEntityListener(listener2);
        
        listeners = s.getEntityListeners();
        assertEquals(2, listeners.size(), "Should have 2 listeners");
        assertTrue(listeners.contains(listener1), "Should contain listener1");
        assertTrue(listeners.contains(listener2), "Should contain listener2");
        
        // Remove a listener
        s.removeEntityListener(listener1);
        
        listeners = s.getEntityListeners();
        assertEquals(1, listeners.size(), "Should have 1 listener after removal");
        assertTrue(listeners.contains(listener2), "Should only contain listener2");
    }

}
