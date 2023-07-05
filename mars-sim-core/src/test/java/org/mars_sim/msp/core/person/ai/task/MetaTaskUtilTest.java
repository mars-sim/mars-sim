/*
 * Mars Simulation Project
 * MetaTaskUtilTest.java
 * @date 2022-11-15
 * @author Barry Evans
 */

package org.mars_sim.msp.core.person.ai.task;

import org.mars.sim.mapdata.location.LocalPosition;
import org.mars_sim.msp.core.AbstractMarsSimUnitTest;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.MalfunctionMeta;
import org.mars_sim.msp.core.malfunction.MalfunctionMeta.EffortSpec;
import org.mars_sim.msp.core.malfunction.MalfunctionRepairWork;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.MetaTaskUtil;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.vehicle.Vehicle;


/**
 * Tests the ability of a person to load resources into an EVA suit.
 */
public class MetaTaskUtilTest
extends AbstractMarsSimUnitTest {

	private static final LocalPosition POS = new LocalPosition(0, 0);
	private Settlement settlement;
	private Person person;

	@Override
	public void setUp() {
		super.setUp();

		MetaTaskUtil.initializeMetaTasks();

		settlement = buildSettlement();

		person = buildPerson("Test Body", settlement);

	}

	public void testEatDrink() throws Exception {
		EatDrink eatDrink = new EatDrink(person);
		assertMetaTask(eatDrink, "Eating");
	}

	public void testLoadVehicleGarage() throws Exception {
		LoadVehicleGarage evaLoad = new LoadVehicleGarage(person, buildMission(settlement, person));
		assertMetaTask(evaLoad, "Loading Vehicle");
	}

	public void testLoadVehicleEVA() throws Exception {
		LoadVehicleEVA evaLoad = new LoadVehicleEVA(person, buildMission(settlement, person));
		assertMetaTask(evaLoad, "Loading Vehicle");
	}

	public void testUnloadVehicleGarage() throws Exception {
		Vehicle vehicle = buildRover(settlement, "loader", null);

		UnloadVehicleGarage evaLoad = new UnloadVehicleGarage(person, vehicle);
		assertMetaTask(evaLoad, "Unloading Vehicle");
	}


	public void testUnloadVehicleEVA() throws Exception {
		Vehicle vehicle = buildRover(settlement, "loader", null);

		UnloadVehicleEVA evaLoad = new UnloadVehicleEVA(person, vehicle);
		assertMetaTask(evaLoad, "Unloading Vehicle");
	}

	public void testRepairInside() throws Exception {
		Building garage = buildBuilding(settlement.getBuildingManager(), POS, 0D, 1);
		Malfunction m = createMalfunction(garage, person, MalfunctionRepairWork.INSIDE);

		Task evaLoad = new RepairInsideMalfunction(person, garage, m);
		assertMetaTask(evaLoad, "Repairing Malfunction");
	}

	public void testRepairEVA() throws Exception {
		Building garage = buildBuilding(settlement.getBuildingManager(), POS, 0D, 1);

		Malfunction m = createMalfunction(garage, person, MalfunctionRepairWork.EVA);
		Task evaLoad = new RepairEVAMalfunction(person, garage, m);
		assertMetaTask(evaLoad, "Repairing Malfunction");
	}

	private Malfunction createMalfunction(Building b, Person p, MalfunctionRepairWork work) {
		MalfunctionManager mm = b.getMalfunctionManager(); 
//		mm.getMaintenanceParts();
		for(MalfunctionMeta mMeta : simConfig.getMalfunctionConfiguration().getMalfunctionList()) {
			EffortSpec w = mMeta.getRepairEffort().get(work);
			if ((w != null) && (w.getWorkTime() > 0D)) {
				return mm.triggerMalfunction(mMeta, false, p);
			}
		}

		return null;
	}

	public void testMaintainBuilding() throws Exception {
		Building garage = buildBuilding(settlement.getBuildingManager(), POS, 0D, 1);
//		garage.getMalfunctionManager().getMaintenanceParts();

		MaintainBuilding evaLoad = new MaintainBuilding(person, garage);
		assertMetaTask(evaLoad, "Maintaining Building");
	}

	public void testMaintainEVABuilding() throws Exception {
		Building garage = buildBuilding(settlement.getBuildingManager(), POS, 0D, 1);
//		garage.getMalfunctionManager().getMaintenanceParts();

		MaintainBuildingEVA evaLoad = new MaintainBuildingEVA(person, garage);
		assertMetaTask(evaLoad, "Maintaining Building");
	}


	/**
	 * Find the associated MetatTask from a specific Task and make sure it is the
	 * expected one.
	 * 
	 * @param task Seed for search
	 * @param name Expected MetaTask name
	 */
	private void assertMetaTask(Task task, String name) {
		MetaTask mt = MetaTaskUtil.getMetaTypeFromTask(task);

		assertNotNull("No MetaTask for " + task.getName(), mt);
		assertEquals("Metatask name for " + task.getName(), name, mt.getName());
	}
}