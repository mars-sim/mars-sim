/**
 * Mars Simulation Project
 * LoadVehicleTest.java
 * @version 3.1.0 2017-01-21
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.equipment.SpecimenBox;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.MockSettlement;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.MockVehicle;
import org.mars_sim.msp.core.vehicle.Vehicle;

import junit.framework.TestCase;

public class LoadVehicleTest
extends TestCase {

//	private static final Logger logger = Logger.getLogger(LoadVehicleTest.class.getName());

	private Settlement settlement = null;
	
	@Override
    public void setUp() throws Exception {
        SimulationConfig.instance().loadConfig();
        Simulation.instance().testRun();
        
        UnitManager unitManager = Simulation.instance().getUnitManager();
		Iterator<Settlement> i = unitManager.getSettlements().iterator();
		while (i.hasNext()) {
			unitManager.removeUnit(i.next());
		}
				
		// Create test settlement.
		settlement = new MockSettlement();
		
        BuildingManager buildingManager = settlement.getBuildingManager();
        
		// Removes all mock buildings and building functions in the settlement.
		buildingManager.removeAllMockBuildings();
		
		unitManager.addUnit(settlement);
    }

	private static final String resourceName = "hammer";
	private static final String description = "a tool";
	private static final double massPerItem = 1.4D;
	private static final int id = 1001;
	private static final double waterAmount = 400D;

//	private static int oxygenID = ResourceUtil.oxygenID;
//	private static int waterID = ResourceUtil.waterID;
//	private static int foodID = ResourceUtil.foodID;
//	private static int methaneID = ResourceUtil.methaneID;
//	private static int hammerID = ItemResourceUtil.hammerID;
//	private static int soymilkID = ResourceUtil.soymilkID;
	
	/*
	 * Test method for 'org.mars_sim.msp.simulation.person.ai.task.LoadVehicle.LoadingPhase(double)'
	 */
//	public void testLoadingPhase() throws Exception {
//		Settlement settlement = new MockSettlement();
//
//		BuildingManager buildingManager = settlement.getBuildingManager();
//		MockBuilding building0 = new MockBuilding(buildingManager);
//        building0.setTemplateID(0);
//        building0.setName("building 0");
//        building0.setWidth(9D);
//        building0.setLength(9D);
//        building0.setXLocation(0D);
//        building0.setYLocation(0D);
//        building0.setFacing(0D);
//        buildingManager.addMockBuilding(building0);
//
//        BuildingAirlock airlock0 = new BuildingAirlock(building0, 1, 0D, 0D, 0D, 0D, 0D, 0D);
//        building0.addFunction(new EVA(building0, airlock0));
//
//		Person person = new Person(settlement);
//
//		Vehicle vehicle = new MockVehicle(settlement);
//        Part hammer = ItemResourceUtil.createItemResource(resourceName, id, description, massPerItem, 1);
//        
//		Inventory vehicleInv = vehicle.getInventory();
//
//		int oxygenID = ResourceUtil.oxygenID;
//		int foodID = ResourceUtil.foodID;
//		int waterID = ResourceUtil.waterID;
//		int methaneID = ResourceUtil.methaneID;
//		int soymilkID = ResourceUtil.soymilkID;
//
//		int hammerID = hammer.getID();//ItemResourceUtil.hammerID;
//		
//		vehicleInv.addAmountResourceTypeCapacity(oxygenID, 100D);
//		vehicleInv.addAmountResourceTypeCapacity(foodID, 100D);
//		vehicleInv.addAmountResourceTypeCapacity(waterID, waterAmount);
//		vehicleInv.addAmountResourceTypeCapacity(methaneID, 100D);
//		vehicleInv.addAmountResourceTypeCapacity(soymilkID, 20D);
//		vehicleInv.storeAmountResource(soymilkID, 20D, true);
//		vehicleInv.addGeneralCapacity(100D);
//
//		Inventory settlementInv = settlement.getInventory();
//
//		settlementInv.addAmountResourceTypeCapacity(oxygenID, 100D);
//		settlementInv.storeAmountResource(oxygenID, 100D, true);
//		settlementInv.addAmountResourceTypeCapacity(foodID, 100D);
//		settlementInv.storeAmountResource(foodID, 100D, true);
//		settlementInv.addAmountResourceTypeCapacity(waterID, waterAmount);
//		settlementInv.storeAmountResource(waterID, waterAmount, true);
//		settlementInv.addAmountResourceTypeCapacity(methaneID, 100D);
//		settlementInv.storeAmountResource(methaneID, 100D, true);
//		settlementInv.addAmountResourceTypeCapacity(soymilkID, 20D);
//		settlementInv.storeAmountResource(soymilkID, 20D, true);
//		settlementInv.storeItemResources(hammerID, 5);
//
//		Map<Integer, Number> resourcesMap = new HashMap<Integer, Number>();
//		resourcesMap.put(oxygenID, 100D);
//		resourcesMap.put(foodID, 100D);
//		resourcesMap.put(waterID, waterAmount);
//		resourcesMap.put(methaneID, 100D);
//		resourcesMap.put(soymilkID, 20D);
//		resourcesMap.put(hammerID, Integer.valueOf(5));
//		
//		
//		for (int x = 0; x < 5; x++) {
//			settlementInv.storeUnit(new SpecimenBox(settlement.getCoordinates()));
//		}
//
//		Map<Integer, Number> requiredResourcesMap = new HashMap<Integer, Number>();
//		requiredResourcesMap.put(oxygenID, 100D);
//		requiredResourcesMap.put(foodID, 100D);
//		requiredResourcesMap.put(waterID, waterAmount);
//		requiredResourcesMap.put(methaneID, 100D);
//		requiredResourcesMap.put(hammerID, Integer.valueOf(5));
//
//		Map<Integer, Number> optionalResourcesMap = new HashMap<Integer, Number>(0);
//
//		Map<Integer, Integer> requiredEquipmentMap = new HashMap<>();
//		requiredEquipmentMap.put(EquipmentType.convertName2ID(SpecimenBox.TYPE), Integer.valueOf(5));
//
//		Map<Integer, Integer> optionalEquipmentMap = new HashMap<>(0);
//
//		LoadVehicleGarage loadVehicle = new LoadVehicleGarage(person, vehicle, requiredResourcesMap, optionalResourcesMap,
//		        requiredEquipmentMap, optionalEquipmentMap);
//		loadVehicle.loadingPhase(10D);
//
//		assertEquals("Vehicle loaded correctly.", 5, vehicle.getInventory().findNumUnitsOfClass(SpecimenBox.class));
//	}

//	/*
//	 * Test method for 'org.mars_sim.msp.simulation.person.ai.task.LoadVehicle.hasEnoughSupplies(Settlement, Map, Map)'
//	 */
//	public void testHasEnoughSuppliesGood() throws Exception {
//		Settlement settlement = new MockSettlement();
//		Inventory inv = settlement.getInventory();
////        ItemResource hammer = ItemResourceUtil.createItemResource(resourceName,id,description,massPerItem, 1);
//        Part hammer = ItemResourceUtil.createItemResource(resourceName, id, description, massPerItem, 1);
//        
////		AmountResource oxygen = AmountResource.findAmountResource(OXYGEN);
////		AmountResource food = AmountResource.findAmountResource(FOOD);
////		AmountResource water = AmountResource.findAmountResource(WATER);
////		AmountResource methane = AmountResource.findAmountResource(METHANE);
////		AmountResource soymilk = AmountResource.findAmountResource(SOYMILK);
//
//		inv.addAmountResourceTypeCapacity(oxygenID, 100D);
//		inv.storeAmountResource(oxygenID, 100D, true);
//		inv.addAmountResourceTypeCapacity(foodID, 200D);
//		inv.storeAmountResource(foodID, 200D, true);
//		inv.addAmountResourceTypeCapacity(waterID, waterAmount);
//		inv.storeAmountResource(waterID, waterAmount, true);
//		inv.addAmountResourceTypeCapacity(methaneID, 100D);
//		inv.storeAmountResource(methaneID, 100D, true);
//		inv.addAmountResourceTypeCapacity(soymilkID, 20D);
//		inv.storeAmountResource(soymilkID, 20D, true);
//		
//		inv.storeItemResources(hammer.getID(), 5);
//
//		for (int x = 0; x < 5; x++) {
//			inv.storeUnit(new SpecimenContainer(settlement.getCoordinates()));
//		}
//
//		Map<Integer, Number> resourcesMap = new HashMap<Integer, Number>();
//		resourcesMap.put(oxygenID, 100D);
//		resourcesMap.put(foodID, 200D);
//		resourcesMap.put(waterID, waterAmount);
//		resourcesMap.put(methaneID, 100D);
//		resourcesMap.put(soymilkID, 20D);
//		resourcesMap.put(hammer.getID(), Integer.valueOf(5));
//
//		Map<Integer, Integer> equipmentMap = new HashMap<>();
////		equipmentMap.put(SpecimenContainer.class, Integer.valueOf(5));
//		equipmentMap.put(EquipmentType.str2int(SpecimenContainer.TYPE), Integer.valueOf(5));
//		
//		Vehicle vehicle = new MockVehicle(settlement);
//
//		assertTrue("Enough supplies at settlement for trip.",
//				LoadVehicleGarage.hasEnoughSupplies(settlement, vehicle, resourcesMap, equipmentMap, 2, 1D));
//	}
	
//	public void testHasEnoughSuppliesNoAmountResources() throws Exception {
//		Settlement settlement = new MockSettlement();
//		Inventory inv = settlement.getInventory();
////        ItemResource hammer = ItemResourceUtil.createItemResource(resourceName,id,description,massPerItem, 1);
//        Part hammer = ItemResourceUtil.createItemResource(resourceName, id, description, massPerItem, 1);
//		int hammerID = hammer.getID();
//		
//		int oxygenID = ResourceUtil.oxygenID;
//		int foodID = ResourceUtil.foodID;
//		int waterID = ResourceUtil.waterID;
//		int methaneID = ResourceUtil.methaneID;
//		
//		inv.storeItemResources(hammerID, 5);
//
//		for (int x = 0; x < 5; x++) {
//			inv.storeUnit(new SpecimenContainer(settlement.getCoordinates()));
//		}
//
//		Map<Integer, Number> resourcesMap = new HashMap<Integer, Number>();
//		resourcesMap.put(oxygenID, 100D);
//		resourcesMap.put(foodID, 100D);
//		resourcesMap.put(waterID, waterAmount);
//		resourcesMap.put(methaneID, 100D);
//		resourcesMap.put(hammerID, 5);
//
//		Map<Integer, Integer> equipmentMap = new HashMap<>();
//		equipmentMap.put(EquipmentType.convertType2ID(SpecimenContainer.TYPE), 5);
//
//		Vehicle vehicle = new MockVehicle(settlement);
////		logger.severe("vehicle : " + vehicle);
//		assertFalse("Not enough amount resource supplies at settlement for trip.",
//				LoadVehicleGarage.hasEnoughSupplies(settlement, vehicle, resourcesMap, equipmentMap, 2, 1D));
//	}

//	public void testHasEnoughSuppliesNoItemResources() throws Exception {
//		Settlement settlement = new MockSettlement();
//		Inventory inv = settlement.getInventory();
////        ItemResource hammer = ItemResourceUtil.createItemResource(resourceName,id,description,massPerItem, 1);
//        Part hammer = ItemResourceUtil.createItemResource(resourceName, id, description, massPerItem, 1);
//		int hammerID = hammer.getID();
//
//		int oxygenID = ResourceUtil.oxygenID;
//		int foodID = ResourceUtil.foodID;
//		int waterID = ResourceUtil.waterID;
//		int methaneID = ResourceUtil.methaneID;
//		
//		inv.addAmountResourceTypeCapacity(oxygenID, 100D);
//		inv.storeAmountResource(oxygenID, 100D, true);
//		inv.addAmountResourceTypeCapacity(foodID, 100D);
//		inv.storeAmountResource(foodID, 100D, true);
//		inv.addAmountResourceTypeCapacity(waterID, waterAmount);
//		inv.storeAmountResource(waterID, waterAmount, true);
//		inv.addAmountResourceTypeCapacity(methaneID, 100D);
//		inv.storeAmountResource(methaneID, 100D, true);
//
//		for (int x = 0; x < 5; x++) {
//			inv.storeUnit(new SpecimenContainer(settlement.getCoordinates()));
//		}
//
//		Map<Integer, Number> resourcesMap = new HashMap<Integer, Number>();
//		resourcesMap.put(oxygenID, 100D);
//		resourcesMap.put(foodID, 100D);
//		resourcesMap.put(waterID, waterAmount);
//		resourcesMap.put(methaneID, 100D);
//		resourcesMap.put(hammerID, 5);
//
//		Map<Integer, Integer> equipmentMap = new HashMap<>();
//		equipmentMap.put(EquipmentType.convertType2ID(SpecimenContainer.TYPE), 5);
//
//		Vehicle vehicle = new MockVehicle(settlement);
////		Inventory vInv = vehicle.getInventory();
////		vInv.addAmountResourceTypeCapacity(oxygenID, 100D);
////		vInv.storeAmountResource(oxygenID, 100D, true);
////		vInv.addAmountResourceTypeCapacity(foodID, 100D);
////		vInv.storeAmountResource(foodID, 100D, true);
////		vInv.addAmountResourceTypeCapacity(waterID, waterAmount);
////		vInv.storeAmountResource(waterID, waterAmount, true);
////		vInv.addAmountResourceTypeCapacity(methaneID, 100D);
////		vInv.storeAmountResource(methaneID, 100D, true);
////		vInv.storeItemResources(hammerID, 5);
//		
//		assertFalse("Not enough item resource supplies at settlement for trip.",
//				LoadVehicleGarage.hasEnoughSupplies(settlement, vehicle, resourcesMap, equipmentMap, 2, 1D));
//	}

//	public void testHasEnoughSuppliesNoEquipment() throws Exception {
//		Settlement settlement = new MockSettlement();
//		Inventory inv = settlement.getInventory();
////        ItemResource hammer = ItemResourceUtil.createItemResource(resourceName,id,description,massPerItem, 1);
//        Part hammer = ItemResourceUtil.createItemResource(resourceName, id, description, massPerItem, 1);
//        int hammerID = hammer.getID();
//        
//		int oxygenID = ResourceUtil.oxygenID;
//		int foodID = ResourceUtil.foodID;
//		int waterID = ResourceUtil.waterID;
//		int methaneID = ResourceUtil.methaneID;
//		
//		inv.addAmountResourceTypeCapacity(oxygenID, 100D);
//		inv.storeAmountResource(oxygenID, 100D, true);
//		inv.addAmountResourceTypeCapacity(foodID, 100D);
//		inv.storeAmountResource(foodID, 100D, true);
//		inv.addAmountResourceTypeCapacity(waterID, waterAmount);
//		inv.storeAmountResource(waterID, waterAmount, true);
//		inv.addAmountResourceTypeCapacity(methaneID, 100D);
//		inv.storeAmountResource(methaneID, 100D, true);
//
//		inv.storeItemResources(hammerID, 5);
//
//		logger.info("inv's food : " + inv.getAmountResourceStored(foodID, false));
//		
//		Map<Integer, Number> resourcesMap = new HashMap<Integer, Number>();
//		resourcesMap.put(oxygenID, 100D);
//		resourcesMap.put(foodID, 100D);
//		resourcesMap.put(waterID, waterAmount);
//		resourcesMap.put(methaneID, 100D);
//		resourcesMap.put(hammerID, 5);
//
//		Map<Integer, Integer> equipmentMap = new HashMap<>();
//		equipmentMap.put(EquipmentType.convertType2ID(SpecimenContainer.TYPE), 5);
//
//		Vehicle vehicle = new MockVehicle(settlement);
////		Inventory vInv = vehicle.getInventory();
////		vInv.addAmountResourceTypeCapacity(oxygenID, 100D);
////		vInv.storeAmountResource(oxygenID, 100D, true);
////		vInv.addAmountResourceTypeCapacity(foodID, 100D);
////		vInv.storeAmountResource(foodID, 100D, true);
////		vInv.addAmountResourceTypeCapacity(waterID, waterAmount);
////		vInv.storeAmountResource(waterID, waterAmount, true);
////		vInv.addAmountResourceTypeCapacity(methaneID, 100D);
////		vInv.storeAmountResource(methaneID, 100D, true);
////		vInv.storeItemResources(hammerID, 5);
//		
//		assertFalse("Not enough equipment supplies at settlement for trip.",
//				LoadVehicleGarage.hasEnoughSupplies(settlement, vehicle, resourcesMap, equipmentMap, 2, 1D));
//	}

	/*
	 * Test method for 'org.mars_sim.msp.simulation.person.ai.task.LoadVehicle.isFullyLoaded()'
	 */
	public void testIsFullyLoadedGood() throws Exception {
		Settlement settlement = new MockSettlement();
		Vehicle vehicle = new MockVehicle(settlement);
//        ItemResource hammer = ItemResourceUtil.createItemResource(resourceName,id,description,massPerItem, 1);
        Part hammer = ItemResourceUtil.createItemResource(resourceName, id, description, massPerItem, 1);
		Inventory inv = vehicle.getInventory();
		inv.addGeneralCapacity(100D);

		int hammerID = hammer.getID();
		
		int oxygenID = ResourceUtil.oxygenID;
		int foodID = ResourceUtil.foodID;
		int waterID = ResourceUtil.waterID;
		int methaneID = ResourceUtil.methaneID;
		
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
			inv.storeUnit(new SpecimenBox(settlement.getCoordinates()));
		}

		Map<Integer, Number> requiredResourcesMap = new HashMap<Integer, Number>();
		requiredResourcesMap.put(oxygenID, 100D);
		requiredResourcesMap.put(foodID, 100D);
		requiredResourcesMap.put(waterID, waterAmount);
		requiredResourcesMap.put(methaneID, 100D);
		requiredResourcesMap.put(hammerID, Integer.valueOf(5));

		Map<Integer, Number> optionalResourcesMap = new HashMap<Integer, Number>(0);

		Map<Integer, Integer> requiredEquipmentMap = new HashMap<>();
		requiredEquipmentMap.put(EquipmentType.convertName2ID(SpecimenBox.TYPE), Integer.valueOf(5));

		Map<Integer, Integer> optionalEquipmentMap = new HashMap<>(0);

		assertTrue("Vehicle is fully loaded", LoadVehicleGarage.isFullyLoaded(requiredResourcesMap,
		        optionalResourcesMap, requiredEquipmentMap, optionalEquipmentMap, vehicle, settlement));
	}

	/*
	 * Test method for 'org.mars_sim.msp.simulation.person.ai.task.LoadVehicle.isFullyLoaded()'
	 */
	public void testIsFullyLoadedNoAmountResources() throws Exception {
//		Settlement settlement = new MockSettlement();
		Vehicle vehicle = new MockVehicle(settlement);
//        ItemResource hammer = ItemResourceUtil.createItemResource(resourceName,id,description,massPerItem, 1);
        Part hammer = ItemResourceUtil.createItemResource(resourceName, id, description, massPerItem, 1);
		int hammerID = hammer.getID();
		
		int oxygenID = ResourceUtil.oxygenID;
		int foodID = ResourceUtil.foodID;
		int waterID = ResourceUtil.waterID;
		int methaneID = ResourceUtil.methaneID;
		
        Inventory inv = vehicle.getInventory();
		inv.addGeneralCapacity(100D);

		inv.storeItemResources(hammerID, 5);

		for (int x = 0; x < 5; x++) {
			inv.storeUnit(new SpecimenBox(settlement.getCoordinates()));
		}

		Map<Integer, Number> requiredResourcesMap = new HashMap<Integer, Number>();
		requiredResourcesMap.put(oxygenID, 100D);
		requiredResourcesMap.put(foodID, 100D);
		requiredResourcesMap.put(waterID, waterAmount);
		requiredResourcesMap.put(methaneID, 100D);
		requiredResourcesMap.put(hammerID, 5);

		Map<Integer, Number> optionalResourcesMap = new HashMap<Integer, Number>(0);

		Map<Integer, Integer> requiredEquipmentMap = new HashMap<>();
		requiredEquipmentMap.put(EquipmentType.convertName2ID(SpecimenBox.TYPE), Integer.valueOf(5));

		Map<Integer, Integer> optionalEquipmentMap = new HashMap<>(0);

		assertFalse("Vehicle is not fully loaded", LoadVehicleGarage.isFullyLoaded(requiredResourcesMap,
		        optionalResourcesMap, requiredEquipmentMap, optionalEquipmentMap, vehicle, settlement));
	}

	/*
	 * Test method for 'org.mars_sim.msp.simulation.person.ai.task.LoadVehicle.isFullyLoaded()'
	 */
	public void testIsFullyLoadedNoItemResources() throws Exception {
//		Settlement settlement = new MockSettlement();
		Vehicle vehicle = new MockVehicle(settlement);
        Part hammer = ItemResourceUtil.createItemResource(resourceName, id, description, massPerItem, 1);
		Inventory inv = vehicle.getInventory();
		inv.addGeneralCapacity(100D);

		int hammerID = hammer.getID();
		
		int oxygenID = ResourceUtil.oxygenID;
		int foodID = ResourceUtil.foodID;
		int waterID = ResourceUtil.waterID;
		int methaneID = ResourceUtil.methaneID;
		
		inv.addAmountResourceTypeCapacity(oxygenID, 100D);
		inv.storeAmountResource(oxygenID, 100D, true);
		inv.addAmountResourceTypeCapacity(foodID, 100D);
		inv.storeAmountResource(foodID, 100D, true);
		inv.addAmountResourceTypeCapacity(waterID, waterAmount);
		inv.storeAmountResource(waterID, waterAmount, true);
		inv.addAmountResourceTypeCapacity(methaneID, 100D);
		inv.storeAmountResource(methaneID, 100D, true);

		for (int x = 0; x < 5; x++) {
			inv.storeUnit(new SpecimenBox(settlement.getCoordinates()));
		}

		Map<Integer, Number> requiredResourcesMap = new HashMap<Integer, Number>();
		requiredResourcesMap.put(oxygenID, 100D);
		requiredResourcesMap.put(foodID, 100D);
		requiredResourcesMap.put(waterID, waterAmount);
		requiredResourcesMap.put(methaneID, 100D);
		requiredResourcesMap.put(hammerID, Integer.valueOf(5));

		Map<Integer, Number> optionalResourcesMap = new HashMap<Integer, Number>(0);

		Map<Integer, Integer> requiredEquipmentMap = new HashMap<>();
		requiredEquipmentMap.put(EquipmentType.convertName2ID(SpecimenBox.TYPE), Integer.valueOf(5));

		Map<Integer, Integer> optionalEquipmentMap = new HashMap<>(0);

		assertFalse("Vehicle is not fully loaded", LoadVehicleGarage.isFullyLoaded(requiredResourcesMap,
                optionalResourcesMap, requiredEquipmentMap, optionalEquipmentMap, vehicle, settlement));
	}

	/*
	 * Test method for 'org.mars_sim.msp.simulation.person.ai.task.LoadVehicle.isFullyLoaded()'
	 */
	public void testIsFullyLoadedNoEquipment() throws Exception {
//		Settlement settlement = new MockSettlement();
		Vehicle vehicle = new MockVehicle(settlement);
//        ItemResource hammer = ItemResourceUtil.createBrandNewItemResource(resourceName,id,description,massPerItem, 1);
        Part hammer = ItemResourceUtil.createItemResource(resourceName, id, description, massPerItem, 1);
        Inventory inv = vehicle.getInventory();
		inv.addGeneralCapacity(100D);

		int hammerID = hammer.getID();
		
		int oxygenID = ResourceUtil.oxygenID;
		int foodID = ResourceUtil.foodID;
		int waterID = ResourceUtil.waterID;
		int methaneID = ResourceUtil.methaneID;
		
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
		requiredResourcesMap.put(oxygenID, 100D);
		requiredResourcesMap.put(foodID, 100D);
		requiredResourcesMap.put(waterID, waterAmount);
		requiredResourcesMap.put(methaneID, 100D);
		requiredResourcesMap.put(hammerID, Integer.valueOf(5));

		Map<Integer, Number> optionalResourcesMap = new HashMap<Integer, Number>(0);

		Map<Integer, Integer> requiredEquipmentMap = new HashMap<>();
		requiredEquipmentMap.put(EquipmentType.convertName2ID(SpecimenBox.TYPE), Integer.valueOf(5));

		Map<Integer, Integer> optionalEquipmentMap = new HashMap<>(0);

		assertFalse("Vehicle is not fully loaded", LoadVehicleGarage.isFullyLoaded(requiredResourcesMap,
                optionalResourcesMap, requiredEquipmentMap, optionalEquipmentMap, vehicle, settlement));
	}
}