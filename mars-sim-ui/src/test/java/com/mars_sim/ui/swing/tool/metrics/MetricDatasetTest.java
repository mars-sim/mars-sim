/*
 * Mars Simulation Project
 * MetricDatasetTest.java
 * @date 2025-10-25
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.Entity;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.metrics.Metric;
import com.mars_sim.core.metrics.MetricCategory;
import com.mars_sim.core.metrics.MetricManager;
import com.mars_sim.core.time.MarsTime;

/**
 * Unit tests for the MetricDataset class.
 */
class MetricDatasetTest {
    private static class MockEntity implements Entity {
        private final String id;
        
        public MockEntity(String id) {
            this.id = id;
        }
        
        @Override
        public String toString() {
            return "MockEntity{" + "id='" + id + '\'' + '}';
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            MockEntity that = (MockEntity) obj;
            return id.equals(that.id);
        }
        
        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String getName() {
            return id;
        }

        @Override
        public String getContext() {
            return "MockContext";
        }   
    }

    private static final int BASE1 = 10;
    private static final int DELTA1 = 5;
    private static final int BASE2 = 20;
    private static final int DELTA2 = 2;
    private static final int DATA_POINTS = 10;
    private static final double TIME_DELTA = 150D;
    private static final MetricCategory CATEGORY = new MetricCategory("TestCategory");

    private Entity testEntity1;
    private Entity testEntity2;
    private Metric metric1;
    private Metric metric2;
    private MarsTime startTime;
    private MetricManager manager;

    @BeforeEach
    void setup() {
        SimulationConfig.loadConfig();
        var sim = Simulation.instance();
        sim.testRun();
        var clock = sim.getMasterClock();
        manager = new MetricManager();
        
        // Create test entities
        testEntity1 = new MockEntity("E1");
        testEntity2 = new MockEntity("E2");
        
        // Get metrics without adding data points (to avoid simulation dependency)
        metric1 = manager.getMetric(testEntity1, CATEGORY, "M1");
        metric2 = manager.getMetric(testEntity2, CATEGORY, "M1");
        
        // Add sample data to metrics manually
        startTime = clock.getMarsTime();
        for (int i = 0; i < DATA_POINTS; i++) {
            var now = clock.getMarsTime().addTime(TIME_DELTA);
            clock.setMarsTime(now);

            metric1.recordValue(BASE1 + (DELTA1 * i));
            metric2.recordValue(BASE2 + (DELTA2 * i));
        }
    }

    @Test
    @DisplayName("Adding a single metric should update series count")
    void testAddSingleMetric() {

        MetricDataset dataset = new MetricDataset();
        dataset.addMetric(manager.getMetric(testEntity1, CATEGORY, "M1"));

        assertEquals(1, dataset.getSeriesCount(), "Dataset should have 1 series after adding 1 metric");
        assertEquals(DATA_POINTS, dataset.getItemCount(0), "First series should have 10 items");

        for(int j = 0; j < DATA_POINTS; j++) {
            Number x = dataset.getX(0, j);
            Number y = dataset.getY(0, j);
            MarsTime expectedTime = startTime.addTime((j + 1) * TIME_DELTA);
            double expectedValue = BASE1 + (DELTA1 * j);

            assertEquals(expectedTime.getTotalMillisols(), x.intValue(), "X value should match timestamp");
            assertEquals(expectedValue, y.doubleValue(), "Y value should match recorded value");
        }
    }

    
    @Test
    void testAddSingleCumulativeMetric() {

        MetricDataset dataset = new MetricDataset();
        dataset.addMetric(manager.getMetric(testEntity1, CATEGORY, "M1"));
        dataset.setCumulative(true);

        assertEquals(1, dataset.getSeriesCount(), "Dataset should have 1 series after adding 1 metric");
        assertEquals(DATA_POINTS, dataset.getItemCount(0), "First series should have 10 items");

        double total = 0;
        for(int j = 0; j < DATA_POINTS; j++) {
            Number x = dataset.getX(0, j);
            Number y = dataset.getY(0, j);
            MarsTime expectedTime = startTime.addTime((j + 1) * TIME_DELTA);
            total += BASE1 + (DELTA1 * j);

            assertEquals(expectedTime.getTotalMillisols(), x.intValue(), "X value should match timestamp");
            assertEquals(total, y.doubleValue(), "Y value should match recorded value");
        }
    }

    @Test
    void testAddTwoMetric() {

        MetricDataset dataset = new MetricDataset();
        dataset.addMetric(manager.getMetric(testEntity1, CATEGORY, "M1"));
        dataset.addMetric(manager.getMetric(testEntity2, CATEGORY, "M1"));

        assertEquals(2, dataset.getSeriesCount(), "Dataset should have 2 series after adding 2 metrics");
        assertEquals(DATA_POINTS, dataset.getItemCount(0), "First series should have 10 items");
        assertEquals(DATA_POINTS, dataset.getItemCount(1), "Second series should have 10 items");

        for(int j = 0; j < DATA_POINTS; j++) {
            Number x1 = dataset.getX(0, j);
            Number y1 = dataset.getY(0, j);
            MarsTime expectedTime1 = startTime.addTime((j + 1) * TIME_DELTA);
            double expectedValue1 = BASE1 + (DELTA1 * j);

            assertEquals(expectedTime1.getTotalMillisols(), x1.intValue(), "X value should match timestamp");
            assertEquals(expectedValue1, y1.doubleValue(), "Y value should match recorded value");

            Number x2 = dataset.getX(1, j);
            Number y2 = dataset.getY(1, j);
            MarsTime expectedTime2 = startTime.addTime((j + 1) * TIME_DELTA);
            double expectedValue2 = BASE2 + (DELTA2 * j);

            assertEquals(expectedTime2.getTotalMillisols(), x2.intValue(), "X value should match timestamp");
            assertEquals(expectedValue2, y2.doubleValue(), "Y value should match recorded value");
        }
    }
}