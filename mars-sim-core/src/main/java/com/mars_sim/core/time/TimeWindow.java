/**
 * Mars Simulation Project
 * TimeWindow.java
 * @date 2026-11-14
 * @author Barry Evans
 */
package com.mars_sim.core.time;

import java.io.Serializable;

/**
 * Represents a time window on Mars, defined by start and end times in MSol.
 * It assumes the start & end mSol are based in MCT (Mars Coordinated Time).
 * The logic can accomodate the time window spanning a Sol boundary, i.e. end < start
 * 
 * @param startMSol Start of the time window in MSol
 * @param endMSol End of the time window in MSol
 */
public record TimeWindow(int startMSol, int endMSol) implements Serializable {

    /**
     * Constructs a TimeWindow adjusted for the local MarsZone.
     * @param startMSol Start of the time period in MSol in the local zone
     * @param endMSol End of the time period in MSol in the local zone
     * @param localZone Local zone of the above
     */
    public TimeWindow (int startMSol, int endMSol, MarsZone localZone) {
        this((startMSol + localZone.getMSolOffset()) % 1000,
             (endMSol + localZone.getMSolOffset()) % 1000);
    }

    /**
     * Check the time to see if it falls within the valid time window.
     * @param mSol the current time to check, in MSol
     * @return true if the time is within the time window, false otherwise
     */
    public boolean isWithinRange(int mSol) {
        if (startMSol <= endMSol) {
            return startMSol <= mSol && mSol < endMSol;
        } else {
            return startMSol <= mSol || mSol < endMSol;
        }
    }

    /**
     * Gets the time duration of the time window in MSol.
     * Caters for time windows that span across Sol boundaries.
     * @return Duration between start and end
     */
    public int getDuration() {
        if (startMSol < endMSol) {
            // Start and end on same Sol
            return endMSol - startMSol;
        }
        else {
            // Ends on the following Sol so 2 separate segments
            return 1000 - (startMSol - endMSol);
        }
    }
}