package com.mars_sim.core.map.location;

/**
 * Lightweight self-checks for {@link FrameTransform} that do not rely on JUnit.
 * <p>
 * This class lives under main sources so it compiles without test frameworks.
 * You can invoke {@link #runAll()} from a debugger or a small harness if desired.
 * </p>
 */
class FrameTransformTest {

    private static final double EPS = 1e-9;

    /**
     * Minimal stub implementing {@link LocalBoundedObject} for testing.
     */
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
        public LocalPosition getPosition() {
            return pos;
        }

        @Override
        public double getWidth() {
            return w;
        }

        @Override
        public double getLength() {
            return l;
        }

        @Override
        public double getFacing() {
            return f;
        }
    }

    /** Run all self-checks (no output unless an AssertionError is thrown). */
    static void runAll() {
        zeroDeg_exampleFromIssue();
        rotated45_135_etc_roundTrip();
    }

    /** Example derived from the original issue description. */
    static void zeroDeg_exampleFromIssue() {
        StubObj b = new StubObj(10.0, 0.0, 9.0, 9.0, 0.0);
        FrameTransform tf = FrameTransform.forBuilding(b);

        LocalPosition spotLocal = new LocalPosition(1.0, -1.0);
        LocalPosition inSettlement = tf.toSettlement(spotLocal);

        assertEquals(11.0, inSettlement.getX(), EPS, "X mismatch at 0°");
        assertEquals(-1.0, inSettlement.getY(), EPS, "Y mismatch at 0°");
        assertTrue(tf.contains(b, inSettlement), "Containment failed at 0°");

        LocalPosition back = tf.toBuilding(inSettlement);
        assertEquals(1.0, back.getX(), EPS, "Round-trip X mismatch at 0°");
        assertEquals(-1.0, back.getY(), EPS, "Round-trip Y mismatch at 0°");
    }

    /** Round-trip and containment checks for several facings. */
    static void rotated45_135_etc_roundTrip() {
        double[] facings = new double[] {45.0, 90.0, 135.0, 180.0, 270.0};
        for (double facing : facings) {
            StubObj b = new StubObj(-3.2, 7.5, 10.0, 14.0, facing);
            FrameTransform tf = FrameTransform.forBuilding(b);

            LocalPosition pLocal = new LocalPosition(2.5, -3.0);
            LocalPosition pSettle = tf.toSettlement(pLocal);
            LocalPosition back = tf.toBuilding(pSettle);

            assertEquals(pLocal.getX(), back.getX(), EPS, "Round-trip X mismatch at " + facing + "°");
            assertEquals(pLocal.getY(), back.getY(), EPS, "Round-trip Y mismatch at " + facing + "°");
            assertTrue(tf.contains(b, pSettle), "Containment failed at " + facing + "°");
        }
    }

    // ---------------------------------------------------------------------
    // Simple assertion helpers (no external dependencies)
    // ---------------------------------------------------------------------

    private static void assertEquals(double expected, double actual, double eps, String msg) {
        if (Math.abs(expected - actual) > eps) {
            throw new AssertionError(msg + " (expected=" + expected + ", actual=" + actual + ", eps=" + eps + ")");
        }
    }

    private static void assertTrue(boolean condition, String msg) {
        if (!condition) {
            throw new AssertionError(msg);
        }
    }
}
