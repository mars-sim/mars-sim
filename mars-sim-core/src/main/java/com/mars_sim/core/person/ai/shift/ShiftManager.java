/*
 * Mars Simulation Project
 * ShiftManager.java
 * @date 2023-09-01
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.shift;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mars_sim.core.events.ScheduledEventHandler;
import com.mars_sim.core.events.ScheduledEventManager;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.shift.ShiftSlot.WorkStatus;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.RandomUtil;

/**
 * This class manages a set of Shifts. This involves the initial allocation and the changing
 * of On/Off Duty as the day progresses.
 */
public class ShiftManager implements Serializable { 
    /**
     * Handles rotating the shifts.
     */
    private class RotationHandler implements ScheduledEventHandler {

		private static final long serialVersionUID = 1L;
		
        @Override
        public String getEventDescription() {
           return "Shift Rotation";
        }

        /**
         * Time to rotate the shift allocation.
         * 
         * @param now Current time not used
         */
        @Override
        public int execute(MarsTime now) {
            rotateShift();
            return rotationSols * 1000;
        }

    }

    /** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(ShiftManager.class.getName());
    
    /**
     * Default leave duration after a shift rotation.
     */
    public static final int ROTATION_LEAVE = 1000;   

    private String name;
    private List<Shift> shifts = new ArrayList<>();
    private Settlement settlement;
    private int leavePercentage = 0;
    private int rotationSols = 0;

    /**
     * Creates a Shift Manager based on a shared ShiftPattern.
     * 
     * @param settlement Owning Settlement
     * @param shiftDefinition Definition of the shift pattern
     * @param mSol Current millisol
     */
    public ShiftManager(Settlement settlement, ShiftPattern shiftDefinition, int mSol) {
        this.name = shiftDefinition.getName();
        this.settlement = settlement;
        this.leavePercentage = shiftDefinition.getLeavePercentage();
        this.rotationSols = shiftDefinition.getRotationSols();

        var offset = settlement.getTimeZone().getMSolOffset();

        // Create future event to rotate shifts
        ScheduledEventManager futures = settlement.getFutureManager();
        futures.addEvent((rotationSols * 1000) + offset, new RotationHandler());

        if (shiftDefinition.getShifts().isEmpty()) {
            throw new  IllegalArgumentException("No shift defined in " + shiftDefinition.getName());
        }
        
        for (ShiftSpec ss : shiftDefinition.getShifts()) {
            Shift s = new Shift(ss, offset);
            shifts.add(s);

            // Create future event for shift change
            int duration = s.initialize(mSol);
            futures.addEvent(duration, s);
        }
    }

    public String getName() {
        return name;
    }

    /**
     * Allocates a Shift slot to a worker. This is based on looking at the percentage currently allocated.
     * 
     * @param worker 
     * @return
     */
    public ShiftSlot allocationShift(Person person) {
       	// Note: at the start of the sim, role type of a person is null 
        Shift selectedShift = findSuitableShift(null);
        if (selectedShift == null) {
            throw new IllegalStateException("No shift selected for allocation");
        }
        return new ShiftSlot(selectedShift, person);
    }
    
    private int getTotalAllocated() {
        int totalAllocated = shifts.stream().map(Shift::getSlotNumber).reduce(0, Integer::sum);
        if (totalAllocated == 0) {
            // If no one is allocated, just fudge it to get the allocations started
            totalAllocated = 1;
        }
        return totalAllocated;
    }

    /**
     * Finds a suitable shift for a new allocation. Potentially exclude a Shift from the search.
     * 
     * @param person
     * @param exclude
     * @return
     */
    private Shift findSuitableShift(Shift exclude) {  	
        int totalAllocated = getTotalAllocated();

        Shift selectedShift = null;
        int biggestShoftfall = Integer.MIN_VALUE;
        for (Shift s : shifts) {
            int allocatedPerc = ((s.getSlotNumber() * 100) / totalAllocated);
            int shortfall = s.getPopPercentage() - allocatedPerc;
            if (!s.equals(exclude) && shortfall > biggestShoftfall) {
                selectedShift = s;
                biggestShoftfall = shortfall;
            }
        }

        return selectedShift;
    }

    /**
     * Looks to see if any Shift reallocations can be done. Any holidayers come off leave
     * and some are selected to change Shift.
     */
	private void rotateShift() {
        logger.info(settlement, "Rotating shifts.");

        // Get anyone who is not dead and is not returning from holiday
        // and not onCall
        List<Person> potentials = settlement.getAllAssociatedPeople().stream()
                    .filter(p -> !p.isDeclaredDead())
                    .filter(o -> (o.getShiftSlot().getStatus() != WorkStatus.ON_LEAVE))
                    .filter(o -> (o.getShiftSlot().getStatus() != WorkStatus.ON_CALL))
                    .collect(Collectors.toList());

        // Select someone to change Shift
        int maxOnLeave = Math.max(1, (potentials.size() * leavePercentage)/100);
        int changedCount = 0;
        while (!potentials.isEmpty() && (changedCount < maxOnLeave)) {
            int idx = RandomUtil.getRandomInt(potentials.size() - 1);
            Person p = potentials.remove(idx);
            
            if (assignNewShift(p, ROTATION_LEAVE) != null) {
            	changedCount++;
            }
        }
    }

	/**
	 * Assigns a person to a new work shift.
	 * 
	 * @param person
	 * @param leaveDuration
	 * @return the new shift
	 */
	public Shift assignNewShift(Person person, int leaveDuration) {
		ShiftSlot candidate = person.getShiftSlot();
	
		if (candidate.isGuest()) {
			// No need of getting a new shift for guests
			return candidate.getShift();
		}
			
        // Find a new Shift but exclude the current one
        Shift newShift = findSuitableShift(candidate.getShift());
        if (newShift != null) {
            candidate.setOnLeave(leaveDuration);
            Shift oldShift = candidate.getShift();
            candidate.setShift(newShift);
            logger.info(person, "Assigning the change of shift from " + oldShift.getName() 
        	+ " to " + newShift.getName() + ".");
        }

        return newShift;
	}
	
    /**
     * Gets the available Shifts.
     * 
     * @return
     */
    public List<Shift> getShifts() {
		return shifts;
	}

    /**
     * Gets how often does shifts get changes in terms of Sols.
     * 
     * @return
     */
    public int getRotationSols() {
        return rotationSols;
    }

    /**
     * Gets the percentage of people allowed on leave.
     */
    public int getMaxOnLeave() {
        return leavePercentage;
    }
    
    /**
     * Gets a modifier based on the Shift start time. This is based on how far through the shift a person is;
     * it is weighted towards the 1st 50% of the shift.
     * 
     * @param person
     * @param millisol
     * @return
     */
    public static double getShiftModifier(Person person, double shiftFraction, int millisol) {
        double completed = person.getShiftSlot().getShift().getShiftCompleted(millisol);

        // Do not start in the last 30% of a shift
        if (completed > shiftFraction) {
            return 0D;
        }
        return 1D - completed;
    }
}
