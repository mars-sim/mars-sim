package com.mars_sim.core.time;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CompressedClockListenerTest {
    private static class TestListener implements ClockListener {
        long pulseArrived = 0L;
        double totalElapsed = 0D;
        long pulseId = 0;
        private boolean lastPause;

        @Override
        public void clockPulse(ClockPulse currentPulse) {
            pulseId = currentPulse.getId();
            totalElapsed = currentPulse.getElapsed();
            pulseArrived = System.currentTimeMillis();
        }

        @Override
        public void pauseChange(boolean isPaused, boolean showPane) {
            lastPause = isPaused;
        }
    }

    private static final long MIN_DURATION = 100L;
    private static final double PULSE_ELAPSED = 0.1D;

    @Test
    void testClockPulse() {
        TestListener listener = new TestListener();
        var compressed = new CompressedClockListener(listener, MIN_DURATION);

        int id = 0;
        var pulse = new ClockPulse(id, PULSE_ELAPSED, null, null, false, false, false, false);

        // First pulse should always be passed
        long startTime = System.currentTimeMillis();
        compressed.clockPulse(pulse);
        assertEquals(0, listener.pulseId);
        assertEquals(PULSE_ELAPSED, listener.totalElapsed, 0.0001D);
        
        // Keep sending pulses until one get forwarded
        // or a fallback time has past to make the test does not get stuck
        while ((listener.pulseId == 0) && (System.currentTimeMillis() - startTime < (1.5*MIN_DURATION))) {
            id++;
            pulse = new ClockPulse(id, PULSE_ELAPSED, null, null, false, false, false, false);
            compressed.clockPulse(pulse);
        }
        assertEquals(MIN_DURATION, listener.pulseArrived - startTime);
        assertEquals(id, listener.pulseId);
        assertEquals(id * PULSE_ELAPSED, listener.totalElapsed, 0.01D);
    }

    @Test
    void testPauseChange() {
        TestListener listener = new TestListener();
        var compressed = new CompressedClockListener(listener, MIN_DURATION);

        compressed.pauseChange(true, false);
        assertEquals(true, listener.lastPause);
        
        compressed.pauseChange(false, false);
        assertEquals(false, listener.lastPause);
    }
}
