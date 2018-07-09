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
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.equipment.SpecimenContainer;
import org.mars_sim.msp.core.person.NaturalAttributeType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
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

//    private static final String OXYGEN = LifeSupportType.OXYGEN;
//	private static final String WATER = LifeSupportType.WATER;
//	private static final String METHANE = "methane";
//	private static final String FOOD = LifeSupportType.FOOD;
//	private static final String SOYMILK = "soymilk";

	private static final String resourceName = "hammer";
	private static final String description = "a tool";
	private static final double massPerItem = 1.4D;
	private static final int id = 1;
	private static final double waterAmount = 400D;

	private static int oxygenID = ResourceUtil.oxygenID;
	private static int waterID = ResourceUtil.waterID;
	private static int foodID = ResourceUtil.foodID;
	private static int methaneID = ResourceUtil.methaneID;
//	private static int hammerID = ItemResourceUtil.hammerID;
	
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
		person.getNaturalAttributeManager().setAttribute(NaturalAttributeType.STRENGTH, 100);
		Vehicle vehicle = new MockVehicle(settlement);
        ItemResource hammer = ItemResource.createItemResource(resourceName,id,description,massPerItem, 1);

		Inventory vehicleInv = vehicle.getInventory();

//		AmountResource oxygen = AmountResource.findAmountResource(OXYGEN);
//		AmountResource food = AmountResource.findAmountResource(FOOD);
//		AmountResource water = AmountResource.findAmountResource(WATER);
//		AmountResource methane = AmountResource.findAmountResource(METHANE);
//		AmountResource soymilk = AmountResource.findAmountResource(SOYMILK);
		
		int oxygenID = ResourceUtil.oxygenID;
		int foodID = ResourceUtil.foodID;
		int waterID = ResourceUtil.waterID;
		int methaneID = ResourceUtil.methaneID;
		int soymilkID = ResourceUtil.soymilkID;

		int hammerID = hammer.getID();//ItemResourceUtil.hammerID;
		
		vehicleInv.addAmountResourceTypeCapacity(oxygenID, 100D);
		vehicleInv.addAmountResourceTypeCapacity(foodID, 100D);
		vehicleInv.addAmountResourceTypeCapacity(waterID, waterAmount);
		vehicleInv.addAmountResourceTypeCapacity(methaneID, 100D);
		vehicleInv.addAmountResourceTypeCapacity(soymilkID, 20D);
		vehicleInv.storeAmountResource(soymilkID, 20D, true);
		vehicleInv.addGeneralCapacity(100D);

		Inventory settlementInv = settlement.getInventory();

		settlementInv.addAmountResourceTypeCapacity(oxygenID, 100D);
		settlementInv.storeAmountResource(oxygenID, 100D, true);
		settlementInv.addAmountResourceTypeCapacity(foodID, 100D);
		settlementInv.storeAmountResource(foodID, 100D, true);
		settlementInv.addAmountResourceTypeCapacity(waterID, waterAmount);
		settlementInv.storeAmountResource(waterID, waterAmount, true);
		settlementInv.addAmountResourceTypeCapacity(methaneID, 100D);
		settlementInv.storeAmountResource(methaneID, 100D, true);
		settlementInv.addAmountResourceTypeCapacity(soymilkID, 20D);
		settlementInv.storeAmountResource(soymilkID, 20D, true);
		settlementInv.storeItemResources(hammerID, 5);

		Map<Integer, Number> resourcesMap = new HashMap<Integer, Number>();
		resourcesMap.put(oxygenID, new Double(100D));
		resourcesMap.put(foodID, new Double(100D));
		resourcesMap.put(waterID, new Double(waterAmount));
		resourcesMap.put(methaneID, new Double(100D));
		resourcesMap.put(soymilkID, new Double(20D));
		resourcesMap.put(hammerID, Integer.valueOf(5));
		
		
		for (int x = 0; x < 5; x++) {
			settlementInv.storeUnit(new SpecimenContainer(settlement.getCoordinates()));
		}

		Map<Integer, Number> requiredResourcesMap = new HashMap<Integer, Number>();
		requiredResourcesMap.put(oxygenID, new Double(100D));
		requiredResourcesMap.put(foodID, new Double(100D));
		requiredResourcesMap.put(waterID, new Double(waterAmount));
		requiredResourcesMap.put(methaneID, new Double(100D));
		requiredResourcesMap.put(hammerID, Integer.valueOf(5));

		Map<Integer, Number> optionalResourcesMap = new HashMap<Integer, Number>(0);

		Map<Integer, Integer> requiredEquipmentMap = new HashMap<>();
		requiredEquipmentMap.put(EquipmentType.str2int(SpecimenContainer.TYPE), Integer.valueOf(5));

		Map<Integer, Integer> optionalEquipmentMap = new HashMap<>(0);

		LoadVehicleGarage loadVehicle = new LoadVehicleGarage(person, vehicle, requiredResourcesMap, optionalResourcesMap,
		        requiredEquipmentMap, optionalEquipmentMap);
		loadVehicle.loadingPhase(10D);

		assertEquals("Vehicle loaded correctly.", 5, vehicle.getInventory().findNumUnitsOfClass(SpecimenContainer.class));
	}

	/*
	 * Test method for 'org.mars_sim.msp.simulation.person.ai.task.LoadVehicle.hasEnoughSupplies(Settlement, Map, Map)'

	public void testHasEnoughSuppliesGood() throws Exception {
		Settlement settlement = new MockSettlement();
		Inventory inv = settlement.getInventory();
        ItemResource hammer = ItemResource.createItemResource(resourceName,id,description,massPerItem, 1);

		AmountResource oxygen = AmountResource.findAmountResource(OXYGEN);
		AmountResource food = AmountResource.findAmountResource(FOOD);
		AmountResource water = AmountResource.findAmountResource(WATER);
		AmountResource methane = AmountResource.findAmountResource(METHANE);
		AmountResource soymilk = AmountResource.findAmountResource(SOYMILK);

		inv.addAmountResourceTypeCapacity(oxygenID, 100D);
		inv.storeAmountResource(oxygenID, 100D, true);
		inv.addAmountResourceTypeCapacity(foodID, 200D);
		inv.storeAmountResource(foodID, 200D, true);
		inv.addAmountResourceTypeCapacity(waterID, waterAmount);
		inv.storeAmountResource(waterID, waterAmount, true);
		inv.addAmountResourceTypeCapacity(methaneID, 100D);
		inv.storeAmountResource(methaneID, 100D, true);
		inv.addAmountResourceTypeCapacity(soymilk, 20D);
		inv.storeAmountResource(soymilk, 20D, true);
		
		inv.storeItemResources(hammerID, 5);

		for (int x = 0; x < 5; x++) {
			inv.storeUnit(new SpecimenContainer(settlement.getCoordinates()));
		}

		Map<Resource, Number> resourcesMap = new HashMap<Resource, Number>();
		resourcesMap.put(oxygenID, new Double(100D));
		resourcesMap.put(foodID, new Double(200D));
		resourcesMap.put(waterID, new Double(waterAmount));
		resourcesMap.put(methaneID, new Double(100D));
		resourcesMap.put(soymilk, new Double(20D));
		resourcesMap.put(hammerID, Integer.valueOf(5));

		Map<Class, Integer> equipmentMap = new HashMap<Class, Integer>();
		equipmentMap.put(SpecimenContainer.class, Integer.valueOf(5));

		Vehicle vehicle = new MockVehicle(settlement);

		assertTrue("Enough supplies at settlement for trip.",
				LoadVehicleGarage.hasEnoughSupplies(settlement, vehicle, resourcesMap, equipmentMap, 2, 1D));
	}
*/
	
	public void testHasEnoughSuppliesNoAmountResources() throws Exception {
		Settlement settlement = new MockSettlement();
		Inventory inv = settlement.getInventory();
        ItemResource hammer = ItemResource.createItemResource(resourceName,id,description,massPerItem, 1);
		int hammerID = hammer.getID();
		
		inv.storeItemResources(hammerID, 5);

		for (int x = 0; x < 5; x++) {
			inv.storeUnit(new SpecimenContainer(settlement.getCoordinates()));
		}

//		AmountResource oxygen = AmountResource.findAmountResource(OXYGEN);
//		AmountResource food = AmountResource.findAmountResource(FOOD);
//		AmountResource water = AmountResource.findAmountResource(WATER);
//		AmountResource methane = AmountResource.findAmountResource(METHANE);

		Map<Integer, Number> resourcesMap = new HashMap<Integer, Number>();
		resourcesMap.put(oxygenID, new Double(100D));
		resourcesMap.put(foodID, new Double(100D));
		resourcesMap.put(waterID, new Double(waterAmount));
		resourcesMap.put(methaneID, new Double(100D));
		resourcesMap.put(hammerID, Integer.valueOf(5));

		Map<Integer, Integer> equipmentMap = new HashMap<>();
		equipmentMap.put(EquipmentType.str2int(SpecimenContainer.TYPE), Integer.valueOf(5));

		Vehicle vehicle = new MockVehicle(settlement);

		assertFalse("Not enough amount resource supplies at settlement for trip.",
				LoadVehicleGarage.hasEnoughSupplies(settlement, vehicle, resourcesMap, equipmentMap, 2, 1D));
	}

	public void testHasEnoughSuppliesNoItemResources() throws Exception {
		Settlement settlement = new MockSettlement();
		Inventory inv = settlement.getInventory();
        ItemResource hammer = ItemResource.createItemResource(resourceName,id,description,massPerItem, 1);
		int hammerID = hammer.getID();
		
//		AmountResource oxygen = AmountResource.findAmountResource(OXYGEN);
//		AmountResource food = AmountResource.findAmountResource(FOOD);
//		AmountResource water = AmountResource.findAmountResource(WATER);
//		AmountResource methane = AmountResource.findAmountResource(METHANE);

		inv.addAmountResourceTypeCapacity(oxygenID, 100D);
		inv.storeAmountResource(oxygenID, 100D, true);
		inv.addAmountResourceTypeCapacity(foodID, 100D);
		inv.storeAmountResource(foodID, 100D, true);
		inv.addAmountResourceTypeCapacity(waterID, waterAmount);
		inv.storeAmountResource(waterID, waterAmount, true);
		inv.addAmountResourceTypeCapacity(methaneID, 100D);
		inv.storeAmountResource(methaneID, 100D, true);

		for (int x = 0; x < 5; x++) {
			inv.storeUnit(new SpecimenContainer(settlement.getCoordinates()));
		}

		Map<Integer, Number> resourcesMap = new HashMap<Integer, Number>();
		resourcesMap.put(oxygenID, new Double(100D));
		resourcesMap.put(foodID, new Double(100D));
		resourcesMap.put(waterID, new Double(waterAmount));
		resourcesMap.put(methaneID, new Double(100D));
		resourcesMap.put(hammerID, Integer.valueOf(5));

		Map<Integer, Integer> equipmentMap = new HashMap<>();
		equipmentMap.put(EquipmentType.str2int(SpecimenContainer.TYPE), Integer.valueOf(5));

		Vehicle vehicle = new MockVehicle(settlement);

		assertFalse("Not enough item resource supplies at settlement for trip.",
				LoadVehicleGarage.hasEnoughSupplies(settlement, vehicle, resourcesMap, equipmentMap, 2, 1D));
	}

	public void testHasEnoughSuppliesNoEquipment() throws Exception {
		Settlement settlement = new MockSettlement();
		Inventory inv = settlement.getInventory();
        ItemResource hammer = ItemResource.createItemResource(resourceName,id,description,massPerItem, 1);

//		AmountResource oxygen = AmountResource.findAmountResource(OXYGEN);
//		AmountResource food = AmountResource.findAmountResource(FOOD);
//		AmountResource water = AmountResource.findAmountResource(WATER);
//		AmountResource methane = AmountResource.findAmountResource(METHANE);

        int hammerID = hammer.getID();
        
		inv.addAmountResourceTypeCapacity(oxygenID, 100D);
		inv.storeAmountResource(oxygenID, 100D, true);
		inv.addAmountResourceTypeCapacity(foodID, 100D);
		inv.storeAmountResource(foodID, 100D, true);
		inv.addAmountResourceTypeCapacity(waterID, waterAmount);
		inv.storeAmountResource(waterID, waterAmount, true);
		inv.addAmountResourceTypeCapacity(methaneID, 100D);
		inv.storeAmountResource(methaneID, 100D, true);

		inv.storeItemResources(hammerID, 5);

		Map<Integer, Number> resourcesMap = new HashMap<Integer, Number>();
		resourcesMap.put(oxygenID, new Double(100D));
		resourcesMap.put(foodID, new Double(100D));
		resourcesMap.put(waterID, new Double(waterAmount));
		resourcesMap.put(methaneID, new Double(100D));
		resourcesMap.put(hammerID, Integer.valueOf(5));

		Map<Integer, Integer> equipmentMap = new HashMap<>();
		equipmentMap.put(EquipmentType.str2int(SpecimenContainer.TYPE), Integer.valueOf(5));

		Vehicle vehicle = new MockVehicle(settlement);

		assertFalse("Not enough equipment supplies at settlement for trip.",
				LoadVehicleGarage.hasEnoughSupplies(settlement, vehicle, resourcesMap, equipmentMap, 2, 1D));
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

//		AmountResource oxygen = AmountResource.findAmountResource(OXYGEN);
//		AmountResource food = AmountResource.findAmountResource(FOOD);
//		AmountResource water = AmountResource.findAmountResource(WATER);
//		AmountResource methane = AmountResource.findAmountResource(METHANE);

		int hammerID = hammer.getID();
		
		inv.addAmountResourceTypeCapacity(oxygenID, 100D);
		inv.storeAmountResource(oxygenID, 100D, true);
		inv.addAmountResourceTypeCapacity(foodID, 100D);
		inv.storeAmountResource(foodID, 100D, true);
		inv.addAmountResourceTypeCapacity(waterID, waterAmount);
		inv.storeAmountResource(waterID, waterAmount, true);
		inv.addAmountResourceTypeCapacity(methaneID, 100D);
		inv.storeAmountResource(methaneID, 100D, true);
		inv.storeItemResources(hammerID, 5);

		for (int x = 0; x < 5; x++) {
			inv.storeUnit(new SpecimenContainer(settlement.getCoordinates()));
		}

		Map<Integer, Number> requiredResourcesMap = new HashMap<Integer, Number>();
		requiredResourcesMap.put(oxygenID, 100D);
		requiredResourcesMap.put(foodID, 100D);
		requiredResourcesMap.put(waterID, waterAmount);
		requiredResourcesMap.put(methaneID, 100D);
		requiredResourcesMap.put(hammerID, Integer.valueOf(5));

		Map<Integer, Number> optionalResourcesMap = new HashMap<Integer, Number>(0);

		Map<Integer, Integer> requiredEquipmentMap = new HashMap<>();
		requiredEquipmentMap.put(EquipmentType.str2int(SpecimenContainer.TYPE), Integer.valueOf(5));

		Map<Integer, Integer> optionalEquipmentMap = new HashMap<>(0);

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

//		AmountResource oxygen = AmountResource.findAmountResource(OXYGEN);
//		AmountResource food = AmountResource.findAmountResource(FOOD);
//		AmountResource water = AmountResource.findAmountResource(WATER);
//		AmountResource methane = AmountResource.findAmountResource(METHANE);

		int hammerID = hammer.getID();
		
		Map<Integer, Number> requiredResourcesMap = new HashMap<Integer, Number>();
		requiredResourcesMap.put(oxygenID, 100D);
		requiredResourcesMap.put(foodID, 100D);
		requiredResourcesMap.put(waterID, waterAmount);
		requiredResourcesMap.put(methaneID, 100D);
		requiredResourcesMap.put(hammerID, 5);

		Map<Integer, Number> optionalResourcesMap = new HashMap<Integer, Number>(0);

		Map<Integer, Integer> requiredEquipmentMap = new HashMap<>();
		requiredEquipmentMap.put(EquipmentType.str2int(SpecimenContainer.TYPE), Integer.valueOf(5));

		Map<Integer, Integer> optionalEquipmentMap = new HashMap<>(0);

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

//		AmountResource oxygen = AmountResource.findAmountResource(OXYGEN);
//		AmountResource food = AmountResource.findAmountResource(FOOD);
//		AmountResource water = AmountResource.findAmountResource(WATER);
//		AmountResource methane = AmountResource.findAmountResource(METHANE);

		int hammerID = hammer.getID();
		
		inv.addAmountResourceTypeCapacity(oxygenID, 100D);
		inv.storeAmountResource(oxygenID, 100D, true);
		inv.addAmountResourceTypeCapacity(foodID, 100D);
		inv.storeAmountResource(foodID, 100D, true);
		inv.addAmountResourceTypeCapacity(waterID, waterAmount);
		inv.storeAmountResource(waterID, waterAmount, true);
		inv.addAmountResourceTypeCapacity(methaneID, 100D);
		inv.storeAmountResource(methaneID, 100D, true);

		for (int x = 0; x < 5; x++) {
			inv.storeUnit(new SpecimenContainer(settlement.getCoordinates()));
		}

		Map<Integer, Number> requiredResourcesMap = new HashMap<Integer, Number>();
		requiredResourcesMap.put(oxygenID, new Double(100D));
		requiredResourcesMap.put(foodID, new Double(100D));
		requiredResourcesMap.put(waterID, new Double(waterAmount));
		requiredResourcesMap.put(methaneID, new Double(100D));
		requiredResourcesMap.put(hammerID, Integer.valueOf(5));

		Map<Integer, Number> optionalResourcesMap = new HashMap<Integer, Number>(0);

		Map<Integer, Integer> requiredEquipmentMap = new HashMap<>();
		requiredEquipmentMap.put(EquipmentType.str2int(SpecimenContainer.TYPE), Integer.valueOf(5));

		Map<Integer, Integer> optionalEquipmentMap = new HashMap<>(0);

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

//		AmountResource oxygen = AmountResource.findAmountResource(OXYGEN);
//		AmountResource food = AmountResource.findAmountResource(FOOD);
//		AmountResource water = AmountResource.findAmountResource(WATER);
//		AmountResource methane = AmountResource.findAmountResource(METHANE);

		int hammerID = hammer.getID();
		
		inv.addAmountResourceTypeCapacity(oxygenID, 100D);
		inv.storeAmountResource(oxygenID, 100D, true);
		inv.addAmountResourceTypeCapacity(foodID, 100D);
		inv.storeAmountResource(foodID, 100D, true);
		inv.addAmountResourceTypeCapacity(waterID, waterAmount);
		inv.storeAmountResource(waterID, waterAmount, true);
		inv.addAmountResourceTypeCapacity(methaneID, 100D);
		inv.storeAmountResource(methaneID, 100D, true);
		inv.storeItemResources(hammerID, 5);

		Map<Integer, Number> requiredResourcesMap = new HashMap<Integer, Number>();
		requiredResourcesMap.put(oxygenID, new Double(100D));
		requiredResourcesMap.put(foodID, new Double(100D));
		requiredResourcesMap.put(waterID, new Double(waterAmount));
		requiredResourcesMap.put(methaneID, new Double(100D));
		requiredResourcesMap.put(hammerID, Integer.valueOf(5));

		Map<Integer, Number> optionalResourcesMap = new HashMap<Integer, Number>(0);

		Map<Integer, Integer> requiredEquipmentMap = new HashMap<>();
		requiredEquipmentMap.put(EquipmentType.str2int(SpecimenContainer.TYPE), Integer.valueOf(5));

		Map<Integer, Integer> optionalEquipmentMap = new HashMap<>(0);

		assertFalse("Vehicle is not fully loaded", LoadVehicleGarage.isFullyLoaded(requiredResourcesMap,
                optionalResourcesMap, requiredEquipmentMap, optionalEquipmentMap, vehicle, settlement));
	}
}