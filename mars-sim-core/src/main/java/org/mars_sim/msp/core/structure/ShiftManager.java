package org.mars_sim.msp.core.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.events.ScheduledEventHandler;
import org.mars_sim.msp.core.events.ScheduledEventManager;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.ShiftSlot.WorkStatus;
import org.mars_sim.msp.core.time.MarsTime;
import org.mars_sim.msp.core.tool.RandomUtil;

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
    static final int ROTATION_LEAVE = 1000;   

    private String name;
    private List<Shift> shifts = new ArrayList<>();
    private Settlement settlement;
    private int leavePercentage = 0;
    private int rotationSols = 0;
    private int offset;

    /**
     * Creates a Shift Manager based on a shared ShiftPattern.
     * 
     * @param settlement Owning Settlement
     * @param shiftDefinition Definition of the shift pattern
     * @param sunRiseOffset Offset to Sunrise at this location
     * @param mSol Current millisol
     */
    public ShiftManager(Settlement settlement, ShiftPattern shiftDefinition, int sunriseOffset, int mSol) {
        this.name = shiftDefinition.getName();
        this.settlement = settlement;
        this.leavePercentage = shiftDefinition.getLeavePercentage();
        this.rotationSols = shiftDefinition.getRotationSols();
        this.offset = sunriseOffset;

        // Create future event to rotate shifts
        ScheduledEventManager futures = settlement.getFutureManager();
        futures.addEvent((rotationSols * 1000) + offset, new RotationHandler());

        if (shiftDefinition.getShifts().isEmpty()) {
            throw new  IllegalArgumentException("No shift defined in " + shiftDefinition.getName());
        }
        for(ShiftSpec ss : shiftDefinition.getShifts()) {
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
    public ShiftSlot allocationShift(Person worker) {
        Shift selectedShift = findSuitableShift(null);
        if (selectedShift == null) {
            throw new IllegalStateException("No shift selected for allocation");
        }

        return new ShiftSlot(selectedShift, worker);
    }
    
    private int getTotalAllocated() {
        int totalAllocated = shifts.stream().map(Shift::getSlotNumber).reduce(0, Integer::sum);
        if (totalAllocated == 0) {
            // If no one  is allocated just fudge it to get the allocations started
            totalAllocated = 1;
        }
        return totalAllocated;
    }

    /**
     * Finds a suitable shift for a new allocation. Potentially exclude a Shift from the search.
     * 
     * @param exclude
     * @return
     */
    private Shift findSuitableShift(Shift exclude) {
        int totalAllocated = getTotalAllocated();

        Shift selectedShift = null;
        int biggestShoftfall = Integer.MIN_VALUE;
        for(Shift s : shifts) {
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
        logger.info(settlement, "Rotating shifts");

        // Get anyone who is not dead and is not returning from holiday
        // and not onCall
        List<Person> potentials = settlement.getAllAssociatedPeople().stream()
                    .filter(p -> !p.isDeclaredDead())
                    .filter(o -> (o.getShiftSlot().getStatus() != WorkStatus.ON_LEAVE))
                    .filter(o -> (o.getShiftSlot().getStatus() != WorkStatus.ON_CALL))
                    .collect(Collectors.toList());

        // Select someone to change Shift
        int maxOnLeave = Math.max(1, (int)(potentials.size() * leavePercentage)/100);
        int changedCount = 0;
        while(!potentials.isEmpty() && (changedCount < maxOnLeave)) {
            int idx = RandomUtil.getRandomInt(potentials.size()-1);
            Person p = potentials.remove(idx);
            ShiftSlot candidate = p.getShiftSlot();

            // Find a new Shift but exclude the current one
            Shift newShift = findSuitableShift(candidate.getShift());
            if (newShift != null) {
                candidate.setOnLeave(ROTATION_LEAVE);
                changedCount++;
                Shift oldShift = candidate.getShift();
                candidate.setShift(newShift);
                logger.info(p, "Changes shift from " + oldShift.getName() + " to " + newShift.getName());
            }
        }
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
     * Gets the time mSol offset for this shift.
     */
    public int getOffset() {
        return offset;
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
}
