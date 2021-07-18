/**
 * Mars Simulation Project
 * LoadVehicleTest.java
 * @version 3.1.0 2017-01-21
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.util.Iterator;

import org.junit.Before;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LifeSupportInterface;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.SpecimenBox;
import org.mars_sim.msp.core.mars.Mars;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.MockSettlement;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.MockBuilding;
import org.mars_sim.msp.core.structure.building.function.BuildingAirlock;
import org.mars_sim.msp.core.structure.building.function.EVA;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.vehicle.MockVehicle;
import org.mars_sim.msp.core.vehicle.Vehicle;

import junit.framework.TestCase;

public class UnloadVehicleTest
extends TestCase {

	private UnitManager unitManager;

	@Before
	public void setUp() {
	    // Create new simulation instance.
        SimulationConfig simConfig = SimulationConfig.instance();
        simConfig.loadConfig();
        
        Simulation sim = Simulation.instance();
        sim.testRun();

        unitManager = sim.getUnitManager();
        
        Mars mars = sim.getMars();
        Function.initializeInstances(simConfig.getBuildingConfiguration(), sim.getMasterClock().getMarsClock(),
        							 simConfig.getPersonConfig(), simConfig.getCropConfiguration(), mars.getSurfaceFeatures(),
        							 mars.getWeather(), sim.getUnitManager());
	}

    public void testUnloadingPhase() throws Exception {
    	
		AmountResource oxygen = ResourceUtil.findAmountResource(LifeSupportInterface.OXYGEN);
		AmountResource food = ResourceUtil.findAmountResource(LifeSupportInterface.FOOD);
		AmountResource water = ResourceUtil.findAmountResource(LifeSupportInterface.WATER);
		AmountResource methane = ResourceUtil.findAmountResource("methane");

        String resourceName = "hammer";
        String description = "a tool";
        int id = 1;
        double massPerItem = 1.4D;
        ItemResource hammer = ItemResourceUtil.createItemResource(resourceName,id,description,massPerItem, 1);
  
		Settlement settlement = new MockSettlement();
		unitManager.addUnit(settlement);

		
		Inventory settlementInv = settlement.getInventory();
		settlementInv.addAmountResourceTypeCapacity(oxygen, 100D);
		settlementInv.addAmountResourceTypeCapacity(food, 100D);
		settlementInv.addAmountResourceTypeCapacity(water, 100D);
		settlementInv.addAmountResourceTypeCapacity(methane, 100D);

		Vehicle vehicle = new MockVehicle(settlement);
		unitManager.addUnit(vehicle);
		
		Inventory vehicleInv = vehicle.getInventory();
		vehicleInv.addAmountResourceTypeCapacity(oxygen, 100D);
		vehicleInv.storeAmountResource(oxygen, 100D, true);
		vehicleInv.addAmountResourceTypeCapacity(food, 100D);
		vehicleInv.storeAmountResource(food, 100D, true);
		vehicleInv.addAmountResourceTypeCapacity(water, 100D);
		vehicleInv.storeAmountResource(water, 100D, true);
		vehicleInv.addAmountResourceTypeCapacity(methane, 100D);
		vehicleInv.storeAmountResource(methane, 100D, true);
		vehicleInv.addGeneralCapacity(100D);
		vehicleInv.storeItemResources(hammer.getID(), 5);
		for (int x = 0; x < 5; x++) {
			Equipment eqm = new SpecimenBox(settlement.getCoordinates());
			unitManager.addUnit(eqm);
			vehicleInv.storeUnit(eqm);
		}

//		BuildingManager buildingManager = settlement.getBuildingManager();
        MockBuilding b0 = new MockBuilding(settlement.getBuildingManager(), "B0");
        b0.setTemplateID(0);
        b0.setName("B0");
        b0.setWidth(9D);
        b0.setLength(9D);
        b0.setXLocation(0D);
        b0.setYLocation(0D);
        b0.setFacing(0D);
//        buildingManager.addMockBuilding(b0);

        BuildingAirlock airlock0 = new BuildingAirlock(b0, 1, 0D, 0D, 0D, 0D, 0D, 0D);
        b0.addFunction(new EVA(b0, airlock0));

//		Person person = new Person(settlement);
//        
////		settlement.getInventory().storeUnit(person);
//		person.getNaturalAttributeManager().setAttribute(NaturalAttributeType.STRENGTH, 100);
//
//		UnloadVehicleGarage unloadVehicle = new UnloadVehicleGarage(person, vehicle);
//		unloadVehicle.unloadingPhase(11D);
//		unloadVehicle.unloadingPhase(12D);
//		unloadVehicle.unloadingPhase(13D);
//		unloadVehicle.unloadingPhase(14D);
//		unloadVehicle.unloadingPhase(15D);
//		unloadVehicle.unloadingPhase(16D);
//		unloadVehicle.unloadingPhase(100D);
//
//		assertEquals("Vehicle unloaded correctly.", 0D, vehicleInv.getTotalInventoryMass(false), 0D);
	}
}