/*
 * Mars Simulation Project
 * ShiftManagerTest.java
 * @date 2022-11-20
 * @author Barry DavEvansis
 */
package org.mars_sim.msp.core.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Test the intenrals of the SHiftMansger
 */
public class ShiftManagerTest extends TestCase {

	private ShiftManager buildManager(int [] endTimes, int [] allocations) {
        List<ShiftSpec> specs = new ArrayList<>();
        int startTime = 0;
        for(int i = 0; i < endTimes.length; i++) {
            int endTime = endTimes[i];
            specs.add(new ShiftSpec(Integer.toString(i), startTime, endTime, allocations[i]));
            startTime = endTime;
        }

        ShiftPattern sp = new ShiftPattern("Test", specs);

        return new ShiftManager(sp);
    }

    /**
     * Test that Shift switching between On duty & Off duty as the day progresses
     */
    public void testShiftDuty() {
        int [] endTimes = {500, 1000};
        int [] allocations = {50, 50};
        ShiftManager sm = buildManager(endTimes, allocations);

        int currentShift = 0;
        for(int t = 0; t < 100; t++) {
            int time = t * 10;
            sm.timePassing(time);

            if (endTimes[currentShift] <= time) {
                currentShift++;
            }
            List<Shift> shifts = sm.getShifts();
            for(int idx = 0; idx < shifts.size(); idx++) {
                Shift s = shifts.get(idx);

                if (idx == currentShift) {
                    assertTrue("Current shift onduty", s.isOnDuty());
                }
                else {
                    assertFalse("Shift offduty", s.isOnDuty());
                }
            }

        }
    }

    /**
     * Test the allocation of workers to Shifts
     */
	public void testShiftAllocation() {
        int [] endTimes = {500, 700, 1000};
        int [] allocations = {40, 40, 20};
        ShiftManager sm = buildManager(endTimes, allocations);

        Map<Shift,Integer> actuals = new HashMap<>();
        // Allocation 100 sloys
        for(int i = 0; i < 100; i++) {
            ShiftSlot ss = sm.allocationShift();
            actuals.merge(ss.getShift(), 1, (a,b) -> a+b);
        }
     
        // Check the reported allocations
        for(Shift s : sm.getShifts()) {
            assertEquals("Shift reported allocation", s.getPopPercentage(), s.getSlotNumber());

            int actual = actuals.get(s);
            assertEquals("Shift actual allocation", actual, s.getSlotNumber());
        }

        // 
    }

    /**
     * Check that the OnDuty override flag works
     */
    public void testOnCal() {
        int [] endTimes = {500, 1000};
        int [] allocations = {40, 20};
        ShiftManager sm = buildManager(endTimes, allocations);

        // Allocate a slot and get the allocated Shift 
        ShiftSlot slot = sm.allocationShift();
        Shift allocatedShift = slot.getShift();

        // Shift on duty bu no on call
        sm.timePassing(allocatedShift.getEnd() - 10);
        assertEquals("Shift onduty but no on call", ShiftSlot.WorkStatus.ON_DUTY, slot.getStatus());

        // Shift of duty but no on call
        sm.timePassing(allocatedShift.getEnd());
        assertEquals("Shift off duty but no on call", ShiftSlot.WorkStatus.OFF_DUTY, slot.getStatus());

        slot.setOnCall(true);
        
        // Shift on duty but on call
        sm.timePassing(allocatedShift.getEnd() - 10);
        assertEquals("Shift onduty but on call", ShiftSlot.WorkStatus.ON_CALL, slot.getStatus());

        // Shift of duty bu on call
        sm.timePassing(allocatedShift.getEnd());
        assertEquals("Shift off duty but on call", ShiftSlot.WorkStatus.ON_CALL, slot.getStatus());
    }
}