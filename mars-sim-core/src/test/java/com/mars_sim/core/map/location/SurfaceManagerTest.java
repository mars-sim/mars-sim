package com.mars_sim.core.map.location;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.tool.RandomUtil;

class SurfaceManagerTest {

    private class TestFeature implements SurfacePOI {
        private Coordinates location;

        public TestFeature(Coordinates location) {
            this.location = location;
        }

        @Override
        public Coordinates getCoordinates() {
            return location;
        }
    }

    
    @Test
    void testGetFeaturesCenterHorizontal() {

        var mgr = new SurfaceManager<TestFeature>();
        int testBands = 10;

        double thetaPerBand = Math.PI/testBands;
        var center = new Coordinates(Math.PI/2, 0);
        mgr.addFeature(new TestFeature(center));

        // Band 0 is the center, create 2 points per bands one left and one right
        for(int i = 1; i < testBands/2; i++) {
            // Offset is middle of band
            double offset = thetaPerBand * ((double)i - 0.5);

            var locn = new Coordinates(center.getPhi(), center.getTheta() + offset);
            mgr.addFeature(new TestFeature(locn));

            locn = new Coordinates(center.getPhi(), center.getTheta() - offset);
            mgr.addFeature(new TestFeature(locn));
        }

        // One in the center; then 1 either side as the angle widens
        int expected = 1;
        for(int i = 0; i < testBands/2; i++) {
            var found = mgr.getFeatures(center, i * thetaPerBand * 1.01);

            assertEquals(expected, found.size(), "Slice #" + i);
            expected += 2;
        }
    }

    @Test
    void testGetFeaturesCenterVertical() {

        var mgr = new SurfaceManager<TestFeature>();
        int testBands = 10;

        // Vettical range is PI but each test point create one above and below equator
        double phiPerBand = (Math.PI/2)/testBands;
        var center = new Coordinates(Math.PI/2, 0D);
        mgr.addFeature(new TestFeature(center));

        for(int i = 1; i < testBands; i++) {
            // Offset is middle of band
            double offset = phiPerBand * ((double)i - 0.5);

            // CReate 2 test location, one above and one below center
            var locn = new Coordinates(center.getPhi() + offset, center.getTheta());
            mgr.addFeature(new TestFeature(locn));

            locn = new Coordinates(center.getPhi() - offset, center.getTheta());
            mgr.addFeature(new TestFeature(locn));
        }

        int expected = 1;
        for(int i = 0; i < testBands; i++) {
            var found = mgr.getFeatures(center, i * phiPerBand);
            // Note: the line below always fails
            assertEquals(expected, found.size(), "Slice #" + i);
            expected += 2;
        }
    }

    @Test
    void testGetFeaturesPolarVertical() {

        var mgr = new SurfaceManager<TestFeature>();
        int testBands = 10;

        // Vettical range is PI but each test point create one below the pole
        double phiPerBand = (Math.PI/2)/testBands;
        var center = new Coordinates(0D, 0D);
        mgr.addFeature(new TestFeature(center));

        for(int i = 1; i < testBands; i++) {
            // Offset is middle of band
            double offset = phiPerBand * ((double)i - 0.5);

            // Create a test location one below center but random around the globe
            var locn = new Coordinates(center.getPhi() + offset, RandomUtil.getRandomDouble(Math.PI));
            mgr.addFeature(new TestFeature(locn));
        }

        for(int i = 0; i < testBands; i++) {
            var found = mgr.getFeatures(center, i * phiPerBand);
            // Note: the line below always fails
            assertEquals(i+1, found.size(), "Slice #" + i);
        }
    }
}
