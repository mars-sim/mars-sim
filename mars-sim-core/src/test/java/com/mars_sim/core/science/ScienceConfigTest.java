/*
 * Mars Simulation Project
 * ScienceConfigTest.java
 * @date 2023-11-25
 * @author Barry Evans
 */
package com.mars_sim.core.science;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ScienceConfigTest {

    @Test
    void testBotany() {
        ScienceConfig scienceConfig = new ScienceConfig();
        var topics = scienceConfig.getTopics(ScienceType.BOTANY);
        assertEquals(16, topics.size(), "Botany topics");
        assertTrue(topics.contains("Crop Yield"), "Botany topic should be 'Crop Yield'");
    }

    @Test
    void testGetATopic() {
        ScienceConfig scienceConfig = new ScienceConfig();
        for(ScienceType st : ScienceType.values()) {
            assertNotNull("Science " + st.name() + " has a topic", scienceConfig.getATopic(st));
        }
    }

    /**
     * Test that the average time for each phase is positive.
     */
    @Test
    void testGetAverageTime() {
        ScienceConfig scienceConfig = new ScienceConfig();

        for(SciencePhaseTime pt : SciencePhaseTime.values()) {
            int ave = scienceConfig.getAverageTime(pt);
            assertTrue(ave > 0,"Science phase " + pt.name() + " has +ve average time");
        }
    }
}
