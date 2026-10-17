package com.mars_sim.core.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.time.MarsTime;

class MSolDataLoggerTest {

    @Test
    void testAddDataPointAcrossSols() {
        MSolDataLogger<String> logger = new MSolDataLogger<>(5);

        MarsTime sol1Time1 = new MarsTime(0);
        MarsTime sol1Time2 = sol1Time1.addTime(125D);
        MarsTime sol2Time1 = sol1Time1.addTime(1008D);
        MarsTime sol3Time1 = sol1Time1.addTime(2350D);

        DataLogger.changeTime(sol1Time1);
        logger.addDataPoint("sol1-first");

        DataLogger.changeTime(sol1Time2);
        logger.addDataPoint("sol1-second");

        DataLogger.changeTime(sol2Time1);
        logger.addDataPoint("sol2-first");

        DataLogger.changeTime(sol3Time1);
        logger.addDataPoint("sol3-first");

        Map<Integer, List<MSolDataItem<String>>> history = logger.getHistory();

        int sol1 = sol1Time1.getMissionSol();
        int sol2 = sol2Time1.getMissionSol();
        int sol3 = sol3Time1.getMissionSol();

        assertEquals(3, history.size(), "History should contain three sols");
        assertTrue(history.containsKey(sol1), "Missing first sol");
        assertTrue(history.containsKey(sol2), "Missing second sol");
        assertTrue(history.containsKey(sol3), "Missing third sol");

        List<MSolDataItem<String>> sol1Entries = history.get(sol1);
        assertEquals(2, sol1Entries.size(), "Unexpected number of entries for first sol");
        assertEquals("sol1-first", sol1Entries.get(0).getData(), "Unexpected first entry data");
        assertEquals(sol1Time1.getMillisolInt(), sol1Entries.get(0).getMsol(), "Unexpected first entry msol");
        assertEquals("sol1-second", sol1Entries.get(1).getData(), "Unexpected second entry data");
        assertEquals(sol1Time2.getMillisolInt(), sol1Entries.get(1).getMsol(), "Unexpected second entry msol");

        List<MSolDataItem<String>> sol2Entries = history.get(sol2);
        assertEquals(1, sol2Entries.size(), "Unexpected number of entries for second sol");
        assertEquals("sol2-first", sol2Entries.get(0).getData(), "Unexpected second sol entry data");
        assertEquals(sol2Time1.getMillisolInt(), sol2Entries.get(0).getMsol(), "Unexpected second sol entry msol");

        List<MSolDataItem<String>> sol3Entries = history.get(sol3);
        assertEquals(1, sol3Entries.size(), "Unexpected number of entries for third sol");
        assertEquals("sol3-first", sol3Entries.get(0).getData(), "Unexpected third sol entry data");
        assertEquals(sol3Time1.getMillisolInt(), sol3Entries.get(0).getMsol(), "Unexpected third sol entry msol");
    }

    @Test
    void testVisitVisitsEachElement() {
        MSolDataLogger<String> logger = new MSolDataLogger<>(5);
        MarsTime start = new MarsTime(0);

        Map<Integer, List<String>> expectedBySol = new HashMap<>();
        int expectedTotal = 0;

        expectedTotal += addAtTime(logger, start, "A", expectedBySol);
        expectedTotal += addAtTime(logger, start.addTime(420D), "B", expectedBySol);
        expectedTotal += addAtTime(logger, start.addTime(1002D), "C", expectedBySol);
        expectedTotal += addAtTime(logger, start.addTime(1700D), "D", expectedBySol);
        expectedTotal += addAtTime(logger, start.addTime(2505D), "E", expectedBySol);

        Map<Integer, List<String>> visitedBySol = new HashMap<>();
        AtomicInteger visitedCount = new AtomicInteger(0);

        logger.visit((sol, value) -> {
            visitedBySol.computeIfAbsent(sol, key -> new ArrayList<>()).add(value);
            visitedCount.incrementAndGet();
        });

        assertEquals(expectedTotal, visitedCount.get(), "visit should be called once per logged item");
        assertEquals(expectedBySol.keySet(), visitedBySol.keySet(), "Visited sols mismatch");

        for (int sol : expectedBySol.keySet()) {
            assertEquals(expectedBySol.get(sol), visitedBySol.get(sol), "Visited values mismatch for sol " + sol);
        }
    }

    private int addAtTime(MSolDataLogger<String> logger, MarsTime time, String value, Map<Integer, List<String>> expectedBySol) {
        DataLogger.changeTime(time);
        logger.addDataPoint(value);
        expectedBySol.computeIfAbsent(time.getMissionSol(), key -> new ArrayList<>()).add(value);
        return 1;
    }
}