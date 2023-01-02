/*
 * Mars Simulation Project
 * Shift.java
 * @date 2022-11-20
 * @author Barry Evans
 */
package org.mars_sim.msp.core.structure;


import org.mars_sim.msp.core.events.ScheduledEventHandler;

/**
 * This is an activeShift that has a numebr of Slots forworks allocated.
 */
public class Shift implements ScheduledEventHandler {

    private int start;
    private int end;
    private String name;
    private boolean onDuty= false;
    private int targetPercentage;
    private int members = 0;

    /**
     * Create an active Shift defined by a shared specification
     * @param spec The Specification of the Shift
     * @param offset MSols offset 
     */
    Shift(ShiftSpec spec, int offset) {
        // The Shift Spec assumes standard timezie; but an offset has to be applied to
        // keep the Shift aligned with Sunrise/Sunset
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
     * Initialise the Shift to the current msol.
     * @param mSol The time in the day to check for.
     * @return Duratino millisols to the next shift change
     */
    int initialize(int mSol) {
        if (start < end) {
            // Start and end on same Sol
            onDuty = (start <= mSol) && (mSol <  end);
        }
        else {
            // Ends on the following Sol so 2 seperate segments
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
     * What is the target percentage of the population that should use this Shift
     * @return
     */
    public int getPopPercentage() {
        return targetPercentage;
    }

    public int getSlotNumber() {
        return members;
    }
    
    /**
     * Increase how many shots have been allocationed to the Shift
     */
    void joinShift() {
        members++;
    }

    /**
     * Someone has left the Shift
     */
    public void leaveShift() {
        members--;
    }

    /**
     * When does the shift start
     */
    public int getStart() {
        return start;
    }

    /**
     * When does the shift end
     */
    public int getEnd() {
        return end;
    }

    @Override
    public String getEventDescription() {
        return "Shift " + name + (onDuty ? " off duty" : " on duty");
    }

    @Override
    public int execute() {
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
