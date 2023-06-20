/*
 * Mars Simulation Project
 * Shift.java
 * @date 2022-11-20
 * @author Barry Evans
 */
package org.mars_sim.msp.core.structure;


import org.mars_sim.msp.core.events.ScheduledEventHandler;
import org.mars_sim.msp.core.time.MarsTime;

/**
 * This class represents an active work shift that has a number of slots for works allocated.
 */
public class Shift implements ScheduledEventHandler {

	private static final long serialVersionUID = 1L;
	
    private int start;
    private int end;
    private String name;
    private boolean onDuty = false;
    private int targetPercentage;
    private int members = 0;

    /**
     * Creates an active Shift defined by a shared specification.
     * 
     * @param spec The Specification of the Shift
     * @param offset MSols offset 
     */
    Shift(ShiftSpec spec, int offset) {
        // The ShiftSpec assumes standard time zone; but an offset has to be applied to
        // keep the shift aligned with Sunrise/Sunset
        this.start = (spec.getStart() + offset)%1000;
        this.end = (spec.getEnd() + offset)%1000;
        this.name = spec.getName();
        this.targetPercentage = spec.getPopPercentage();
    }

    public String getName() {
        return name;
    }

    public boolean isOnDuty() {
        return onDuty;
    }

    /**
     * Initialises the Shift to the current msol.
     * 
     * @param mSol The time in the day to check for.
     * @return Duration millisols to the next shift change
     */
    int initialize(int mSol) {
        if (start < end) {
            // Start and end on same Sol
            onDuty = (start <= mSol) && (mSol <  end);
        }
        else {
            // Ends on the following Sol so 2 separate segments
            onDuty = (start <= mSol) || (mSol <  end);
        }

        int duration = 0;
        if (onDuty) {
            duration = end - mSol;
        }
        else {
            duration = start - mSol;
        }
        if (duration < 0) {
            duration += 1000;
        }
        return duration;
    }

    /**
     * Gets the target percentage of the population that should use this Shift.
     * 
     * @return
     */
    public int getPopPercentage() {
        return targetPercentage;
    }

    public int getSlotNumber() {
        return members;
    }
    
    /**
     * Increases how many shots have been allocated to the shift.
     */
    void joinShift() {
        members++;
    }

    /**
     * Leaves the shift.
     */
    public void leaveShift() {
        members--;
    }

    /**
     * Gets when the shift starts.
     */
    public int getStart() {
        return start;
    }

    /**
     * Gets when the shift ends.
     */
    public int getEnd() {
        return end;
    }

    @Override
    public String getEventDescription() {
        return "Shift " + name + (onDuty ? " off duty" : " on duty");
    }

    /**
     * Time to switch over the shift.
     * 
     * @param now Time now when the handler was called; not used
     */
    @Override
    public int execute(MarsTime now) {
        // Flip the on duty flag
        onDuty = !onDuty;

        int duration = 0;
        if (onDuty) {
            duration = end - start;
        }
        else {
            duration = start - end;
        }
        if (duration < 0) {
            duration += 1000;
        }
        return duration;
    }
}
