/*
 * Mars Simulation Project
 * LoadControllerTest
 * @date 2023-04-16
 * @author Scott Davis
 */

package com.mars_sim.core.vehicle.task;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentInventory;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.person.GenderType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.resource.SuppliesManifest;
import com.mars_sim.core.structure.MockSettlement;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;

import junit.framework.TestCase;

/**
 * Tests the loading controller operation for vehicles.
 */
public class LoadControllerTest
extends TestCase {

	// Extra amount to add to resource to handle double arithmetic mismatch
	private static final double EXTRA_RESOURCE = 0.01D;
	private static final String SMALL_HAMMER = "small hammer";
	private static final String FIRE_EXTINGUSHER = "fire extinguisher";
	private static final String PIPE_WRENCH = "pipe wrench";


	private Settlement settlement = null;
	private UnitManager unitManager;
	private Vehicle vehicle;
	private Person person;
	private Integer pipeWrenchID;
	private Integer smallHammerID;
	private Integer fireExtinguisherID;

	@Override
    public void setUp() {

        SimulationConfig config = SimulationConfig.instance();
		config.loadConfig();
        Simulation.instance().testRun();

        unitManager = Simulation.instance().getUnitManager();

		// Create test settlement.
		settlement = new MockSettlement();
		
		unitManager.addUnit(settlement);

		vehicle = new Rover("Test Cargo Rover",
							config.getVehicleConfiguration().getVehicleSpec("cargo rover"), settlement);
		
		// Call addOwnedVehicle prior to addUnit
		settlement.addOwnedVehicle(vehicle);
		
		unitManager.addUnit(vehicle);

		person = Person.create("Jim Loader", settlement, GenderType.MALE).build();
		
		settlement.addACitizen(person);

		// Make the person strong to get loading quicker
		person.getNaturalAttributeManager().setAttribute(NaturalAttributeType.STRENGTH, 40);

		smallHammerID = ItemResourceUtil.findIDbyItemResourceName(SMALL_HAMMER);
		fireExtinguisherID = ItemResourceUtil.findIDbyItemResourceName(FIRE_EXTINGUSHER);
		pipeWrenchID = ItemResourceUtil.findIDbyItemResourceName(PIPE_WRENCH);
    }

	/*
	 * Test method for 'com.mars_sim.simulation.person.ai.task.LoadVehicle.LoadingPhase(double)'
	 */
	public void testBackgroundLoading() {
		var requiredResources = new SuppliesManifest();
		requiredResources.addResource(ResourceUtil.oxygenID, 20D, true);
		requiredResources.addResource(ResourceUtil.methanolID, 10D, true);

		loadSettlementResources(settlement, requiredResources.getResources(true));

		LoadingController controller = vehicle.setLoading(requiredResources);
		int loadingCount = 0;
		while (loadingCount < 100) {
			controller.backgroundLoad(80);
			loadingCount++;
		}
		assertTrue("Multiple loadings", (loadingCount > 1));
		assertTrue("Loading controller complete", controller.isCompleted());
		checkVehicleResources(vehicle, requiredResources.getResources(true));
	}

	/*
	 * Test method loading Equipment
	 */
	public void testLoadRequiredEquipment() {
		var manifest = new SuppliesManifest();
		manifest.addEquipment(EquipmentType.getResourceID(EquipmentType.BARREL), 10, true);
		manifest.addEquipment(EquipmentType.getResourceID(EquipmentType.SPECIMEN_BOX), 5, true);

		// Load the manifest
		testLoading(100, manifest);
	}

	/*
	 * Test method loading Equipment
	 */
	public void testLoadOptionalEquipment() {
		var manifest = new SuppliesManifest();
		manifest.addEquipment(EquipmentType.getResourceID(EquipmentType.BARREL), 10, true);
		manifest.addEquipment(EquipmentType.getResourceID(EquipmentType.SPECIMEN_BOX), 5, true);
		manifest.addEquipment(EquipmentType.getResourceID(EquipmentType.GAS_CANISTER), 10, false);

		// Load the manifest
		testLoading(100, manifest);
	}

	/*
	 * Test method loading Equipment
	 */
	public void testLoadMissingOptionalEquipment() {
		Map<Integer, Integer> requiredEquipMap = new HashMap<>();
		requiredEquipMap.put(EquipmentType.getResourceID(EquipmentType.BARREL), 10);
		requiredEquipMap.put(EquipmentType.getResourceID(EquipmentType.SPECIMEN_BOX), 5);

		Map<Integer, Integer> optionalEquipMap = new HashMap<>();
		optionalEquipMap.put(EquipmentType.getResourceID(EquipmentType.GAS_CANISTER), 10);

		loadSettlementEquipment(settlement, requiredEquipMap);
		loadSettlementEquipment(settlement, optionalEquipMap);

		// Add an extra resource that will not be present
		var manifest = new SuppliesManifest();
		var missingId = EquipmentType.getResourceID(EquipmentType.LARGE_BAG);
		manifest.addEquipment(missingId, 10, false);
		requiredEquipMap.entrySet().forEach(e -> manifest.addEquipment(e.getKey(), e.getValue(), true));
		optionalEquipMap.entrySet().forEach(e -> manifest.addEquipment(e.getKey(), e.getValue(), false));
		loadIt(100, manifest);

		// Check Equipment that was present in settlement
		checkVehicleEquipment(vehicle, requiredEquipMap);
		checkVehicleEquipment(vehicle, optionalEquipMap);

		EquipmentType eType = EquipmentType.convertID2Type(missingId);
		long optionalLoaded = vehicle.getEquipmentSet().stream()
				.filter(e -> (e.getEquipmentType() == eType))
				.count();
		assertEquals("Optional Equipment loaded", 0, optionalLoaded);
	}



	/*
	 * Test method loading Resource Items
	 */
	public void testLoadRequiredItemResources() {
		var manifest = new SuppliesManifest();
		manifest.addItem(fireExtinguisherID, 1, true);
		manifest.addItem(smallHammerID, 2, true);

		// Load the manifest
		testLoading(100, manifest);
	}

	/*
	 * Test method loading Resource Items
	 */
	public void testLoadOptionalItemResources() {
		var manifest = new SuppliesManifest();
		manifest.addItem(fireExtinguisherID, 1, true);
		manifest.addItem(smallHammerID, 2, true);
		manifest.addItem(pipeWrenchID, 10, false);

		// Load the manifest
		testLoading(100, manifest);
	}

	/*
	 * Load with optional resource present
	 */
	public void testLoadMissingOptionalItemResources() {
		var manifest = new SuppliesManifest();
		manifest.addItem(fireExtinguisherID, 1, true);
		manifest.addItem(smallHammerID, 2, true);
		manifest.addItem(pipeWrenchID, 10, false);

		testLoadOptionalResources(100, manifest,
								  ItemResourceUtil.printerID);
	}

	/*
	 * Test method for 'com.mars_sim.simulation.person.ai.task.LoadVehicle.LoadingPhase(double)'
	 */
	public void testLoadRequiredAmountResources() {
		var manifest = new SuppliesManifest();
		manifest.addResource(ResourceUtil.foodID, 30D, true);
		manifest.addResource(ResourceUtil.waterID, 10D, true);

		// Load the manifest
		testLoading(200, manifest);
	}


	/*
	 * Test method for 'com.mars_sim.simulation.person.ai.task.LoadVehicle.LoadingPhase(double)'
	 */
	public void testLoadFailedAmountResources() {
		var requiredResources = new SuppliesManifest();
		// Add 2000kg food to the manifest
		requiredResources.addResource(ResourceUtil.foodID, 50D, true);

		LoadingController controller = vehicle.setLoading(requiredResources);

		// Run the loader but do not load an resources into the settlement
		for(int i = 0 ; i < (LoadingController.MAX_SETTLEMENT_ATTEMPTS - 1); i++) {
			controller.load(person, 1D);
			assertFalse("Load completed #" + i, controller.isCompleted());
			assertFalse("Load failed #" + i, controller.isFailure());
		}

		// Do the last load and it should fail
		controller.load(person, 1D);

		// Vehicle is not loaded and failed
		assertFalse("Vehicle loaded", controller.isCompleted());
		assertTrue("Vehicle load did not failed", controller.isFailure());
	}

	/*
	 * Test method for 'com.mars_sim.simulation.person.ai.task.LoadVehicle.LoadingPhase(double)'
	 */
	public void testLoadOptionalAmountResources() {
		var manifest = new SuppliesManifest();
		manifest.addResource(ResourceUtil.foodID, 20D, true);
		manifest.addResource(ResourceUtil.waterID, 10D, true);
		manifest.addResource(ResourceUtil.co2ID, 4D, false);

		// Load the manifest
		testLoading(200, manifest);
	}

	/*
	 * Load with optional resource present
	 */
	public void testLoadMissingOptionalAmountResources() {
		var manifest = new SuppliesManifest();
		manifest.addResource(ResourceUtil.foodID, 100D, true);
		manifest.addResource(ResourceUtil.co2ID, 4D, false);

		testLoadOptionalResources(100, manifest,
								  ResourceUtil.oxygenID);
	}

	/*
	 * Test method loading Equipment
	 */
	public void testLoadFull() {
		var manifest = new SuppliesManifest();
		manifest.addEquipment(EquipmentType.getResourceID(EquipmentType.BARREL), 5, true);
		manifest.addEquipment(EquipmentType.getResourceID(EquipmentType.SPECIMEN_BOX), 5, true);
		manifest.addEquipment(EquipmentType.getResourceID(EquipmentType.GAS_CANISTER), 5, false);

		manifest.addResource(ResourceUtil.foodID, 100D, true);
		manifest.addResource(fireExtinguisherID, 1, true);
		manifest.addResource(smallHammerID, 2, true);
		manifest.addResource(ResourceUtil.co2ID, 4D, false);
		manifest.addResource(pipeWrenchID, 5D, false);

		// Load the manifest
		testLoading(200, manifest);
	}

	/*
	 * Executes a loading for a manifest and a batch of tests.
	 */
	private void testLoading(int maxCycles, SuppliesManifest manifest) {

		var resourcesManifest = manifest.getResources(true);
		var optionalResourcesManifest = manifest.getResources(false);
		var equipmentManifest = manifest.getEquipment(true);
		var optionalEquipmentManifest = manifest.getEquipment(false);

		// Add resources to Settlement
		loadSettlementResources(settlement, resourcesManifest);
		loadSettlementResources(settlement, optionalResourcesManifest);
		loadSettlementEquipment(settlement, equipmentManifest);
		loadSettlementEquipment(settlement, optionalEquipmentManifest);

		// Make sure Vehicle has capacity
		setResourcesCapacity(vehicle, resourcesManifest);
		setResourcesCapacity(vehicle, optionalResourcesManifest);

		// Load the manifest
		loadIt(maxCycles, manifest);
		checkVehicleResources(vehicle, resourcesManifest);
		checkVehicleEquipment(vehicle, equipmentManifest);

		checkVehicleResources(vehicle, optionalResourcesManifest);
		checkVehicleEquipment(vehicle, optionalEquipmentManifest);

		// Reload the same manifest which should complete immediately
		reload(manifest);
	}

	/**
	 * Load some Equipment into a Settlement
	 * @param settlement
	 * @param manifest
	 */
	private void loadSettlementEquipment(Settlement settlement, Map<Integer, Integer> manifest) {
		for(Entry<Integer, Integer> item : manifest.entrySet()) {
			EquipmentType type = EquipmentType.convertID2Type(item.getKey());
			for(int i = 0; i < item.getValue(); i++) {
				EquipmentFactory.createEquipment(type, settlement);
			}
		}
	}

	/**
	 * Test loading optional resources where one is missiing
	 * @param maxCycles
	 * @param manifest Manifest holding mandatory and optional
	 * @param missingId Missing resource that should not be loaded
	 */
	private void testLoadOptionalResources(int maxCycles,
							SuppliesManifest manifest, int missingId) {

		var requiredResources = manifest.getResources(true);
		var optionalResources = manifest.getResources(false);
	
		loadSettlementResources(settlement, requiredResources);
		setResourcesCapacity(vehicle, requiredResources);

		loadSettlementResources(settlement, optionalResources);
		setResourcesCapacity(vehicle, optionalResources);

		// Add an extra resource that will not be present
		var expandedManifest = new SuppliesManifest();
		expandedManifest.addResource(missingId, 10D, false);
		requiredResources.entrySet().forEach(e -> expandedManifest.addResource(e.getKey(), e.getValue().doubleValue(), true));
		optionalResources.entrySet().forEach(e -> expandedManifest.addResource(e.getKey(), e.getValue().doubleValue(), false));

		loadIt(maxCycles, expandedManifest);

		checkVehicleResources(vehicle, requiredResources);
		checkVehicleResources(vehicle, optionalResources);

		double optionalLoaded;
		if (missingId < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
			optionalLoaded = vehicle.getAmountResourceStored(missingId);
		}
		else {
			optionalLoaded = vehicle.getItemResourceStored(missingId);
		}

		assertEquals("Optional resource loaded", 0D, optionalLoaded);
	}


	/**
	 * Run a reload controller to load this manifest. This expects teh manifest
	 * to be already satisified by the Vehicle
	 * @param resourcesManifest
	 * @param optionalResourcesManifest
	 * @param equipmentManifest
	 * @param optionalEquipmentManifest
	 * @return
	 */
	private LoadingController reload(SuppliesManifest manifest) {

		LoadingController controller = vehicle.setLoading(manifest);

		// Vehicle should already be loaded
		assertTrue("Vehicle already loaded", controller.isCompleted());
		assertTrue("Reload completes on first attempt", controller.load(person, 1));

		return controller;
	}

	/**
	 * Run a loading controller for a manifest.
	 * @param resourcesManifest
	 * @param optionalResourcesManifest
	 * @param equipmentManifest
	 * @param optionalEquipmentManifest
	 * @return
	 */
	private LoadingController loadIt(int maxCycles,SuppliesManifest manifest) {

		LoadingController controller = vehicle.setLoading(manifest);
		assertTrue("Vehicle has of LOADING", vehicle.haveStatusType(StatusType.LOADING));


		int loadingCount = 0;
		boolean loaded = false;
		while (!loaded && (loadingCount < maxCycles)) {
			loaded  = controller.load(person, 1);
			loadingCount++;
		}
		assertTrue("Multiple loadings", (loadingCount > 1));
		assertTrue("Load operation stopped on load complete", loaded);
		assertFalse("Loading controller successful", controller.isFailure());
		assertTrue("Loading controller complete", controller.isCompleted());
		assertFalse("Vehicle clear of LOADING", vehicle.haveStatusType(StatusType.LOADING));

		return controller;
	}

	/**
	 * Check if a vehicle has the required Equipment
	 * @param vehicle2
	 * @param equipmentManifest
	 */
	private void checkVehicleEquipment(Vehicle source, Map<Integer, Integer> manifest) {
		for(Entry<Integer, Integer> item : manifest.entrySet()) {
			EquipmentType eType = EquipmentType.convertID2Type(item.getKey());
			long stored = source.getEquipmentSet().stream()
					.filter(e -> (e.getEquipmentType() == eType))
					.count();
			assertEquals("Equipment in vehicle " + eType.name(),
					item.getValue().intValue(), stored);
		}
	}


	/**
	 * Check if the Vehicle has the Resources defined by a manifest
	 * @param source
	 * @param requiredResources
	 */
	private void checkVehicleResources(Vehicle source, Map<Integer, Number> requiredResources) {

		for (Entry<Integer, Number> resource : requiredResources.entrySet()) {
			int key = resource.getKey();
			if (key < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
				String resourceName = ResourceUtil.findAmountResourceName(key);

				double stored = source.getAmountResourceStored(key);
				double expected = resource.getValue().doubleValue();
				assertLessThan("Vehicle amount resource stored " + resourceName, expected, stored);
			}
			else {
				int stored = source.getItemResourceStored(key);
				int expected = resource.getValue().intValue();
				String itemName = ItemResourceUtil.findItemResourceName(key);
				assertEquals("Vehicle item resource stored " + itemName, expected, stored);
			}
		}
	}

	private static void assertLessThan(String message, double expected, double stored) {
		boolean test = expected <= stored;
		if (!test) {
			assertTrue(message + ":" + expected + " <= " + stored, test);
		}
	}

	/**
	 * Set the capacity of the Vehicle to support the manifest
	 * @param target
	 * @param requiredResourcesMap
	 */
	private void setResourcesCapacity(Vehicle target, Map<Integer, Number> requiredResourcesMap) {
		EquipmentInventory inv = target.getEquipmentInventory();
		
		for (Entry<Integer, Number> v : requiredResourcesMap.entrySet()) {
			int key = v.getKey();
			if (key < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
				inv.setResourceCapacity(key, v.getValue().doubleValue() * 1.01D);
			}
		}
	}

	/**
	 * Load up a Settlements Amount Resources from a manifest
	 * @param target
	 * @param optionalResourcesManifest
	 */
	private void loadSettlementResources(Settlement target, Map<Integer, Number> optionalResourcesManifest) {

		for (Entry<Integer, Number> resource : optionalResourcesManifest.entrySet()) {
			int key = resource.getKey();
			if (key < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
				// Add extra to the stored to give a tolerance
				double amount = resource.getValue().doubleValue() + EXTRA_RESOURCE;
				target.storeAmountResource(key, amount);
			}
			else {
				target.storeItemResource(key, resource.getValue().intValue());
			}
		}
	}
}