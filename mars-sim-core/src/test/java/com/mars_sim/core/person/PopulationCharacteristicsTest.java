package com.mars_sim.core.person;

import junit.framework.TestCase;

public class PopulationCharacteristicsTest extends TestCase {
    public void testGetAverageHeight() {
        PopulationCharacteristics a = new PopulationCharacteristics(40, 40, 10, 4, 0, 0);
        assertEquals("Average height", 7D, a.getAverageHeight());
    }

    public void testGetAverageWeight() {
        PopulationCharacteristics a = new PopulationCharacteristics(40, 40, 0, 0, 20, 10);
        assertEquals("Average weight", 15D, a.getAverageWeight());  
    }

    public void testGetRandomHeight() {
        PopulationCharacteristics a = new PopulationCharacteristics(40, 40, 130, 130, 20, 10);
        double mh = a.getRandomHeight(GenderType.MALE);
        assertTrue("Random male height created", mh > 0);

        double fh = a.getRandomHeight(GenderType.FEMALE);
        assertTrue("Random female height created", fh > 0);
    }

    public void testGetRandomWeight() {
        PopulationCharacteristics a = new PopulationCharacteristics(40, 40, 130, 130, 20, 10);
        double mw = a.getRandomWeight(GenderType.MALE, 130);
        assertTrue("Random male weight created", mw > 0);

        double fw = a.getRandomWeight(GenderType.FEMALE, 130);
        assertTrue("Random female weight created", fw > 0);
    }
}
