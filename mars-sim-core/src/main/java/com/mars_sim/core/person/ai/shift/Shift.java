/**
 * Mars Simulation Project
 * Shift.java
 * @date 2022-11-20
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.shift;

import java.util.HashSet;
import java.util.Set;

import com.mars_sim.core.events.ScheduledEventHandler;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MarsZone;
import com.mars_sim.core.time.TimeWindow;

/**
 * This class represents an active work shift that has a number of slots for works allocated.
 */
public class Shift implements ScheduledEventHandler {

	private static final long serialVersionUID = 1L;
	
    private final TimeWindow dutyTime;
    private final String name;
    private boolean onDuty;
    private final int targetPercentage;
    private final Set<ShiftSlot> members = new HashSet<>();

    /**
     * Creates an active Shift defined by a shared specification.
     * 
     * @param spec The Specification of the Shift
     * @param zone The MarsZone for the shift
     */
    Shift(ShiftSpec spec, MarsZone zone) {
        // The ShiftSpec assumes standard time zone; but an offset has to be applied to
        this.dutyTime = new TimeWindow(spec.getStart(), spec.getEnd(), zone);
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
        onDuty = dutyTime.isWithinRange(mSol);

        int duration = 0;
        if (onDuty) {
            duration = dutyTime.endMSol() - mSol;
        }
        else {
            duration = dutyTime.startMSol() - mSol;
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
        return members.size();
    }
    
    /**
     * Increases how many shots have been allocated to the shift.
     */
    void joinShift(ShiftSlot slot) {
        members.add(slot);
    }

    /**
     * Leaves the shift.
     */
    public void leaveShift(ShiftSlot slot) {
        members.remove(slot);
    }

    /**
     * Gets when the shift starts.
     */
    public int getStart() {
        return dutyTime.startMSol();
    }

    /**
     * Gets when the shift ends.
     */
    public int getEnd() {
        return dutyTime.endMSol();
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

        // Tell members
        members.forEach(ShiftSlot::shiftChange);

        int duration = 0;
        if (onDuty) {
            duration = dutyTime.endMSol() - dutyTime.startMSol();
        }
        else {
            duration = dutyTime.startMSol() - dutyTime.endMSol();
        }
        if (duration < 0) {
            duration += 1000;
        }
        return duration;
    }
    
    /**
     * At a given mSol how far through the Shift is it?
     * @param mSol
     * @return Percentage of Shift done; 0 means not on Duty.
     */
    public double getShiftCompleted(int mSol) {
        int duration = dutyTime.getDuration();

        int adjustedMSol = mSol - dutyTime.startMSol();
        if (adjustedMSol < 0) {
            adjustedMSol += 1000;
        }

        if (adjustedMSol > duration) {
            return 0D;
        }
        return (double)adjustedMSol/duration;
    }
}
