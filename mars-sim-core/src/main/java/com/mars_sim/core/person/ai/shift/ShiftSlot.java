/*
 * Mars Simulation Project
 * ShiftSlot.java
 * @date 2025-10-12
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.shift;

import com.mars_sim.core.events.ScheduledEventHandler;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.time.MarsTime;

/**
 * This class represents a slot on a specific shift for work.
 */
public class ShiftSlot implements ScheduledEventHandler {

	private static final long serialVersionUID = 1L;
	
	private static final String SEPARATOR = " ";

    // Change of shift
    public static final String SHIFT_EVENT = "shift_change";
	
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
    private boolean isGuest = false;
    
    private Shift shift;
    private Person worker;

    /**
     * Constructor.
     * 
     * @param shift
     * @param worker
     * @param defaultOnCall true if this person starts if on-call shift and no need of joining any shift
     */
    ShiftSlot(Shift shift, Person worker) {
        this.shift = shift;
        this.worker = worker;

      	if (RoleType.GUEST == worker.getRole().getType()) {         
      		isGuest = true;
      		onCall = true;
      	}
      	
        else {
        	shift.joinShift(this);
        }
    }

    /**
     * Is this a guest slot ?
     * 
     * @return
     */
    public boolean isGuest() {
    	return isGuest;
    }
    
    /**
     * Sets this as a guest slot.
     * 
     * @param value
     */
    public void setGuest(boolean value) {
    	if (value) {         
      		isGuest = true;
      		onCall = true;
      	}
    	else
    		isGuest = false;
    }
    
    /**
     * Updates the OnCall override flag.
     * 
     * @param newOnCall
     * @return Previous OnCall.
     */
    public boolean setOnCall(boolean newOnCall) {
    	if (isGuest)
    		return onCall;
    	
        boolean origOnCall = onCall;
        onCall = newOnCall;
        worker.fireUnitUpdate(SHIFT_EVENT);

        return origOnCall;
    }

    /**
     * Sets this worker on a leave day.
     * 
     * @param duration Duration of the leave
     */
    public void setOnLeave(int duration) {
        onLeave = true;
        worker.fireUnitUpdate(SHIFT_EVENT);


        // Scheduled end of leave
        worker.getAssociatedSettlement().getFutureManager().addEvent(duration, this);
    }

    /**
     * Extracts the status of this slot in terms of active work.
     */
    public WorkStatus getStatus() {
        if (onCall || isGuest) {
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
     	if (isGuest)
    		return;
    	
        shift.leaveShift(this);
        shift = newShift;
        shift.joinShift(this);

        worker.fireUnitUpdate(SHIFT_EVENT);
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
        worker.fireUnitUpdate(SHIFT_EVENT);
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

    /**
     * The shift has changed. If direct on shift, i.e. not on call or leave then fire an event.
     */
    void shiftChange() {
        if (!onCall && !onLeave) {
            worker.fireUnitUpdate(SHIFT_EVENT);
        }
    }

    /**
     * Resigns from the current shift and do not join another.
     */
    public void resignShift() {
        shift.leaveShift(this);
    }
}
