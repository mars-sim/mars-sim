package com.mars_sim.core.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CompressedClockListenerTest {
    private static class TestListener implements ClockPulseListener {
        long pulseArrived = 0L;
        double totalElapsed = 0D;
        long pulseId = 0;

        @Override
        public void clockPulse(ClockPulse currentPulse) {
            pulseId = currentPulse.getId();
            totalElapsed = currentPulse.getElapsed();
            pulseArrived = System.currentTimeMillis();
        }
    }

    private static final long MIN_DURATION = 100L;
    private static final double PULSE_ELAPSED = 0.1D;

    @Test
    void testClockPulse() {
        // Capture the start before the listener records its own start time,
        // otherwise a millisecond tick in between makes the elapsed check flaky
        long startTime = System.currentTimeMillis();

        TestListener listener = new TestListener();
        var compressed = new CompressedClockListener(listener, MIN_DURATION);

        int id = 0;

        // Keep sending pulses until one get forwarded
        // or a fallback time has past to make the test does not get stuck
        while ((listener.pulseId == 0) && (System.currentTimeMillis() - startTime < (10*MIN_DURATION))) {
            id++;
            var pulse = new ClockPulse(id, PULSE_ELAPSED, null, null, false, false, false, false, false);
            compressed.clockPulse(pulse);
        }

        var elapsed = listener.pulseArrived - startTime;
        assertTrue(elapsed >= MIN_DURATION, "Elapsed time should be at least the minimum duration");
        assertEquals(id, listener.pulseId);
        assertEquals(id * PULSE_ELAPSED, listener.totalElapsed, 0.01D);
    }
}
