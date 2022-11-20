/*
 * Mars Simulation Project
 * ShiftSlot.java
 * @date 2022-11-19
 * @author Barry Evans
 */
package org.mars_sim.msp.core.structure;

/**
 * Represents a Slot on a specific Shift for work.
 */
public class ShiftSlot {

    /**
     * The work status of this Slot.
     */
    public enum WorkStatus {
        ON_DUTY, OFF_DUTY, ON_CALL;
    }

    private boolean onCall;
    private Shift shift;

    ShiftSlot(Shift shift) {
        this.shift = shift;
        this.onCall = false;
    }

    /**
     * Update the OnCall override flag.
     */
    public void setOnCall(boolean newOnCall) {
        onCall = newOnCall;
    }

    /**
     * Extract teh status of this slot in terms of active work.
     */
    public WorkStatus getStatus() {
        if (onCall) {
            return WorkStatus.ON_CALL;
        }
        else if (shift.isOnDuty()) {
            return WorkStatus.ON_DUTY;
        }
        return WorkStatus.OFF_DUTY;
    }

    /**
     * Get the parent Shift of this slot.
     */
    public Shift getShift() {
        return shift;
    }
}
