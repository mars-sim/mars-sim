/*
 * Mars Simulation Project
 * ScienceConfigTest.java
 * @date 2023-11-25
 * @author Barry Evans
 */
package com.mars_sim.core.science;

import junit.framework.TestCase;

public class ScienceConfigTest extends TestCase {
    public void testGetATopic() {
        ScienceConfig scienceConfig = new ScienceConfig();
        for(ScienceType st : ScienceType.values()) {
            assertNotNull("Science " + st.name() + " has a topic", scienceConfig.getATopic(st));
        }
    }

    /**
     * Test that the average time for each phase is positive.
     */
    public void testGetAverageTime() {
        ScienceConfig scienceConfig = new ScienceConfig();

        for(SciencePhaseTime pt : SciencePhaseTime.values()) {
            int ave = scienceConfig.getAverageTime(pt);
            assertTrue("Science phase " + pt.name() + " has +ve average time", ave > 0);
        }
    }
}
