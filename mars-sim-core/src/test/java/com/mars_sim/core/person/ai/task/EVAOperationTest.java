package com.mars_sim.core.person.ai.task;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.EVA;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.equipment.EVASuit;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.CoordinatesException;
import com.mars_sim.core.map.location.CoordinatesFormat;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.EVAOperation.LightLevel;
import com.mars_sim.core.person.ai.task.util.PersonTaskManager;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.science.task.MarsSimContext;
import com.mars_sim.core.time.MarsTime;

public class EVAOperationTest extends AbstractMarsSimUnitTest{

    private static final int MAX_EVA_CALLS = 1500;

    /**
     * Helper method to advance an EVAOperation through an EVA.
     * @param context The context to create any entities
     * @param eva The building to use as EVA
     * @param task The EVA operation task to advance
     * @return Calls needed.
     */
    public static int executeEVAWalk(MarsSimContext context, EVA eva, EVAOperation task) {
        var person = (Person)task.getWorker();
		PersonTaskManager tm = person.getMind().getTaskManager();
		tm.replaceTask(task);
		
        var onSite = task.getOutsidePhase();
		int callsUsed = 0;
        MarsTime now = context.getSim().getMasterClock().getMarsTime();
		while ((callsUsed < MAX_EVA_CALLS) && !task.isDone() && !onSite.equals(task.getPhase())) {
			tm.executeTask(MSOLS_PER_EXECUTE);
            now = now.addTime(MSOLS_PER_EXECUTE);

            // Due to a mis-design in the Airlock code; the operator is elected via a pulse instead of
            // proactively in the pre-breathe phase
            var pulse = context.createPulse(now, false, false);
            eva.timePassing(pulse);

			callsUsed++;
		}
		
		return callsUsed;
	}

    /**
     * Prepare context for a person to do an EVA. Create the EVA in teh settlemetn and put the Person
     * within the Building. Creates an EVASuit and loads it with required resources.
     */
    public static EVA prepareForEva(MarsSimContext context, Person p) {
        var s = p.getAssociatedSettlement();
        var b = context.buildEVA(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 0);
        BuildingManager.addToActivitySpot(p, b, FunctionType.EVA);
        Equipment e = EquipmentFactory.createEquipment(EquipmentType.EVA_SUIT, s);
        e.storeAmountResource(ResourceUtil.OXYGEN_ID, EVASuit.OXYGEN_CAPACITY);
        e.storeAmountResource(ResourceUtil.WATER_ID, EVASuit.WATER_CAPACITY);

        // Mark sure person is fit
        var pc = p.getPhysicalCondition();
        pc.setFatigue(0);
        pc.setHunger(0D);
        pc.setThirst(0);
        pc.setStress(0);

        return b.getEVA();
    }
    /**
     * This test does not attempt to check the solar irradiance logic just the light levels.
     * @throws CoordinatesException 
     */
    public void testIsSunlightAroundGlobal() throws CoordinatesException {
        var locn = CoordinatesFormat.fromString("0.0 0.0");
        
        assertLightLevel("zero time, center locn", locn, false, false);

        // First 90 degress is on the darkside
        
        locn = CoordinatesFormat.fromString("0.0 120.0");
        assertLightLevel("zero time, quarter locn", locn, true, false);

        locn = CoordinatesFormat.fromString("0.0 180.0");
        assertLightLevel("zero time, half locn", locn, true, true);

        locn = CoordinatesFormat.fromString("0.0 -120.0");
        assertLightLevel("zero time, three quarters locn", locn, true, false);
    }

    private void assertLightLevel(String message, Coordinates locn, boolean low, boolean high) {
        // Always returns true
        assertEquals(message + " none level", true, EVAOperation.isSunlightAboveLevel(locn, LightLevel.NONE));
        assertEquals(message + " low level", low, EVAOperation.isSunlightAboveLevel(locn, LightLevel.LOW));
        assertEquals(message + " high level", high, EVAOperation.isSunlightAboveLevel(locn, LightLevel.HIGH));
    }
}
