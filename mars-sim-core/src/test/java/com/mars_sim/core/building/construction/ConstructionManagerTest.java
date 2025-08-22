// package org.mars_sim.msp.core.construction;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import org.junit.jupiter.api.Test;

public class ConstructionManagerTest {

    @Test
    void enqueue_start_complete_emits_events_and_releases_tile() {
        var clock = new SimClock(Duration.ofMillis(10), 1L);
        var bus = new SimClock.EventBus(clock);

        // Minimal validator for the test environment
        ConstructionManager.BlueprintValidator validator =
                id -> id.equals("Hab-Dome") || id.equals("Greenhouse");

        var cm = new ConstructionManager(clock, bus, validator);

        // Track events
        final int[] queued = {0}, started = {0}, completed = {0};
        bus.subscribe(ConstructionManager.ConstructionQueued.class,  e -> queued[0]++);
        bus.subscribe(ConstructionManager.ConstructionStarted.class, e -> started[0]++);
        bus.subscribe(ConstructionManager.ConstructionCompleted.class,e -> completed[0]++);

        var order = cm.enqueue("Hab-Dome", 10, 5, "test-user");
        assertTrue(cm.isReserved(10, 5));
        assertEquals(1, cm.listQueue().size());
        assertEquals(1, queued[0]);

        var pulled = cm.pullNextToStart();
        assertTrue(pulled.isPresent());
        assertEquals(order.id(), pulled.get().id());
        assertEquals(0, cm.listQueue().size());
        assertEquals(1, started[0]);

        assertTrue(cm.complete(order.id()));
        assertFalse(cm.isReserved(10, 5));
        assertEquals(1, completed[0]);
    }

    @Test
    void prevents_double_booking_same_tile() {
        var clock = new SimClock(Duration.ofMillis(10), 1L);
        var bus = new SimClock.EventBus(clock);
        ConstructionManager.BlueprintValidator validator = id -> true;
        var cm = new ConstructionManager(clock, bus, validator);

        cm.enqueue("Any", 2, 3, null);
        assertThrows(IllegalStateException.class, () -> cm.enqueue("Any", 2, 3, null));
    }
}
