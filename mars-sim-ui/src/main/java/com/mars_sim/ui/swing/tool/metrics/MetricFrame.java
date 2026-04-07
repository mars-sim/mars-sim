package com.mars_sim.ui.swing.tool.metrics;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.mars_sim.core.Entity;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.metrics.Metric;
import com.mars_sim.core.metrics.MetricCategory;
import com.mars_sim.core.metrics.MetricManager;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MasterClock;

public class MetricFrame extends JFrame {
    private static final int INITIAL_POINTS = 10;
    private static final double D1_STEP = 75D;
    private static final MetricCategory FULL_CAT = new MetricCategory("Full");
    private static final MetricCategory HALF_CAT = new MetricCategory("Half");
    private static final MetricCategory DYN_CAT = new MetricCategory("Dynamic");

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

    private Simulation sim;
    private MetricChartViewer viewer;
    private MetricManager metricManager;
    
    private MetricFrame() {
        super("Mars Sim - Metric Chart Viewer");

                        
        SimulationConfig.loadConfig();
        sim = Simulation.instance();
        sim.testRun();

        metricManager = new MetricManager();
            
        // Create and add the chart viewer
        viewer = new MetricChartViewer(metricManager);
        add(viewer);
        
        // Configure frame
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private MetricManager loadTestData(MasterClock clock) {
        System.out.println("Loading test data into MetricManager");

        var rover1 = new TestEntity("Rover 1");
        var rover2 = new TestEntity("Rover 2");


        for(int step = 1; step <= INITIAL_POINTS; step++) {
            advanceTime(clock, 500D);

            metricManager.addValue(rover1, FULL_CAT, "Measure", -15D * step);
            metricManager.addValue(rover2, FULL_CAT, "Temperature", 5D * step);
            metricManager.addValue(rover1, FULL_CAT, "Temperature", 8D * step);

            if (step % 2 == 0) {
                metricManager.addValue(rover1, HALF_CAT, "Pressure", 0.7D * step);
                metricManager.addValue(rover2, HALF_CAT, "Energy", 120D * step);
            }

            metricManager.addValue(rover1, FULL_CAT, "Performance", 85D * step);
            metricManager.addValue(rover2, FULL_CAT, "Performance", 90D * step);
        }
        return metricManager;
    }

    private static void advanceTime(MasterClock clock, double seconds) {
        var now = clock.getMarsTime();
        now = now.addTime(seconds);
        clock.setMarsTime(now);
    }

    private class Updater implements Runnable {
        @Override
        public void run() {
            int count = 0;
            Metric d1 = null;
            try {
                while(true) {
                    Thread.sleep(2000);
                    if (sim == null) {
                        return;
                    }

                    // Only update values on every 2nd pass
                    count++;
                    if (count % 2 == 1) {
                        if (d1 == null) {
                            var rover3 = new TestEntity("Rover 3");
                            d1 = metricManager.getMetric(rover3, DYN_CAT, "Metric");
                        }
                        advanceTime(sim.getMasterClock(), 500D);
                        d1.recordValue((INITIAL_POINTS * D1_STEP) + Math.random() * D1_STEP);
                    }
                    
                    // Simulate the UI updating
                    viewer.clockUpdate((ClockPulse)null); // Use null because viewer does not read pulse currently
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void startUpdates() {
        // Add more sample data as needed, load after vieer is created
        // to check events
        loadTestData(sim.getMasterClock());

        Thread updaterThread = new Thread(new Updater());
        updaterThread.setDaemon(true);
        updaterThread.start();
    }

    /**
     * Main method for testing the component.
     */
    public static void main(String[] args) {
        var tester = new MetricFrame();

        tester.setLocationRelativeTo(null); // Center on screen
        tester.setVisible(true);

        tester.startUpdates();
    }
}
