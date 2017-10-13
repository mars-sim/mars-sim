/**
 * Mars Simulation Project
 * LoadVehicleTest.java
 * @version 3.1.0 2017-01-21
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LifeSupportType;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.equipment.SpecimenContainer;
import org.mars_sim.msp.core.person.NaturalAttribute;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.Resource;
import org.mars_sim.msp.core.structure.MockSettlement;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.MockBuilding;
import org.mars_sim.msp.core.structure.building.function.BuildingAirlock;
import org.mars_sim.msp.core.structure.building.function.EVA;
import org.mars_sim.msp.core.vehicle.MockVehicle;
import org.mars_sim.msp.core.vehicle.Vehicle;

public class LoadVehicleTest
extends TestCase {

	@Override
    public void setUp() throws Exception {
        SimulationConfig.loadConfig();
    }

    private static final String OXYGEN = LifeSupportType.OXYGEN;
	private static final String WATER = LifeSupportType.WATER;
	private static final String METHANE = "methane";
	private static final String FOOD = LifeSupportType.FOOD;

	private static final String resourceName = "hammer";
	private static final String description = "a tool";
	private static final double massPerItem = 1.4D;
	private static final int id = 1;
	
	/*
	 * Test method for 'org.mars_sim.msp.simulation.person.ai.task.LoadVehicle.LoadingPhase(double)'
	 */
	public void testLoadingPhase() throws Exception {
		Settlement settlement = new MockSettlement();

		BuildingManager buildingManager = settlement.getBuildingManager();
		MockBuilding building0 = new MockBuilding(buildingManager);
        building0.setTemplateID(0);
        building0.setName("building 0");
        building0.setWidth(9D);
        building0.setLength(9D);
        building0.setXLocation(0D);
        building0.setYLocation(0D);
        building0.setFacing(0D);
        buildingManager.addBuilding(building0, false);

        BuildingAirlock airlock0 = new BuildingAirlock(building0, 1, 0D, 0D, 0D, 0D, 0D, 0D);
        building0.addFunction(new EVA(building0, airlock0));

		//Person person = new Person("test person", PersonGender.MALE, null, settlement, "Mars Society (MS)");
		// 2017-04-11 Use Builder Pattern for creating an instance of Person
		Person person = Person.create("test person", settlement)
								.setGender(GenderType.MALE)
								.setCountry(null)
								.setSponsor("Mars Society (MS)")
								.build();
		person.initialize();
		person.getNaturalAttributeManager().setAttribute(NaturalAttribute.STRENGTH, 100);
		Vehicle vehicle = new MockVehicle(settlement);
        ItemResource hammer = ItemResource.createItemResource(resourceName,id,description,massPerItem, 1);

		Inventory vehicleInv = vehicle.getInventory();

		AmountResource oxygen = AmountResource.findAmountResource(OXYGEN);
		AmountResource food = AmountResource.findAmountResource(FOOD);
		AmountResource water = AmountResource.findAmountResource(WATER);
		AmountResource methane = AmountResource.findAmountResource(METHANE);

		vehicleInv.addAmountResourceTypeCapacity(oxygen, 100D);
		vehicleInv.addAmountResourceTypeCapacity(food, 100D);
		vehicleInv.addAmountResourceTypeCapacity(water, 100D);
		vehicleInv.addAmountResourceTypeCapacity(methane, 100D);
		vehicleInv.addGeneralCapacity(100D);

		Inventory settlementInv = settlement.getInventory();

		settlementInv.addAmountResourceTypeCapacity(oxygen, 100D);
		settlementInv.storeAmountResource(oxygen, 100D, true);
		settlementInv.addAmountResourceTypeCapacity(food, 100D);
		settlementInv.storeAmountResource(food, 100D, true);
		settlementInv.addAmountResourceTypeCapacity(water, 100D);
		settlementInv.storeAmountResource(water, 100D, true);
		settlementInv.addAmountResourceTypeCapacity(methane, 100D);
		settlementInv.storeAmountResource(methane, 100D, true);
		settlementInv.storeItemResources(hammer, 5);

		for (int x = 0; x < 5; x++) {
			settlementInv.storeUnit(new SpecimenContainer(settlement.getCoordinates()));
		}

		Map<Resource, Number> requiredResourcesMap = new HashMap<Resource, Number>();
		requiredResourcesMap.put(oxygen, new Double(100D));
		requiredResourcesMap.put(food, new Double(100D));
		requiredResourcesMap.put(water, new Double(100D));
		requiredResourcesMap.put(methane, new Double(100D));
		requiredResourcesMap.put(hammer, Integer.valueOf(5));

		Map<Resource, Number> optionalResourcesMap = new HashMap<Resource, Number>(0);

		Map<Class, Integer> requiredEquipmentMap = new HashMap<Class, Integer>();
		requiredEquipmentMap.put(SpecimenContainer.class, Integer.valueOf(5));

		Map<Class, Integer> optionalEquipmentMap = new HashMap<Class, Integer>(0);

		LoadVehicleGarage loadVehicle = new LoadVehicleGarage(person, vehicle, requiredResourcesMap, optionalResourcesMap,
		        requiredEquipmentMap, optionalEquipmentMap);
		loadVehicle.loadingPhase(10D);

		assertEquals("Vehicle loaded correctly.", 5, vehicle.getInventory().findNumUnitsOfClass(SpecimenContainer.class));
	}

	/*
	 * Test method for 'org.mars_sim.msp.simulation.person.ai.task.LoadVehicle.hasEnoughSupplies(Settlement, Map, Map)'
	 */
	public void testHasEnoughSuppliesGood() throws Exception {
		Settlement settlement = new MockSettlement();
		Inventory inv = settlement.getInventory();
        ItemResource hammer = ItemResource.createItemResource(resourceName,id,description,massPerItem, 1);

		AmountResource oxygen = AmountResource.findAmountResource(OXYGEN);
		AmountResource food = AmountResource.findAmountResource(FOOD);
		AmountResource water = AmountResource.findAmountResource(WATER);
		AmountResource methane = AmountResource.findAmountResource(METHANE);

		inv.addAmountResourceTypeCapacity(oxygen, 100D);
		inv.storeAmountResource(oxygen, 100D, true);
		inv.addAmountResourceTypeCapacity(food, 100D);
		inv.storeAmountResource(food, 100D, true);
		inv.addAmountResourceTypeCapacity(water, 100D);
		inv.storeAmountResource(water, 100D, true);
		inv.addAmountResourceTypeCapacity(methane, 100D);
		inv.storeAmountResource(methane, 100D, true);

		inv.storeItemResources(hammer, 5);

		for (int x = 0; x < 5; x++) {
			inv.storeUnit(new SpecimenContainer(settlement.getCoordinates()));
		}

		Map<Resource, Number> resourcesMap = new HashMap<Resource, Number>();
		resourcesMap.put(oxygen, new Double(100D));
		resourcesMap.put(food, new Double(100D));
		resourcesMap.put(water, new Double(100D));
		resourcesMap.put(methane, new Double(100D));
		resourcesMap.put(hammer, Integer.valueOf(5));

		Map<Class, Integer> equipmentMap = new HashMap<Class, Integer>();
		equipmentMap.put(SpecimenContainer.class, Integer.valueOf(5));

		Vehicle vehicle = new MockVehicle(settlement);

		assertTrue("Enough supplies at settlement for trip.",
				LoadVehicleGarage.hasEnoughSupplies(settlement, vehicle, resourcesMap, equipmentMap, 0, 0D));
	}

	public void testHasEnoughSuppliesNoAmountResources() throws Exception {
		Settlement settlement = new MockSettlement();
		Inventory inv = settlement.getInventory();
        ItemResource hammer = ItemResource.createItemResource(resourceName,id,description,massPerItem, 1);

		inv.storeItemResources(hammer, 5);

		for (int x = 0; x < 5; x++) {
			inv.storeUnit(new SpecimenContainer(settlement.getCoordinates()));
		}

		AmountResource oxygen = AmountResource.findAmountResource(OXYGEN);
		AmountResource food = AmountResource.findAmountResource(FOOD);
		AmountResource water = AmountResource.findAmountResource(WATER);
		AmountResource methane = AmountResource.findAmountResource(METHANE);

		Map<Resource, Number> resourcesMap = new HashMap<Resource, Number>();
		resourcesMap.put(oxygen, new Double(100D));
		resourcesMap.put(food, new Double(100D));
		resourcesMap.put(water, new Double(100D));
		resourcesMap.put(methane, new Double(100D));
		resourcesMap.put(hammer, Integer.valueOf(5));

		Map<Class, Integer> equipmentMap = new HashMap<Class, Integer>();
		equipmentMap.put(SpecimenContainer.class, Integer.valueOf(5));

		Vehicle vehicle = new MockVehicle(settlement);

		assertFalse("Not enough amount resource supplies at settlement for trip.",
				LoadVehicleGarage.hasEnoughSupplies(settlement, vehicle, resourcesMap, equipmentMap, 0, 0D));
	}

	public void testHasEnoughSuppliesNoItemResources() throws Exception {
		Settlement settlement = new MockSettlement();
		Inventory inv = settlement.getInventory();
        ItemResource hammer = ItemResource.createItemResource(resourceName,id,description,massPerItem, 1);

		AmountResource oxygen = AmountResource.findAmountResource(OXYGEN);
		AmountResource food = AmountResource.findAmountResource(FOOD);
		AmountResource water = AmountResource.findAmountResource(WATER);
		AmountResource methane = AmountResource.findAmountResource(METHANE);

		inv.addAmountResourceTypeCapacity(oxygen, 100D);
		inv.storeAmountResource(oxygen, 100D, true);
		inv.addAmountResourceTypeCapacity(food, 100D);
		inv.storeAmountResource(food, 100D, true);
		inv.addAmountResourceTypeCapacity(water, 100D);
		inv.storeAmountResource(water, 100D, true);
		inv.addAmountResourceTypeCapacity(methane, 100D);
		inv.storeAmountResource(methane, 100D, true);

		for (int x = 0; x < 5; x++) {
			inv.storeUnit(new SpecimenContainer(settlement.getCoordinates()));
		}

		Map<Resource, Number> resourcesMap = new HashMap<Resource, Number>();
		resourcesMap.put(oxygen, new Double(100D));
		resourcesMap.put(food, new Double(100D));
		resourcesMap.put(water, new Double(100D));
		resourcesMap.put(methane, new Double(100D));
		resourcesMap.put(hammer, Integer.valueOf(5));

		Map<Class, Integer> equipmentMap = new HashMap<Class, Integer>();
		equipmentMap.put(SpecimenContainer.class, Integer.valueOf(5));

		Vehicle vehicle = new MockVehicle(settlement);

		assertFalse("Not enough item resource supplies at settlement for trip.",
				LoadVehicleGarage.hasEnoughSupplies(settlement, vehicle, resourcesMap, equipmentMap, 0, 0D));
	}

	public void testHasEnoughSuppliesNoEquipment() throws Exception {
		Settlement settlement = new MockSettlement();
		Inventory inv = settlement.getInventory();
        ItemResource hammer = ItemResource.createItemResource(resourceName,id,description,massPerItem, 1);

		AmountResource oxygen = AmountResource.findAmountResource(OXYGEN);
		AmountResource food = AmountResource.findAmountResource(FOOD);
		AmountResource water = AmountResource.findAmountResource(WATER);
		AmountResource methane = AmountResource.findAmountResource(METHANE);

		inv.addAmountResourceTypeCapacity(oxygen, 100D);
		inv.storeAmountResource(oxygen, 100D, true);
		inv.addAmountResourceTypeCapacity(food, 100D);
		inv.storeAmountResource(food, 100D, true);
		inv.addAmountResourceTypeCapacity(water, 100D);
		inv.storeAmountResource(water, 100D, true);
		inv.addAmountResourceTypeCapacity(methane, 100D);
		inv.storeAmountResource(methane, 100D, true);

		inv.storeItemResources(hammer, 5);

		Map<Resource, Number> resourcesMap = new HashMap<Resource, Number>();
		resourcesMap.put(oxygen, new Double(100D));
		resourcesMap.put(food, new Double(100D));
		resourcesMap.put(water, new Double(100D));
		resourcesMap.put(methane, new Double(100D));
		resourcesMap.put(hammer, Integer.valueOf(5));

		Map<Class, Integer> equipmentMap = new HashMap<Class, Integer>();
		equipmentMap.put(SpecimenContainer.class, Integer.valueOf(5));

		Vehicle vehicle = new MockVehicle(settlement);

		assertFalse("Not enough equipment supplies at settlement for trip.",
				LoadVehicleGarage.hasEnoughSupplies(settlement, vehicle, resourcesMap, equipmentMap, 0, 0D));
	}

	/*
	 * Test method for 'org.mars_sim.msp.simulation.person.ai.task.LoadVehicle.isFullyLoaded()'
	 */
	public void testIsFullyLoadedGood() throws Exception {
		Settlement settlement = new MockSettlement();
		Vehicle vehicle = new MockVehicle(settlement);
        ItemResource hammer = ItemResource.createItemResource(resourceName,id,description,massPerItem, 1);
		Inventory inv = vehicle.getInventory();
		inv.addGeneralCapacity(100D);

		AmountResource oxygen = AmountResource.findAmountResource(OXYGEN);
		AmountResource food = AmountResource.findAmountResource(FOOD);
		AmountResource water = AmountResource.findAmountResource(WATER);
		AmountResource methane = AmountResource.findAmountResource(METHANE);

		inv.addAmountResourceTypeCapacity(oxygen, 100D);
		inv.storeAmountResource(oxygen, 100D, true);
		inv.addAmountResourceTypeCapacity(food, 100D);
		inv.storeAmountResource(food, 100D, true);
		inv.addAmountResourceTypeCapacity(water, 100D);
		inv.storeAmountResource(water, 100D, true);
		inv.addAmountResourceTypeCapacity(methane, 100D);
		inv.storeAmountResource(methane, 100D, true);
		inv.storeItemResources(hammer, 5);

		for (int x = 0; x < 5; x++) {
			inv.storeUnit(new SpecimenContainer(settlement.getCoordinates()));
		}

		Map<Resource, Number> requiredResourcesMap = new HashMap<Resource, Number>();
		requiredResourcesMap.put(oxygen, new Double(100D));
		requiredResourcesMap.put(food, new Double(100D));
		requiredResourcesMap.put(water, new Double(100D));
		requiredResourcesMap.put(methane, new Double(100D));
		requiredResourcesMap.put(hammer, Integer.valueOf(5));

		Map<Resource, Number> optionalResourcesMap = new HashMap<Resource, Number>(0);

		Map<Class, Integer> requiredEquipmentMap = new HashMap<Class, Integer>();
		requiredEquipmentMap.put(SpecimenContainer.class, Integer.valueOf(5));

		Map<Class, Integer> optionalEquipmentMap = new HashMap<Class, Integer>(0);

		assertTrue("Vehicle is fully loaded", LoadVehicleGarage.isFullyLoaded(requiredResourcesMap,
		        optionalResourcesMap, requiredEquipmentMap, optionalEquipmentMap, vehicle, settlement));
	}

	/*
	 * Test method for 'org.mars_sim.msp.simulation.person.ai.task.LoadVehicle.isFullyLoaded()'
	 */
	public void testIsFullyLoadedNoAmountResources() throws Exception {
		Settlement settlement = new MockSettlement();
		Vehicle vehicle = new MockVehicle(settlement);
        ItemResource hammer = ItemResource.createItemResource(resourceName,id,description,massPerItem, 1);
		Inventory inv = vehicle.getInventory();
		inv.addGeneralCapacity(100D);

		inv.storeItemResources(hammer, 5);

		for (int x = 0; x < 5; x++) {
			inv.storeUnit(new SpecimenContainer(settlement.getCoordinates()));
		}

		AmountResource oxygen = AmountResource.findAmountResource(OXYGEN);
		AmountResource food = AmountResource.findAmountResource(FOOD);
		AmountResource water = AmountResource.findAmountResource(WATER);
		AmountResource methane = AmountResource.findAmountResource(METHANE);

		Map<Resource, Number> requiredResourcesMap = new HashMap<Resource, Number>();
		requiredResourcesMap.put(oxygen, new Double(100D));
		requiredResourcesMap.put(food, new Double(100D));
		requiredResourcesMap.put(water, new Double(100D));
		requiredResourcesMap.put(methane, new Double(100D));
		requiredResourcesMap.put(hammer, Integer.valueOf(5));

		Map<Resource, Number> optionalResourcesMap = new HashMap<Resource, Number>(0);

		Map<Class, Integer> requiredEquipmentMap = new HashMap<Class, Integer>();
		requiredEquipmentMap.put(SpecimenContainer.class, Integer.valueOf(5));

		Map<Class, Integer> optionalEquipmentMap = new HashMap<Class, Integer>(0);

		assertFalse("Vehicle is not fully loaded", LoadVehicleGarage.isFullyLoaded(requiredResourcesMap,
		        optionalResourcesMap, requiredEquipmentMap, optionalEquipmentMap, vehicle, settlement));
	}

	/*
	 * Test method for 'org.mars_sim.msp.simulation.person.ai.task.LoadVehicle.isFullyLoaded()'
	 */
	public void testIsFullyLoadedNoItemResources() throws Exception {
		Settlement settlement = new MockSettlement();
		Vehicle vehicle = new MockVehicle(settlement);
        ItemResource hammer = ItemResource.createItemResource(resourceName,id,description,massPerItem, 1);
		Inventory inv = vehicle.getInventory();
		inv.addGeneralCapacity(100D);

		AmountResource oxygen = AmountResource.findAmountResource(OXYGEN);
		AmountResource food = AmountResource.findAmountResource(FOOD);
		AmountResource water = AmountResource.findAmountResource(WATER);
		AmountResource methane = AmountResource.findAmountResource(METHANE);

		inv.addAmountResourceTypeCapacity(oxygen, 100D);
		inv.storeAmountResource(oxygen, 100D, true);
		inv.addAmountResourceTypeCapacity(food, 100D);
		inv.storeAmountResource(food, 100D, true);
		inv.addAmountResourceTypeCapacity(water, 100D);
		inv.storeAmountResource(water, 100D, true);
		inv.addAmountResourceTypeCapacity(methane, 100D);
		inv.storeAmountResource(methane, 100D, true);

		for (int x = 0; x < 5; x++) {
			inv.storeUnit(new SpecimenContainer(settlement.getCoordinates()));
		}

		Map<Resource, Number> requiredResourcesMap = new HashMap<Resource, Number>();
		requiredResourcesMap.put(oxygen, new Double(100D));
		requiredResourcesMap.put(food, new Double(100D));
		requiredResourcesMap.put(water, new Double(100D));
		requiredResourcesMap.put(methane, new Double(100D));
		requiredResourcesMap.put(hammer, Integer.valueOf(5));

		Map<Resource, Number> optionalResourcesMap = new HashMap<Resource, Number>(0);

		Map<Class, Integer> requiredEquipmentMap = new HashMap<Class, Integer>();
		requiredEquipmentMap.put(SpecimenContainer.class, Integer.valueOf(5));

		Map<Class, Integer> optionalEquipmentMap = new HashMap<Class, Integer>(0);

		assertFalse("Vehicle is not fully loaded", LoadVehicleGarage.isFullyLoaded(requiredResourcesMap,
                optionalResourcesMap, requiredEquipmentMap, optionalEquipmentMap, vehicle, settlement));
	}

	/*
	 * Test method for 'org.mars_sim.msp.simulation.person.ai.task.LoadVehicle.isFullyLoaded()'
	 */
	public void testIsFullyLoadedNoEquipment() throws Exception {
		Settlement settlement = new MockSettlement();
		Vehicle vehicle = new MockVehicle(settlement);
        ItemResource hammer = ItemResource.createItemResource(resourceName,id,description,massPerItem, 1);
		Inventory inv = vehicle.getInventory();
		inv.addGeneralCapacity(100D);

		AmountResource oxygen = AmountResource.findAmountResource(OXYGEN);
		AmountResource food = AmountResource.findAmountResource(FOOD);
		AmountResource water = AmountResource.findAmountResource(WATER);
		AmountResource methane = AmountResource.findAmountResource(METHANE);

		inv.addAmountResourceTypeCapacity(oxygen, 100D);
		inv.storeAmountResource(oxygen, 100D, true);
		inv.addAmountResourceTypeCapacity(food, 100D);
		inv.storeAmountResource(food, 100D, true);
		inv.addAmountResourceTypeCapacity(water, 100D);
		inv.storeAmountResource(water, 100D, true);
		inv.addAmountResourceTypeCapacity(methane, 100D);
		inv.storeAmountResource(methane, 100D, true);
		inv.storeItemResources(hammer, 5);

		Map<Resource, Number> requiredResourcesMap = new HashMap<Resource, Number>();
		requiredResourcesMap.put(oxygen, new Double(100D));
		requiredResourcesMap.put(food, new Double(100D));
		requiredResourcesMap.put(water, new Double(100D));
		requiredResourcesMap.put(methane, new Double(100D));
		requiredResourcesMap.put(hammer, Integer.valueOf(5));

		Map<Resource, Number> optionalResourcesMap = new HashMap<Resource, Number>(0);

		Map<Class, Integer> requiredEquipmentMap = new HashMap<Class, Integer>();
		requiredEquipmentMap.put(SpecimenContainer.class, Integer.valueOf(5));

		Map<Class, Integer> optionalEquipmentMap = new HashMap<Class, Integer>(0);

		assertFalse("Vehicle is not fully loaded", LoadVehicleGarage.isFullyLoaded(requiredResourcesMap,
                optionalResourcesMap, requiredEquipmentMap, optionalEquipmentMap, vehicle, settlement));
	}
}