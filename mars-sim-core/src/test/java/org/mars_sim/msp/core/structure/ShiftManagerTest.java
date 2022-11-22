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
import java.util.Map.Entry;

import org.mars_sim.msp.core.AbstractMarsSimUnitTest;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.ShiftSlot.WorkStatus;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * Test the intenrals of the SHiftMansger
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

        ShiftPattern sp = new ShiftPattern("Test", specs, 20, 0);

        return new ShiftManager(owner, sp);
    }

    /**
     * Test that Shift switching between On duty & Off duty as the day progresses
     */
    public void testShiftDuty() {
        Settlement settlement = buildSettlement();

        int [] endTimes = {400, 900};
        int [] allocations = {50, 50};
        ShiftManager sm = buildManager(settlement, endTimes, allocations);

        int currentShift = 0;
        for(int t = 0; t < 100; t++) {
            int time = t * 10;
            sm.timePassing(createPulse(1, time, false));

            if (endTimes[currentShift] == time) {
                currentShift++;
                currentShift %= endTimes.length;
            }
            List<Shift> shifts = sm.getShifts();
            for(int idx = 0; idx < shifts.size(); idx++) {
                Shift s = shifts.get(idx);

                if (idx == currentShift) {
                    assertTrue("Shift " + s.getName() + " On duty @ " + time, s.isOnDuty());
                }
                else {
                    assertFalse("Shift " + s.getName() + " Off duty @ " + time, s.isOnDuty());
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
            ShiftSlot ss = sm.allocationShift();
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
        ShiftManager sm = settlement.getShiftManager();

        int personCount = 20;
        Map<ShiftSlot,Shift> origAllocation = new HashMap<>();
        for(int i = 0; i < personCount; i++) {
            Person p = buildPerson("Slot #" + i, settlement);
            ShiftSlot ss = p.getShiftSlot();
            origAllocation.put(ss, ss.getShift());
        }

        assertTrue("Shiftmanger has leave allowance", sm.getMaxOnLeave() > 0);

        // Check shifts don;t change
        sm.timePassing(createPulse(sm.getRotationSols() - 1, 10, true));
        assertEquals("No shift changes on normal day", 0, sm.getOnLeave().size());

        // Rotate shifts. mission sol new sol flag set
        sm.timePassing(createPulse(sm.getRotationSols(), 10, true));

        List<ShiftSlot> onLeave = sm.getOnLeave();

        // Check the reported allocations
        int changedShifts = 0;
        for(Entry<ShiftSlot, Shift> e : origAllocation.entrySet()) {
            boolean changed = !e.getKey().getShift().equals(e.getValue());
            if (changed) {
                changedShifts++;
                assertTrue("Changed shift on leave", onLeave.contains(e.getKey()));
            }
        }

        assertFalse("Someone is on leave", onLeave.isEmpty());
        assertEquals("Report changed shift and actuals", onLeave.size(), changedShifts);
        assertEquals("On leave against target", (sm.getMaxOnLeave() * personCount)/100, onLeave.size());

        // Check day after rotation. mission sol new sol flag set
        sm.timePassing(createPulse(sm.getRotationSols() + 1, 10, true));
        assertTrue("SomLeave has been cleared", onLeave.isEmpty());
    }

    /**
     * Check that the OnDuty override flag works
     */
    public void testOnCal() {
        Settlement settlement = buildSettlement();

        int [] endTimes = {500, 1000};
        int [] allocations = {40, 20};
        ShiftManager sm = buildManager(settlement, endTimes, allocations);

        // Allocate a slot and get the allocated Shift 
        ShiftSlot slot = sm.allocationShift();
        int shiftEnd = slot.getShift().getEnd();

        // Shift on duty standard
        testWorkStatus(sm, slot, shiftEnd, "Standard worker", WorkStatus.ON_DUTY, WorkStatus.OFF_DUTY);

        // Check OnCall
        slot.setOnCall(true);
        testWorkStatus(sm, slot, shiftEnd, "OnCall worker", WorkStatus.ON_CALL, WorkStatus.ON_CALL);

        // Check OnLeave & OnCall. On Call takes precedence
        slot.setOnLeave(true);
        testWorkStatus(sm, slot, shiftEnd, "OnCall & OnLeave worker", WorkStatus.ON_CALL, WorkStatus.ON_CALL);

        // Check OnLeave 
        slot.setOnCall(false);
        testWorkStatus(sm, slot, shiftEnd, "OnLeave worker", WorkStatus.ON_LEAVE, WorkStatus.ON_LEAVE);
    }

    /**
     * Test the Workstatus before and after a Shift ends.
     * @param sm Shift manager controlling shifts
     * @param slot Slot being tested
     * @param shiftEnd Time the shift ends
     * @param scenario Scenario description
     * @param preEnd The Workstatus befor eshift ends
     * @param postEnd Work statu after the SHift ends
     * 
     */
    private void testWorkStatus(ShiftManager sm, ShiftSlot slot, int shiftEnd, String scenario,
                                WorkStatus preEnd, WorkStatus postEnd) {
        // Shift on duty but on call
        sm.timePassing(createPulse(1, shiftEnd - 10, false));
        assertEquals(scenario + " during shift", preEnd, slot.getStatus());

        // Shift of duty bu on call
        sm.timePassing(createPulse(1, shiftEnd, false));
        assertEquals(scenario + " after shift", postEnd, slot.getStatus());
    }

    /**
     * Creates a Clokc pulse that just contains a MarsClock.
     * @param missionSol Sol in the current mission
     * @param mSol MSol throught the day
     * @param newSol Is the new Sol flag set
     * @return
     */
    private ClockPulse createPulse(int missionSol, int mSol, boolean newSol) {
        MarsClock marsTime = new MarsClock(1, 1, 2, mSol, missionSol);
        return new ClockPulse(1, 1D, marsTime, null, null, newSol, true);
    }
}