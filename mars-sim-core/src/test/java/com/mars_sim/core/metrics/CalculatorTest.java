package com.mars_sim.core.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
@DisplayName("Metric Calculator Tests")
class CalculatorTest extends MarsSimUnitTest{
    
    private static final double[] VALUES = {10.0, 20.0, 30.0, 40.0, 50.0};
    private static final MetricCategory TEMP = new MetricCategory("Temperature");

    private Metric metric;
    private MasterClock masterClock;
    private MarsTime startTime;

    @Override
    @BeforeEach
    public void init() {
        super.init();

        masterClock = getContext().getSim().getMasterClock();
        startTime = masterClock.getMarsTime();

        Entity mockEntity = new MockEntity("E1");
        var metricKey = new MetricKey(mockEntity, TEMP, "Average");
        metric = new MemoryMetric(metricKey);

        int mSol = 10;
        for (double value : VALUES) {
            recordValue(mSol, value);
            mSol += 10;
        }
    }

    /**
     * Helper to add a value at a specific sol. This involves changing the time in the MasterClock.
     * @param mSol Millisols into the sol
     * @param value Value to record
     */
    private void recordValue(int mSol, double value) {
        MarsTime time = startTime.addTime(mSol);
        masterClock.setMarsTime(time);
        metric.recordValue(value);
    }

    @Test
    @DisplayName("Check average calculator")
    void testAverageCalculator() {
        var calculator = new Average();
        metric.apply(calculator);
        
        double total = 0.0;
        for (double v : VALUES) {
            total += v;
        }
        assertEquals(total / VALUES.length, calculator.getAverage(), 0.001);
    }

    @Test
    @DisplayName("Check MinMax calculator")
    void testMinMaxCalculator() {
        var calculator = new MinMax();
        metric.apply(calculator);
        
        double smallest = Double.MAX_VALUE;
        double largest = Double.MIN_VALUE;
        for (double v : VALUES) {
            smallest = Math.min(smallest, v);
            largest = Math.max(largest, v);
        }
        assertEquals(smallest, calculator.getMin());
        assertEquals(largest, calculator.getMax());
    }

    @Test
    @DisplayName("Check Total calculator")
    void testTotalCalculator() {
        var calculator = new Total();
        metric.apply(calculator);
        
        double total = 0.0;
        for (double v : VALUES) {
            total += v;
        }
        assertEquals(total, calculator.getSum(), 0.00);
        assertEquals(VALUES.length, calculator.getCount());
    }
}