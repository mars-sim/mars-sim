package com.mars_sim.core.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.Entity;
import com.mars_sim.core.MockEntity;
import com.mars_sim.core.metrics.memory.MemoryMetric;
import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MasterClock;

/**
 * Unit tests for the Metric class.
 */
@DisplayName("Metric Tests")
class MetricTest extends MarsSimUnitTest{
    
    private MetricKey metricKey;
    private MarsTime startTime;
    private MasterClock masterClock;

    @Override
    @BeforeEach
    public void init() {
        super.init();
        Entity mockEntity = new MockEntity("E1");

        metricKey = new MetricKey(mockEntity, "Temperature", "Average");

        masterClock = getContext().getSim().getMasterClock();
        startTime = masterClock.getMarsTime();
    }

    private Metric createMetric(MetricKey key) {
        return new MemoryMetric(key);
    }

    @Test
    @DisplayName("Constructor should create Metric with given key")
    void testConstructor() {
        // When
        Metric newMetric = createMetric(metricKey);
        
        // Then
        assertEquals(metricKey, newMetric.getKey());
        assertTrue(newMetric.getSolRange().isEmpty());
    }
    
    /**
     * Helper to add a value at a specific sol. This involves changing the time in the MasterClock.
     * @param metric
     * @param sol
     * @param mSol Millisols into the sol
     * @param value Value to record
     */
    private void recordValue(Metric metric, int sol, int mSol, double value) {
        int delta = ((sol - startTime.getMissionSol()) * 1000) + mSol;
        MarsTime time = startTime.addTime(delta);
        masterClock.setMarsTime(time);
        metric.recordValue(value);
    }

    @Test
    @DisplayName("recordValue should add sol series to metric")
    void testRecordValue() {
        // When
        Metric newMetric = createMetric(metricKey);
        recordValue(newMetric, 1, 5, 10.0);
        recordValue(newMetric, 1, 15, 20.0);

        // Then
        var range = newMetric.getSolRange();
        assertEquals(1, range.size());
        assertTrue(range.contains(1));
        
        var totalCalculator = new Total();
        newMetric.apply(totalCalculator);
        assertEquals(30, totalCalculator.getSum(), 0.001);
        assertEquals(2, totalCalculator.getCount());
    }
    
    @Test
    @DisplayName("addSolSeries should add multiple sol series")
    void testAddMultipleSolSeries() {
                // When
        Metric newMetric = createMetric(metricKey);
        recordValue(newMetric, 1, 5, 10.0);
        recordValue(newMetric, 1, 15, 20.0);
        recordValue(newMetric, 2, 15, 20.0);

        // Then
        var range = newMetric.getSolRange();
        assertEquals(2, range.size());
        assertTrue(range.contains(1));
        assertTrue(range.contains(2));

        var totalCalculator = new Total();
        newMetric.apply(totalCalculator);
        assertEquals(50, totalCalculator.getSum(), 0.001);
        assertEquals(3, totalCalculator.getCount());
       
    }
    
    @Test
    @DisplayName("apply should process all data points with calculator")
    void testApplyCalculatorForAll() {
        Metric newMetric = createMetric(metricKey);
        recordValue(newMetric, 1, 5, 10.0);
        recordValue(newMetric, 1, 15, 20.0);
        recordValue(newMetric, 2, 15, 20.0);
        recordValue(newMetric, 3, 300, 50.0);

        // Then
        var range = newMetric.getSolRange();
        assertEquals(3, range.size());
        assertTrue(range.contains(1));
        assertTrue(range.contains(2));
        assertTrue(range.contains(3));

        var results = newMetric.applyBySol(0, sol -> new Total());
        assertEquals(3, results.size());

        var sol1 = results.get(1);
        assertEquals(30, sol1.getSum(), 0.001);
        assertEquals(2, sol1.getCount());

        var sol2 = results.get(2);
        assertEquals(20, sol2.getSum(), 0.001);
        assertEquals(1, sol2.getCount());

        var sol3 = results.get(3);
        assertEquals(50, sol3.getSum(), 0.001);
        assertEquals(1, sol3.getCount());
    }

    @Test
    @DisplayName("apply should process one sol data points with calculator")
    void testApplyCalculatorForOneSol() {
        Metric newMetric = createMetric(metricKey);
        recordValue(newMetric, 1, 5, 10.0);
        recordValue(newMetric, 1, 15, 20.0);
        recordValue(newMetric, 2, 15, 20.0);
        recordValue(newMetric, 3, 300, 50.0);

        var results = newMetric.applyBySol(1, sol -> new Total());
        assertEquals(1, results.size());

        var sol1 = results.get(1);
        assertEquals(30, sol1.getSum(), 0.001);
        assertEquals(2, sol1.getCount());
    }

    @Test
    @DisplayName("apply should process previous two sol data points with calculator")
    void testApplyCalculatorForPreviousSoll() {
        Metric newMetric = createMetric(metricKey);
        recordValue(newMetric, 1, 5, 10.0);
        recordValue(newMetric, 1, 15, 20.0);
        recordValue(newMetric, 2, 15, 20.0);
        recordValue(newMetric, 3, 300, 50.0);

        var results = newMetric.applyBySol(-2, sol -> new Total());
        assertEquals(2, results.size());

        var sol3 = results.get(3);
        assertEquals(50, sol3.getSum(), 0.001);
        assertEquals(1, sol3.getCount());
        
        var sol2 = results.get(2);
        assertEquals(20, sol2.getSum(), 0.001);
        assertEquals(1, sol2.getCount());
    }

    @Test
    @DisplayName("apply should work with empty metric")
    void testApplyWithEmptyMetric() {
        // Given
        Total calculator = new Total();
        
        // When
        var metric = createMetric(metricKey);
        Total result = metric.apply(calculator);
        
        // Then
        assertSame(calculator, result);
        assertEquals(0.0, calculator.getSum(), 0.001);
        assertEquals(0, calculator.getCount());
    }
}