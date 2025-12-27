package com.mars_sim.core.moon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.Named;
import com.mars_sim.core.test.MarsSimUnitTest;

class LunarColonyManagerTest extends MarsSimUnitTest{
    @Test
    void testMaxColonies() {
        var lMgr =  getSim().getLunarColonyManager();

        int maxC = 2;
        lMgr.setMaxColonies(maxC);

        // Fire once pulse to trigger scheduling of creation
        var p = createPulse(1);
        lMgr.timePassing(p);
        
        // Check events scheduled
        var futures = getSim().getScheduleManager();
        var events = new ArrayList<>(futures.getEvents()); // Take a copy as it changes
        assertEquals(maxC, events.size(), "Scheduled colony creation events");

        // Emulate the future events firing
        for(var e : events) {
            var expected = e.getWhen();
            p = createPulse(expected, false, false);
            futures.timePassing(p);
        }

        var colonies = lMgr.getColonySet();
        // num of colonies may vary from 0 to 2
        assertEquals(maxC, colonies.size(), "Number of colonies created");

        var names = colonies.stream().map(Named::getName).collect(Collectors.toSet());
        assertEquals(maxC, names.size(), "Unique colonies");

        // Fire another pulse, should not schedule any more colonies
        p = createPulse(1);
        lMgr.timePassing(p);
        assertTrue(futures.getEvents().isEmpty(), "No scheduled colony creation events");
    }

    @Test
    void testNoColonies() {
        var lMgr =  getSim().getLunarColonyManager();

        lMgr.setMaxColonies(0);

        // Fire once pulse to trigger scheduling of creation
        var p = createPulse(1);
        lMgr.timePassing(p);
        
        // Check events scheduled
        var futures = getSim().getScheduleManager();
        assertTrue(futures.getEvents().isEmpty(), "No scheduled colony creation events");

        var colonies = lMgr.getColonySet();
        assertTrue(colonies.isEmpty());   
    }
}
