package com.mars_sim.core.building.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HouseKeepingTest {
    private static final String [] CLEAN_NAMES = {"CLEAN1", "CLEAN2", "CLEAN3"};
    private static final String [] INSPECT_NAMES = {"INSPECT1", "INSPECT2", "INSPECT3"};
    private static final String LOWEST = "Lowest";


    @Test
    void testGetLeastCleaned() {
        HouseKeeping keep = new HouseKeeping(CLEAN_NAMES, INSPECT_NAMES);

        // Add a new value at a very low value
        keep.cleaned(LOWEST, 1);
        String lowest = keep.getLeastCleaned();

        assertEquals(LOWEST, lowest, "Lowest clean");
    }

    @Test
    void testGetLeastInspected() {
        HouseKeeping keep = new HouseKeeping(CLEAN_NAMES, INSPECT_NAMES);

        // Add a new value at a very low value
        keep.inspected(LOWEST, 1);
        String lowest = keep.getLeastInspected();

        assertEquals(LOWEST, lowest, "Lowest inspected");
    }

    @Test
    void testCleaned() {
        HouseKeeping keep = new HouseKeeping(CLEAN_NAMES, INSPECT_NAMES);

        var origValue = keep.getAverageCleaningScore();

        keep.degradeCleaning(10);

        var degradeValue = keep.getAverageCleaningScore();
        assertTrue(degradeValue < origValue, "Clean value lower after degrade");

        // Do inspect
        keep.cleaned(CLEAN_NAMES[0], 40);
        keep.cleaned(CLEAN_NAMES[1], 40);

        var newValue = keep.getAverageCleaningScore();
        assertTrue(origValue < newValue, "Clean value higher than orig");

        // Check work
        assertEquals(80D, keep.getCumulativeWorkTime(), 0D, "Cumulative work");
    }

    @Test
    void testInspected() {
        HouseKeeping keep = new HouseKeeping(CLEAN_NAMES, INSPECT_NAMES);

        var origValue = keep.getAverageInspectionScore();

        keep.degradeInspected(10);

        var degradeValue = keep.getAverageInspectionScore();
        assertTrue(degradeValue < origValue, "Inspection value lower after degrade");

        // Do inspect
        keep.inspected(INSPECT_NAMES[0], 40);
        keep.inspected(INSPECT_NAMES[1], 40);

        var newValue = keep.getAverageInspectionScore();
        assertTrue(origValue < newValue, "Inspection value higher than orig");

        // Check work
        assertEquals(80D, keep.getCumulativeWorkTime(), 0D, "Cumulative work");
    }
}
