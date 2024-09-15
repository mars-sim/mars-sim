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
		var requiredResourcesMap = new SuppliesManifest();
		requiredResourcesMap.addAmount(ResourceUtil.oxygenID, 20D, true);
		requiredResourcesMap.addAmount(ResourceUtil.methanolID, 10D, true);

		loadSettlement(settlement, requiredResourcesMap);

		LoadingController controller = vehicle.setLoading(requiredResourcesMap);
		int loadingCount = 0;
		while (loadingCount < 100) {
			controller.backgroundLoad(80);
			loadingCount++;
		}
		assertTrue("Multiple loadings", (loadingCount > 1));
		assertTrue("Loading controller complete", controller.isCompleted());
		checkVehicleResources(vehicle, requiredResourcesMap);
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
		var manifest = new SuppliesManifest();
		manifest.addEquipment(EquipmentType.getResourceID(EquipmentType.BARREL), 10, true);
		manifest.addEquipment(EquipmentType.getResourceID(EquipmentType.SPECIMEN_BOX), 5, true);

		manifest.addEquipment(EquipmentType.getResourceID(EquipmentType.GAS_CANISTER), 10, false);

		loadSettlement(settlement, manifest);

		// Add an extra resource that will not be present
		int missingId = EquipmentType.getResourceID(EquipmentType.LARGE_BAG);
		var expanded = new SuppliesManifest(manifest);
		expanded.addEquipment(missingId, 10, false);

		loadIt(100, expanded);

		// Check Equipment that was present in settlement
		checkVehicleInventory(vehicle, manifest);

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
		testLoading(100,manifest);
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
		manifest.addAmount(ResourceUtil.foodID, 30D, true);
		manifest.addAmount(ResourceUtil.waterID, 10D, true);

		// Load the manifest
		testLoading(200, manifest);
	}


	/*
	 * Test method for 'com.mars_sim.simulation.person.ai.task.LoadVehicle.LoadingPhase(double)'
	 */
	public void testLoadFailedAmountResources() {
		var requiredResourcesMap = new SuppliesManifest();
		// Add 2000kg food to the manifest
		requiredResourcesMap.addAmount(ResourceUtil.foodID, 50D, true);

		LoadingController controller = vehicle.setLoading(requiredResourcesMap);

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
		manifest.addAmount(ResourceUtil.foodID, 20D, true);
		manifest.addAmount(ResourceUtil.waterID, 10D, true);

		manifest.addAmount(ResourceUtil.co2ID, 4D, false);

		// Load the manifest
		testLoading(200, manifest);
	}

	/*
	 * Load with optional resource present
	 */
	public void testLoadMissingOptionalAmountResources() {
		var manifest = new SuppliesManifest();
		manifest.addAmount(ResourceUtil.foodID, 100D, true);

		manifest.addAmount(ResourceUtil.co2ID, 4D, false);

		testLoadOptionalResources(100, manifest,
								  ResourceUtil.nitrogenID);
	}

	/*
	 * Test method loading Equipment
	 */
	public void testLoadFull() {
		var manifest = new SuppliesManifest();
		manifest.addEquipment(EquipmentType.getResourceID(EquipmentType.BARREL), 5, true);
		manifest.addEquipment(EquipmentType.getResourceID(EquipmentType.SPECIMEN_BOX), 5, true);

		manifest.addEquipment(EquipmentType.getResourceID(EquipmentType.GAS_CANISTER), 5, false);

		manifest.addAmount(ResourceUtil.foodID, 100D, true);
		manifest.addItem(fireExtinguisherID, 1, true);
		manifest.addItem(smallHammerID, 2, true);

		manifest.addAmount(ResourceUtil.co2ID, 4D, false);
		manifest.addItem(pipeWrenchID, 5, false);

		// Load the manifest
		testLoading(200, manifest);
	}

	/*
	 * Executes a loading for a manifest and a batch of tests.
	 */
	private void testLoading(int maxCycles, SuppliesManifest manifest) {

		// Add resources to Settlement
		loadSettlement(settlement, manifest);

		// Make sure Vehicle has capacity
		setVehicleCapacity(vehicle, manifest);

		// Load the manifest
		loadIt(maxCycles, manifest);
		checkVehicleInventory(vehicle, manifest);

		// Reload the same manifest which should complete immediately
		reload(manifest);
	}

	private void checkVehicleInventory(Vehicle v, SuppliesManifest manifest) {
		checkVehicleEquipment(v, manifest);
		checkVehicleResources(v, manifest);
	}

	private void setVehicleCapacity(Vehicle v, SuppliesManifest manifest) {
		setResourcesCapacity(v, manifest.getAmounts(true));
		setResourcesCapacity(v, manifest.getAmounts(false));
	}

	/**
	 * Load some Equipment into a Settlement
	 * @param settlement
	 * @param manifest
	 */
	private static void loadSettlementEquipment(Settlement settlement, Map<Integer, Integer> manifest) {
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
	 * @param manifest
	 * @param missingId Missing resource that should not be loaded
	 */
	private void testLoadOptionalResources(int maxCycles,
			SuppliesManifest manifest, int missingId) {

		loadSettlement(settlement, manifest);
		setVehicleCapacity(vehicle, manifest);

		// Add an extra resource that will not be present
		var extraOptionalResources = new SuppliesManifest(manifest);
		extraOptionalResources.addAmount(missingId, 10D, false);

		loadIt(maxCycles, manifest);

		checkVehicleInventory(vehicle, manifest);

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
	 * @param manifest
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
	private LoadingController loadIt(int maxCycles, SuppliesManifest manifest) {


		LoadingController controller = vehicle.setLoading(manifest);
		assertTrue("Vehicle has of LOADING", vehicle.haveStatusType(StatusType.LOADING));
		assertEquals("Vehicle of the controller", vehicle, controller.getVehicle());

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
	 * @param source
	 * @param equipmentManifest
	 */
	private void checkVehicleEquipment(Vehicle source, SuppliesManifest manifest) {
		var eqm = new HashMap<>(manifest.getEquipment(true));
		eqm.putAll(manifest.getEquipment(false));

		for(Entry<Integer, Integer> item : eqm.entrySet()) {
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
	 * @param manifest
	 */
	private void checkVehicleResources(Vehicle source, SuppliesManifest manifest) {

		var requiredResourcesMap = new HashMap<>(manifest.getAmounts(true));
		requiredResourcesMap.putAll(manifest.getAmounts(false));
		for (Entry<Integer, Double> resource : requiredResourcesMap.entrySet()) {
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
	private void setResourcesCapacity(Vehicle target, Map<Integer, Double> requiredResourcesMap) {
		EquipmentInventory inv = target.getEquipmentInventory();
		
		for (Entry<Integer, Double> v : requiredResourcesMap.entrySet()) {
			inv.setResourceCapacity(v.getKey(), v.getValue().doubleValue() * 1.01D);
		}
	}

	/**
	 * Load up a Settlements Amount Resources from a manifest
	 * @param target
	 * @param requiredResourcesMap
	 */
	public static void loadSettlement(Settlement target, SuppliesManifest requiredResourcesMap) {
		loadSettlementAmounts(target, requiredResourcesMap.getAmounts(true));
		loadSettlementAmounts(target, requiredResourcesMap.getAmounts(false));
		loadSettlementEquipment(target, requiredResourcesMap.getEquipment(true));
		loadSettlementEquipment(target, requiredResourcesMap.getEquipment(false));
		loadSettlementItems(target, requiredResourcesMap.getItems(true));
		loadSettlementItems(target, requiredResourcesMap.getItems(false));
	}

	private static void loadSettlementAmounts(Settlement target, Map<Integer, Double> resourcesMap) {
		for (Entry<Integer, Double> resource : resourcesMap.entrySet()) {
			// Add extra to the stored to give a tolerance
			double amount = resource.getValue().doubleValue() + EXTRA_RESOURCE;
			target.storeAmountResource(resource.getKey(), amount);
		}
	}

	private static void loadSettlementItems(Settlement target, Map<Integer, Integer> resourcesMap) {
		for (Entry<Integer, Integer> resource : resourcesMap.entrySet()) {
			target.storeItemResource(resource.getKey(), resource.getValue().intValue());
		}
	}
}