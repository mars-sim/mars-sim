package com.mars_sim.ui.swing.tool.metrics;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.mars_sim.core.Entity;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.metrics.MetricManager;

public class MetricFrame {
    private static class TestEntity implements Entity {
        private String name;

        public TestEntity(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getContext() {
            return "test";
        }
    }
    
    private static MetricManager loadTestData() {
                
        SimulationConfig.loadConfig();
        var sim = Simulation.instance();
        sim.testRun();

        var clock = sim.getMasterClock();

        // Create sample MetricManager for testing
        MetricManager metricManager = new MetricManager();
        Entity rover1 = new TestEntity("Rover 1");
        Entity rover2 = new TestEntity("Rover 2");

        for(int step = 1; step <= 10; step++) {
            var now = clock.getMarsTime();
            now = now.addTime(500D);
            clock.setMarsTime(now);

            metricManager.addValue(rover1, "Full", "Measure", -15 * step);
            metricManager.addValue(rover2, "Full", "Temperature", 5 * step);
            metricManager.addValue(rover1, "Full", "Temperature", 8 * step);

            if (step % 2 == 0) {
                metricManager.addValue(rover1, "Half", "Pressure", 0.7 * step);
                metricManager.addValue(rover2, "Half", "Energy", 120 * step);
            }

            metricManager.addValue(rover1, "Full", "Performance", 85 * step);
            metricManager.addValue(rover2, "Full", "Performance", 90 * step);

            metricManager.addValue(rover1, "Full", "Metric", 75 * step);
            metricManager.addValue(rover2, "Full", "Metric", 80 * step);
        }
        return metricManager;
    }

    /**
     * Main method for testing the component.
     */
    public static void main(String[] args) {
        var metricManager = loadTestData();
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Mars Sim - Metric Chart Viewer");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
            // Add more sample data as needed
            
            // Create and add the chart viewer
            MetricChartViewer viewer = new MetricChartViewer(metricManager);
            frame.add(viewer);
            
            // Configure frame
            frame.pack();
            frame.setLocationRelativeTo(null); // Center on screen
            frame.setVisible(true);
        });
    }
}
