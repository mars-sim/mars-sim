package com.mars_sim.core.structure.building.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

class HouseKeepingTest {
    private final static String [] CLEAN_NAMES = {"CLEAN1", "CLEAN2", "CLEAN3"};
    private final static String [] INSPECT_NAMES = {"INSPECT1", "INSPECT2", "INSPECT3"};
    private static final String LOWEST = "Lowest";


    @Test
    void testGetLeastCleaned() {
        HouseKeeping keep = new HouseKeeping(CLEAN_NAMES, INSPECT_NAMES);

        // Add a new value at a very low value
        keep.cleaned(LOWEST, 1);
        String lowest = keep.getLeastCleaned();

        assertEquals("Lowest clean", LOWEST, lowest);
    }

    @Test
    void testGetLeastInspected() {
        HouseKeeping keep = new HouseKeeping(CLEAN_NAMES, INSPECT_NAMES);

        // Add a new value at a very low value
        keep.inspected(LOWEST, 1);
        String lowest = keep.getLeastInspected();

        assertEquals("Lowest inspected", LOWEST, lowest);
    }

    @Test
    void testCleaned() {
        HouseKeeping keep = new HouseKeeping(CLEAN_NAMES, INSPECT_NAMES);

        var origValue = keep.getAverageCleaningScore();

        keep.degradeCleaning(10);

        var degradeValue = keep.getAverageCleaningScore();
        assertTrue("Clean value lower after degrade", degradeValue < origValue);

        // Do inspect
        keep.cleaned(CLEAN_NAMES[0], 40);
        keep.cleaned(CLEAN_NAMES[1], 40);

        var newValue = keep.getAverageCleaningScore();
        assertTrue("Clean value higher than orig", origValue < newValue);

        // Check work
        assertEquals("Cumulative work", 80D, keep.getCumulativeWorkTime(), 0D);
    }

    @Test
    void testInspected() {
        HouseKeeping keep = new HouseKeeping(CLEAN_NAMES, INSPECT_NAMES);

        var origValue = keep.getAverageInspectionScore();

        keep.degradeInspected(10);

        var degradeValue = keep.getAverageInspectionScore();
        assertTrue("Inspection value lower after degrade", degradeValue < origValue);

        // Do inspect
        keep.inspected(INSPECT_NAMES[0], 40);
        keep.inspected(INSPECT_NAMES[1], 40);

        var newValue = keep.getAverageInspectionScore();
        assertTrue("Inspection value higher than orig", origValue < newValue);

        // Check work
        assertEquals("Cumulative work", 80D, keep.getCumulativeWorkTime(), 0D);
    }
}
