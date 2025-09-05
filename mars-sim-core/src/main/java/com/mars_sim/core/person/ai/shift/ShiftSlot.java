/*
 * Mars Simulation Project
 * ShiftSlot.java
 * @date 2023-09-01
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.shift;

import com.mars_sim.core.events.ScheduledEventHandler;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.time.MarsTime;

/**
 * This class represents a slot on a specific shift for work.
 */
public class ShiftSlot implements ScheduledEventHandler {

	private static final long serialVersionUID = 1L;
	
	private static final String SEPARATOR = " ";
	
    /**
     * The work status of this slot.
     */
    public enum WorkStatus {
        ON_DUTY ("On-Duty"), 
        OFF_DUTY ("Off-Duty"), 
        ON_CALL ("On-Call"), 
        ON_LEAVE ("On-Leave");

    	private String name;

		/** hidden constructor. */
		private WorkStatus(String name) {
			this.name = name;
		}
		
		public final String getName() {
			return this.name;
		}

		@Override
		public final String toString() {
			return getName();
		}
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
     * Updates the OnCall override flag.
     * 
     * @return Previous OnCall.
     */
    public boolean setOnCall(boolean newOnCall) {
        boolean origOnCall = onCall;
        onCall = newOnCall;
        return origOnCall;
    }

    /**
     * Sets this worker on a leave day.
     * 
     * @param duration Duration of the leave
     */
    public void setOnLeave(int duration) {
        onLeave = true;

        // Scheduled end of leave
        worker.getAssociatedSettlement().getFutureManager().addEvent(duration, this);
    }

    /**
     * Extracts the status of this slot in terms of active work.
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
     * Gets the parent shift of this slot.
     */
    public Shift getShift() {
        return shift;
    }

    /**
     * Changes the assigned shift.
     * 
     * @param newShift
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
     * Time on leave comes to an end.
     * 
     * @param now Current time not used
     */
    @Override
    public int execute(MarsTime now) {
        onLeave = false;
        return 0;
    }

    /**
     * Gets the extended description of this shift slot.
     * 
     * @return Return the shift name and it on/off status.
     */
    public String getStatusDescription() {
        return shift.getName() + SEPARATOR + getStatus().getName();
    }
}
