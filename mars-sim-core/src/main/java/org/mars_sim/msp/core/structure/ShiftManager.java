package org.mars_sim.msp.core.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class manages a set of Shifts. This invovles the initial allocation and the changing
 * of On/Off Duty as the daya progresses.
 */
public class ShiftManager implements Serializable {
    
    private String name;
    private List<Shift> shifts = new ArrayList<>();

    /**
     * Create a SHift Manager based on a shared ShiftPattern
     * @param shiftDefinition Definition of the shift pattern
     */
    public ShiftManager(ShiftPattern shiftDefinition) {
        this.name = shiftDefinition.getName();

        if (shiftDefinition.getShifts().isEmpty()) {
            throw new  IllegalArgumentException("No shift defined in " + shiftDefinition.getName());
        }
        for(ShiftSpec s : shiftDefinition.getShifts()) {
            shifts.add(new Shift(s));
        }

        // Initialise the initial Shift onDuty
        timePassing(0);
    }

    public String getName() {
        return name;
    }

    /**
     * Allocation a Shift slot to a worker. This is based on looking at the percentage currently allocated.
     * @return
     */
    public ShiftSlot allocationShift() {

        int totalAllocated = shifts.stream().map(Shift::getSlotNumber).reduce(0, Integer::sum);
        if (totalAllocated == 0) {
            // If no one  is allocated just fudge it to get the allocations started
            totalAllocated = 1;
        }


        Shift selectedShift = null;
        int biggestShoftfall = Integer.MIN_VALUE;
        for(Shift s : shifts) {
            int allocatedPerc = ((s.getSlotNumber() * 100) / totalAllocated);
            int shortfall = s.getPopPercentage() - allocatedPerc;
            if (shortfall > biggestShoftfall) {
                selectedShift = s;
                biggestShoftfall = shortfall;
            }
        }
        if (selectedShift == null) {
            throw new IllegalStateException("No shift selected for allocation");
        }
        selectedShift.increaseSlots();
        return new ShiftSlot(selectedShift);
    }

    /**
     * Time has changed during the day so update Shift duty flags.
     * @param currentMSol
     */
    public void timePassing(int currentMSol) {
        for(Shift s : shifts) {
            s.checkShift(currentMSol);
        }
    }

	public List<Shift> getShifts() {
		return shifts;
	}
}
