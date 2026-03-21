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
        waitForPulses(clock);

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
        waitForPulses(clock);

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
        waitForPulses(clock);

        // Pause the clock but it might take one pulse
        clock.setPaused(true);
        assertTrue(clock.isPaused(), "Clock is paused");
        long pausedPulse = baseline.lastPulse;

        // Wait for a few pulses to pass
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }     

        assertTrue(baseline.lastPulse == pausedPulse, "No pulses received while paused");

        clock.setPaused(false);
        assertFalse(clock.isPaused(), "Clock is resumed");

        // Wait for a few pulses to pass
        waitForPulses(clock);
        
        assertTrue(baseline.lastPulse > pausedPulse, "Pulses received after resuming");
    }

    private static void waitForPulses(MasterClock clock) {
        long startPulse = clock.getNextPulse();
        int attempts = 5;

        // Keep sleeping until the next pulse is different from the starting pulse
        // or time runs out
        while ((startPulse == clock.getNextPulse()) && attempts >= 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            attempts--;
        }

        assertTrue(clock.getNextPulse() > startPulse, "Pulses received");
    }

    private static class TestClockListener implements ClockListener {
        int lastDesiredTR = -1;
        boolean lastPause = false;

        @Override
        public void desiredTimeRatioChange(int desiredTR) {
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
