package com.mars_sim.core.time;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;

public class MasterClockTest {

    private static class TestClockPulseListener implements ClockPulseListener {
        long lastPulse = -1;
        boolean doublePulse = false;
        boolean missedPulse = false;

        @Override
        public void clockPulse(ClockPulse currentPulse) {
            if (lastPulse > -1) {
                if (currentPulse.getId() == lastPulse) {
                    doublePulse = true;
                } else if (currentPulse.getId() > lastPulse + 1) {
                    missedPulse = true;
                }
            }
            lastPulse = currentPulse.getId();
        }
    };

    private static final long CLOCK_MIN = 1000; // Time to let clock run
    private SimulationConfig config;

    @BeforeEach
    void setUp() throws Exception {
        config = SimulationConfig.loadConfig();
    }

    @Test
    void testAddClockPulseListener() {
        var clock = new MasterClock(config, 1);

        var listener = new TestClockPulseListener();

        clock.addClockPulseListener(listener);
        clock.addClockPulseListener(listener); // Test adding the same listener again

        clock.start();

        // Wait for a few pulses
        waitForPulses(CLOCK_MIN);

        assertTrue(listener.lastPulse > 1, "Pulse received");
        assertFalse(listener.doublePulse, "Double pulse detected");
        assertFalse(listener.missedPulse, "Missed pulse detected");
    }

    @Test
    void testRemoveClockPulseListener() {
        var clock = new MasterClock(config, 1);

        var baseline = new TestClockPulseListener();
        var removed = new TestClockPulseListener();

        clock.addClockPulseListener(baseline);
        clock.addClockPulseListener(removed); // Test adding the same listener again
        clock.removeClockPulseListener(removed); // Test removing the listener

        clock.start();

        // Wait for a few pulses
        waitForPulses(CLOCK_MIN);

        assertTrue(baseline.lastPulse > 1, "Pulse received");
        assertTrue(removed.lastPulse == -1, "Removed listener should not receive pulses");
    }

    @Test
    void testPausedPulses() {
        var clock = new MasterClock(config, 1);

        var baseline = new TestClockPulseListener();
        clock.addClockPulseListener(baseline);

        clock.start();

        // Wait for a few pulses to pass

        clock.setPaused(true);
        assertTrue(clock.isPaused(), "Clock is paused");
        long pausedPulse = baseline.lastPulse;

        // Wait for a few pulses to pass
        waitForPulses(CLOCK_MIN);       

        assertTrue(baseline.lastPulse == pausedPulse, "No pulses received while paused");

        clock.setPaused(false);
        assertFalse(clock.isPaused(), "Clock is resumed");

        // Wait for a few pulses to pass
        waitForPulses(CLOCK_MIN);
        
        assertTrue(baseline.lastPulse > pausedPulse, "Pulses received after resuming");
    }

    private static void waitForPulses(long mSec) {
        try {
            Thread.sleep(mSec);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static class TestClockListener implements ClockListener {
        double lastDesiredTR = -1D;
        boolean lastPause = false;

        @Override
        public void desiredTimeRatioChange(double desiredTR) {
            lastDesiredTR = desiredTR;
        }

        @Override
        public void pauseChange(boolean isPaused) {
            lastPause = isPaused;
        }
    }

    @Test
    void testSetDesiredTimeRatio() {
        var clock = new MasterClock(config, 1);

        var baseline = new TestClockListener();
        clock.addClockListener(baseline);

        var tr = 2;
        clock.setDesiredTR(tr);
        assertTrue(clock.getDesiredTR() == tr, "Desired time ratio set");
        assertTrue(baseline.lastDesiredTR == tr, "Listener received desired time ratio change");
    }

    
    @Test
    void testSetPaused() {
        var clock = new MasterClock(config, 1);

        var baseline = new TestClockListener();
        clock.addClockListener(baseline);

        clock.setPaused(true);
        assertTrue(clock.isPaused(), "Clock is paused");
        assertTrue(baseline.lastPause, "Listener received pause change");

        clock.setPaused(false);
        assertFalse(clock.isPaused(), "Clock is resumed");
        assertFalse(baseline.lastPause, "Listener received resume change");
    }
}
