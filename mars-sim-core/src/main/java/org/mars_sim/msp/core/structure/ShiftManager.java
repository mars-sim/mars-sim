package org.mars_sim.msp.core.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.ShiftSlot.WorkStatus;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * This class manages a set of Shifts. This invovles the initial allocation and the changing
 * of On/Off Duty as the daya progresses.
 */
public class ShiftManager implements Serializable {
    	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(ShiftManager.class.getName());

    private String name;
    private List<Shift> shifts = new ArrayList<>();
    private List<ShiftSlot> onLeave = new ArrayList<>();
    private Settlement settlement;
    private int leavePercentage = 0;
    private int rotationSols = 0;
    private int offset;

    /**
     * Create a SHift Manager based on a shared ShiftPattern
     * @param shiftDefinition Definition of the shift pattern
     */
    public ShiftManager(Settlement settlement, ShiftPattern shiftDefinition) {
        this.name = shiftDefinition.getName();
        this.settlement = settlement;
        this.leavePercentage = shiftDefinition.getLeavePercentage();
        this.rotationSols = shiftDefinition.getRotationSols();
        
        // Get the rotation about the planet and convert that to a fraction of the Sol.
        double fraction = settlement.getCoordinates().getTheta()/(Math.PI * 2D); 
        if (fraction == 1D) {
            // Gone round the planet
            fraction = 0D;
        }
        this.offset = (int) (100 * fraction) * 10; // Do the offset in units of 10

        if (shiftDefinition.getShifts().isEmpty()) {
            throw new  IllegalArgumentException("No shift defined in " + shiftDefinition.getName());
        }
        for(ShiftSpec s : shiftDefinition.getShifts()) {
            shifts.add(new Shift(s, offset));
        }
    }

    public String getName() {
        return name;
    }

    /**
     * Allocation a Shift slot to a worker. This is based on looking at the percentage currently allocated.
     * @return
     */
    public ShiftSlot allocationShift() {
        Shift selectedShift = findSuitableShift(null);
        if (selectedShift == null) {
            throw new IllegalStateException("No shift selected for allocation");
        }

        return new ShiftSlot(selectedShift);
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
     * Find a suitable shift for a new allocation. Potentially exclude a Shift from the search
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
     * Time has changed during the day so update Shift duty flags.
     * @param pulse Current time frame
     */
    public void timePassing(ClockPulse pulse) {
        int currentMSol = pulse.getMarsTime().getMillisolInt();
        for(Shift s : shifts) {
            if (s.checkShift(currentMSol) && s.isOnDuty()) {
                logger.fine(settlement, "OnDuty Shift is " + s.getName());
            }
        }

        // If a new day then decide changes
        if (pulse.isNewSol()) {
            // Release persons on leave
            for(ShiftSlot ss : onLeave) {
                ss.setOnLeave(false);
            }
            onLeave.clear();

            // Rotate shifts
            if ((pulse.getMarsTime().getMissionSol() % rotationSols) == 0) {
                rotateShift();
            }
        }
    }

    /**
     * Look to see if any Shift reallocations ca be done. Any holidayers come off leave
     * and some are selected to change Shift
     */
	private void rotateShift() {

        // Get anyone who is not dead and is not returning from holiday
        // and not onCall
        List<Person> potentials = settlement.getAllAssociatedPeople().stream()
                    .filter(p -> !p.isDeclaredDead())
                    .filter(o -> (o.getShiftSlot().getStatus() != WorkStatus.ON_LEAVE))
                    .filter(o -> (o.getShiftSlot().getStatus() != WorkStatus.ON_CALL))
                    .collect(Collectors.toList());

        // Select someone to change Shift
        int maxOnLeave = Math.max(1, (int)(potentials.size() * leavePercentage)/100);
        while(!potentials.isEmpty() && (onLeave.size() < maxOnLeave)) {
            int idx = RandomUtil.getRandomInt(potentials.size()-1);
            Person p = potentials.remove(idx);
            ShiftSlot candidate = p.getShiftSlot();

            // Find a new Shift but exclude the current one
            Shift newShift = findSuitableShift(candidate.getShift());
            if (newShift != null) {
                onLeave.add(candidate);
                candidate.setOnLeave(true);
                Shift oldShift = candidate.getShift();
                candidate.setShift(newShift);
                logger.info(p, "Changes shift from " + oldShift.getName() + " to " + newShift.getName());
            }
        }
    }

    /**
     * Get the available Shifts
     * @return
     */
    public List<Shift> getShifts() {
		return shifts;
	}

    /**
     * Get those on Leave Shifts
     * @return
     */
    public List<ShiftSlot> getOnLeave() {
		return onLeave;
	}

    /**
     * Get the time mSol offset for this shift.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * How often does shifts get changes in terms of Sols.
     * @return
     */
    public int getRotationSols() {
        return rotationSols;
    }

    /**
     * Get the percetnage of people allowed on leave
     */
    public int getMaxOnLeave() {
        return leavePercentage;
    }
}
