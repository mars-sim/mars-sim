package com.mars_sim.core.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class PopulationCharacteristicsTest {
    @Test
    void testGetAverageHeight() {
        PopulationCharacteristics a = new PopulationCharacteristics(40, 40, 10, 4, 0, 0);
        assertEquals(7D, a.getAverageHeight(), "Average height");
    }

    @Test
    void testGetAverageWeight() {
        PopulationCharacteristics a = new PopulationCharacteristics(40, 40, 0, 0, 20, 10);
        assertEquals(15D, a.getAverageWeight(), "Average weight");  
    }

    @Test
    void testGetRandomHeight() {
        PopulationCharacteristics a = new PopulationCharacteristics(40, 40, 130, 130, 20, 10);
        double mh = a.getRandomHeight(GenderType.MALE);
        assertTrue(mh > 0, "Random male height created");

        double fh = a.getRandomHeight(GenderType.FEMALE);
        assertTrue(fh > 0, "Random female height created");
    }

    @Test
    void testGetRandomWeight() {
        PopulationCharacteristics a = new PopulationCharacteristics(40, 40, 130, 130, 20, 10);
        double mw = a.getRandomWeight(GenderType.MALE, 130);
        assertTrue(mw > 0, "Random male weight created");

        double fw = a.getRandomWeight(GenderType.FEMALE, 130);
        assertTrue(fw > 0, "Random female weight created");
    }
}
