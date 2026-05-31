package com.mars_sim.core.person.ai.mission;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class MissionTypeTest {
    @Test
    void testGetShortCode() {
        Set<String> shortCodes = new HashSet<>();
        for(var m : MissionType.values()) {
            shortCodes.add(m.getShortCode());
        }

        assertEquals(MissionType.values().length, shortCodes.size(), "Short codes should be unique for each MissionType");
    }
}
