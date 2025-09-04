package com.mars_sim.core.map.location;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class FrameTransformTest {

    private static class StubObj implements LocalBoundedObject {
        private final LocalPosition pos;
        private final double w;
        private final double l;
        private final double f;

        StubObj(double cx, double cy, double width, double length, double facingDeg) {
            this.pos = new LocalPosition(cx, cy);
            this.w = width;
            this.l = length;
            this.f = facingDeg;
        }

        @Override
        public LocalPosition getPosition() { return pos; }

        @Override
        public double getWidth() { return w; }

        @Override
        public double getLength() { return l; }

        @Override
        public double getFacing() { return f; }
    }

    @Test
    void zeroDeg_exampleFromIssue() {
        StubObj b = new StubObj(10, 0, 9, 9, 0);
        var tf = FrameTransform.forBuilding(b);
        LocalPosition spotLocal = new LocalPosition(1, -1);
        LocalPosition inSettlement = tf.toSettlement(spotLocal);
        assertEquals(11.0, inSettlement.getX(), 1e-9);
        assertEquals(-1.0, inSettlement.getY(), 1e-9);
        assertTrue(tf.contains(b, inSettlement));
        assertEquals(1.0, tf.toBuilding(inSettlement).getX(), 1e-9);
        assertEquals(-1.0, tf.toBuilding(inSettlement).getY(), 1e-9);
    }

    @Test
    void rotated45_135_etc_roundTrip() {
        for (double facing : new double[]{45, 90, 135, 180, 270}) {
            StubObj b = new StubObj(-3.2, 7.5, 10, 14, facing);
            var tf = FrameTransform.forBuilding(b);
            LocalPosition pLocal = new LocalPosition(2.5, -3.0);
            LocalPosition pSettle = tf.toSettlement(pLocal);
            LocalPosition back = tf.toBuilding(pSettle);
            assertEquals(pLocal.getX(), back.getX(), 1e-9);
            assertEquals(pLocal.getY(), back.getY(), 1e-9);
            assertTrue(tf.contains(b, pSettle));
        }
    }
}
