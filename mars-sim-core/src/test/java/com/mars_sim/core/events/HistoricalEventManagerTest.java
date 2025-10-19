package com.mars_sim.core.events;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;

class HistoricalEventManagerTest extends MarsSimUnitTest {

    @Test
    void testListeners() {
        var listener =new HistoricalEventListener() {
            int called = 0;
            private HistoricalEvent last;
            @Override
            public void eventAdded(HistoricalEvent he) {
                called++;
                last = he;
            }

            @Override
            public void eventsRemoved(int startIndex, int endIndex) {
                throw new UnsupportedOperationException("Unimplemented method 'eventsRemoved'");
            }
        };

        var manager = new HistoricalEventManager(getContext().getSim().getMasterClock());
        manager.addListener(listener);

        var s = buildSettlement("Event");
        var event = new HistoricalEvent(HistoricalEventType.MEDICAL_CURED,
                    "Test Source","Test Cause",
                "While Testing","Test Who",
                            s, s);
        manager.registerNewEvent(event);

        assertEquals(1, listener.called);
        assertEquals(event, listener.last);

        manager.removeListener(listener);

        var event2 = new HistoricalEvent(
            HistoricalEventType.MEDICAL_CURED,
            "Test Source", "Test Cause",
            "While Testing","Test Who",
            s,s);
        manager.registerNewEvent(event2);
        
        assertEquals(1, listener.called);
        assertEquals(event, listener.last);
    }

    @Test
    void testRegisterNewEvent() {
        var s = buildSettlement("Event");
        var manager = new HistoricalEventManager(getContext().getSim().getMasterClock());

        var event1 = new HistoricalEvent(HistoricalEventType.MEDICAL_CURED,
                                "Test Source", "Test Cause",
                            "While Testing","Test Who",
                                        s,s);
        manager.registerNewEvent(event1);
        assertEquals(1, manager.getEvents().size());

        var event2 = new HistoricalEvent(HistoricalEventType.MEDICAL_DEATH,
                                "Test Source", "Test Cause",
                            "While Testing","Test Who",
                                        s,s);
        manager.registerNewEvent(event2);
        assertEquals(2, manager.getEvents().size());
    }

    @Test
    void testRepeatedEvent() {
        var manager = new HistoricalEventManager(getContext().getSim().getMasterClock());

        var s = buildSettlement("Event");

        // Add distinct events up to the match range
        for(int id=0; id<HistoricalEventManager.MATCH_RANGE; id++) {
            var event1 = new HistoricalEvent(HistoricalEventType.MEDICAL_CURED,
                                "Test Source", "Cause " + id,
                            "While Testing","Test Who",
                                        s,s);
            manager.registerNewEvent(event1);
            assertEquals(id + 1, manager.getEvents().size());
        }

        // Repeat the events and no increase
        for(int id=0; id<HistoricalEventManager.MATCH_RANGE; id++) {
            var event1 = new HistoricalEvent(HistoricalEventType.MEDICAL_CURED,
                                "Test Source", "Cause " + id,
                            "While Testing","Test Who",
                                        s,s);
            manager.registerNewEvent(event1);
            assertEquals(HistoricalEventManager.MATCH_RANGE, manager.getEvents().size());
        }
    }
}
