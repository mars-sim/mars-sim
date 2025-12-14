package com.mars_sim.core.moon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        // Keep firing pulses
        for(int i = 0; i < (maxC * 200); i++) {
            var p = createPulse(1);
            lMgr.timePassing(p);
        }

        var colonies = lMgr.getColonySet();
        assertEquals(maxC, colonies.size());

        var names = colonies.stream().map(Named::getName).collect(Collectors.toSet());
        assertEquals(maxC, names.size(), "Unique colonies");
    }

    @Test
    void testNoColonies() {
        var lMgr =  getSim().getLunarColonyManager();

        lMgr.setMaxColonies(0);

        // Keep firing pulses
        for(int i = 0; i < 2000; i++) {
            var p = createPulse(1);
            lMgr.timePassing(p);
        }

        var colonies = lMgr.getColonySet();
        assertTrue(colonies.isEmpty());      
    }
}
