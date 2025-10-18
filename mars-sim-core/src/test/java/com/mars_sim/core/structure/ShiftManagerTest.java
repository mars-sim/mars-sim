/*
 * Mars Simulation Project
 * ShiftManagerTest.java
 * @date 2022-11-20
 * @author Barry Evans
 */
package com.mars_sim.core.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.events.ScheduledEventManager;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.shift.Shift;
import com.mars_sim.core.person.ai.shift.ShiftManager;
import com.mars_sim.core.person.ai.shift.ShiftPattern;
import com.mars_sim.core.person.ai.shift.ShiftSlot;
import com.mars_sim.core.person.ai.shift.ShiftSpec;
import com.mars_sim.core.person.ai.shift.ShiftSlot.WorkStatus;
import com.mars_sim.core.time.MarsTime;

/**
 * Test the internals of the ShiftMansger
 */
public class ShiftManagerTest extends AbstractMarsSimUnitTest {

	private ShiftManager buildManager(Settlement owner, int [] endTimes, int [] allocations) {
        List<ShiftSpec> specs = new ArrayList<>();

        // Initial start time is end time of last shift as it's a cycle
        int startTime = endTimes[endTimes.length-1];
        for(int i = 0; i < endTimes.length; i++) {
            int endTime = endTimes[i];
            specs.add(new ShiftSpec(Integer.toString(i), startTime, endTime, allocations[i]));
            startTime = endTime;
        }

        ShiftPattern sp = new ShiftPattern("Test", specs, 20, 0, -1);

        return new ShiftManager(owner, sp, 0);
    }

    /**
     * Test that Shift switching between On duty & Off duty as the day progresses
     */
    public void testShiftDuty() {
        Settlement settlement = buildSettlement();

        int [] endTimes = {400, 900};
        int [] allocations = {50, 50};
        ShiftManager sm = buildManager(settlement, endTimes, allocations);
        ScheduledEventManager futures = settlement.getFutureManager();

        int currentShift = 0;
        int quantum = 10;
        MarsTime now = sim.getMasterClock().getMarsTime();
        for(int t = 0; t < 100; t++) {
            now = now.addTime(quantum);
            futures.timePassing(createPulse(now, false, false));

            if (endTimes[currentShift] == now.getMillisolInt()) {
                currentShift++;
                currentShift %= endTimes.length;
            }
            List<Shift> shifts = sm.getShifts();
            for(int idx = 0; idx < shifts.size(); idx++) {
                Shift s = shifts.get(idx);

                if (idx == currentShift) {
                    assertTrue("Shift " + s.getName() + " On duty @ " + now.getMillisolInt(), s.isOnDuty());
                }
                else {
                    assertFalse("Shift " + s.getName() + " Off duty @ " + now.getMillisolInt(), s.isOnDuty());
                }
            }
        }
    }

    /**
     * Test the allocation of workers to Shifts
     */
	public void testShiftAllocation() {
        Settlement settlement = buildSettlement();

        int [] endTimes = {500, 700, 1000};
        int [] allocations = {40, 40, 20};
        ShiftManager sm = buildManager(settlement, endTimes, allocations);

        Map<Shift,Integer> actuals = new HashMap<>();
        // Allocation 100 sloys
        for(int i = 0; i < 100; i++) {
            ShiftSlot ss = sm.allocationShift(buildPerson("Worker #" + i, settlement));
            actuals.merge(ss.getShift(), 1, (a,b) -> a+b);
        }
     
        // Check the reported allocations
        for(Shift s : sm.getShifts()) {
            assertEquals("Shift reported allocation", s.getPopPercentage(), s.getSlotNumber());

            int actual = actuals.get(s);
            assertEquals("Shift actual allocation", actual, s.getSlotNumber());
        }
    }

    
    /**
     * Check that the OnDuty override flag works
     */
    public void testShiftRotation() {
        // This test invovles PErsons so have to take the Shiftmanager belonging to teh Settlmenet
        Settlement settlement = buildSettlement();
        ScheduledEventManager futures = settlement.getFutureManager();
        ShiftManager sm = settlement.getShiftManager();
        var leavePerctage = sm.getMaxOnLeave();
        assertTrue("Shiftmanger has leave allowance", leavePerctage > 0);

        // Add enough people to get someone on leave
        int personCount = (100/leavePerctage) + 2;  // Add 2 to make sure no rounding problem
        Map<ShiftSlot,Shift> origAllocation = new HashMap<>();
        for(int i = 0; i < personCount; i++) {
            Person p = buildPerson("Slot #" + i, settlement);
            ShiftSlot ss = p.getShiftSlot();
            origAllocation.put(ss, ss.getShift());
        }

        // Check shifts don;t change
        MarsTime now = sim.getMasterClock().getMarsTime().addTime((sm.getRotationSols() * 1000) - 1);
        futures.timePassing(createPulse(now, false, false));
        long leaveCount = origAllocation.keySet().stream()
                            .filter(s -> s.getStatus() == WorkStatus.ON_LEAVE)
                            .count();
        assertEquals("No shift changes on normal day", 0, leaveCount);

        // Rotate shifts. mission sol new sol flag set
        now = now.addTime(2);
        futures.timePassing(createPulse(now, false, false));

        List<ShiftSlot> onLeave = new ArrayList<>();

        // Check the reported allocations
        int changedShifts = 0;
        for(Entry<ShiftSlot, Shift> e : origAllocation.entrySet()) {
            ShiftSlot ss = e.getKey();
            boolean changed = !ss.getShift().equals(e.getValue());
            if (changed) {
                changedShifts++;
                assertEquals("Person changing shift status", WorkStatus.ON_LEAVE, ss.getStatus());
                onLeave.add(ss);
            }
            else {
                assertFalse("Person not changing shift not on leave", (WorkStatus.ON_LEAVE == ss.getStatus()));
            }
        }

        assertTrue("At least one is on leave", !onLeave.isEmpty());
        assertEquals("Report changed shift and actuals", onLeave.size(), changedShifts);
        assertEquals("On leave against target", (sm.getMaxOnLeave() * personCount)/100, onLeave.size());

        // Check day after rotation. mission sol new sol flag set
        now = now.addTime(ShiftManager.ROTATION_LEAVE);
        futures.timePassing(createPulse(now, false, false));
        leaveCount = origAllocation.keySet().stream()
                            .filter(s -> s.getStatus() == WorkStatus.ON_LEAVE)
                            .count();
        assertEquals("Worker on leave post rotation", 0, leaveCount);
    }

    /**
     * Checks that the OnDuty override flag works.
     */
    public void testOnCal() {
        Settlement settlement = buildSettlement();
        ScheduledEventManager futures = settlement.getFutureManager();

        Person worker = buildPerson("OnCall Worker", settlement);

        int [] endTimes = {500, 1000};
        int [] allocations = {40, 20};
        ShiftManager sm = buildManager(settlement, endTimes, allocations);

        // Allocate a slot and get the allocated Shift 
        ShiftSlot slot = sm.allocationShift(worker);
        int shiftEnd = slot.getShift().getEnd();

        // Shift on duty standard
        testWorkStatus(futures, slot, shiftEnd, "Standard worker", WorkStatus.ON_DUTY, WorkStatus.OFF_DUTY);

        // Check OnCall
        slot.setOnCall(true);
        testWorkStatus(futures, slot, shiftEnd, "On-Call worker", WorkStatus.ON_CALL, WorkStatus.ON_CALL);

        // Check OnLeave & OnCall. On Call takes precedence
        slot.setOnLeave(1000);
        testWorkStatus(futures, slot, shiftEnd, "On-Call & On-Leave worker", WorkStatus.ON_CALL, WorkStatus.ON_CALL);

        // Check OnLeave 
        slot.setOnCall(false);
        testWorkStatus(futures, slot, shiftEnd, "On-Leave worker", WorkStatus.ON_LEAVE, WorkStatus.ON_LEAVE);
    }

    /**
     * Tests the Work status before and after a Shift ends.
     * 
     * @param sm Shift manager controlling shifts
     * @param slot Slot being tested
     * @param shiftEnd Time the shift ends
     * @param scenario Scenario description
     * @param preEnd The Workstatus before shift ends
     * @param postEnd Work status after the SHift ends
     * 
     */
    private void testWorkStatus(ScheduledEventManager sm, ShiftSlot slot, int shiftEnd, String scenario,
                                WorkStatus preEnd, WorkStatus postEnd) {
        // Shift on duty but on call
        sm.timePassing(createPulse(1, shiftEnd - 10, false, false));
        assertEquals(scenario + " during shift", preEnd, slot.getStatus());

        // Shift of duty but on call
        sm.timePassing(createPulse(1, shiftEnd, false, false));
        assertEquals(scenario + " after shift", postEnd, slot.getStatus());
    }
}