/*
 * Mars Simulation Project
 * CompressedClockListener.java
 * @date 2025-11-29
 * @author Barry Evans
 */
package com.mars_sim.core.time;

/**
 * A ClockListener that compresses clock pulses to reduce the number of notifications
 * sent to the underlying listener. Pulses that occur within the minimum interval
 * are consolidated into a single pulse with the total elapsed time adjusted.
 * The root listener will not receive contigious pulses such that there will
 * be a jump in the pulse id.
 */
public class CompressedClockListener implements ClockListener {

    private ClockListener listener;
    private long minInterval;
    private long lastForward;
    private double msolsSkipped;

    /**
     * Create a compressing listener that compress and consolidated pulses.
     * @param listener Root listener to notify.
     * @param minInterval Minimum interval in milliseconds between pulses.
     */
    public CompressedClockListener(ClockListener listener, long minInterval) {
        this.listener = listener;
        this.minInterval = minInterval;
        this.lastForward = 0;
        this.msolsSkipped = 0D;
    }

    @Override
    public void clockPulse(ClockPulse currentPulse) {

		// Handler is compressing pulses so check the passed time

		// Compare elapsed real time to the minimum
        var timeNow = System.currentTimeMillis();
		long elapsedTime = timeNow - lastForward;
		if (elapsedTime < minInterval) {
			// Less than the minimum so record elapse and skip
			msolsSkipped += currentPulse.getElapsed();
			return;		
		}

        // Build new pulse to include skipped time
        var consolidated = currentPulse.addElapsed(msolsSkipped);

        // Reset count
        lastForward = timeNow;
        msolsSkipped = 0;

		// Call handler
		listener.clockPulse(consolidated);
    }

    @Override
    public void pauseChange(boolean isPaused, boolean showPane) {
        listener.pauseChange(isPaused, showPane);
    }

}
