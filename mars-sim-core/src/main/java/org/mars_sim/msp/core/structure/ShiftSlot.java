/*
 * Mars Simulation Project
 * ShiftSlot.java
 * @date 2022-11-19
 * @author Barry Evans
 */
package org.mars_sim.msp.core.structure;

import org.mars_sim.msp.core.events.ScheduledEventHandler;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * Represents a Slot on a specific Shift for work.
 */
public class ShiftSlot implements ScheduledEventHandler {

	private static final long serialVersionUID = 1L;
	
    /**
     * The work status of this Slot.
     */
    public enum WorkStatus {
        ON_DUTY, OFF_DUTY, ON_CALL, ON_LEAVE;
    }

    private boolean onCall = false;
    private boolean onLeave = false;
    private Shift shift;
    private Person worker;

    ShiftSlot(Shift shift, Person worker) {
        this.shift = shift;
        this.worker = worker;
        shift.joinShift();
    }

    /**
     * Update the OnCall override flag.
     * @return Previous OnCall.
     */
    public boolean setOnCall(boolean newOnCall) {
        boolean origOnCall = onCall;
        onCall = newOnCall;
        return origOnCall;
    }

    /**
     * Set this worker on a leave day.
     * @param duration Duration of the leave
     */
    public void setOnLeave(int duration) {
        onLeave = true;

        // Scheduled end of leave
        worker.getAssociatedSettlement().getFutureManager().addEvent(duration, this);
    }

    /**
     * Extract the status of this slot in terms of active work.
     */
    public WorkStatus getStatus() {
        if (onCall) {
            return WorkStatus.ON_CALL;
        }
        else if (onLeave) {
            return WorkStatus.ON_LEAVE;
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

    /**
     * Change the assigned shift
     */
    void setShift(Shift newShift) {
        shift.leaveShift();
        shift = newShift;
        shift.joinShift();
    }

    @Override
    public String getEventDescription() {
        return "Leave end for " + worker.getName();
    }

    /**
     * Time on leave comes to an end
     * @param now Current time not used
     */
    @Override
    public int execute(MarsClock now) {
        onLeave = false;
        return 0;
    }
}
