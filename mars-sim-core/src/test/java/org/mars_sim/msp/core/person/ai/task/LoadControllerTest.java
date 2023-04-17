/*
 * Mars Simulation Project
 * LoadControllerTest
 * @date 2023-04-16
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.equipment.EquipmentInventory;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.MockSettlement;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

import junit.framework.TestCase;

/**
 * Tests the loading controller operation for a vehicle
 */
public class LoadControllerTest
extends TestCase {

	// Extra amount to add to resouRce to handle double arithmetic mismatch
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
    public void setUp() throws Exception {

        SimulationConfig config = SimulationConfig.instance();
		config.loadConfig();
        Simulation.instance().testRun();

        unitManager = Simulation.instance().getUnitManager();

		// Create test settlement.
		settlement = new MockSettlement();
		unitManager.addUnit(settlement);

		vehicle = new Rover("Test Cargo Rover",
							config.getVehicleConfiguration().getVehicleSpec("cargo rover"), settlement);
		settlement.addOwnedVehicle(vehicle);
		unitManager.addUnit(vehicle);

		person = new Person("Jim Loader", settlement);
		settlement.addACitizen(person);
		// Set the container unit
		person.setContainerUnit(settlement);

		// Make the person strong to get loading quicker
		person.getNaturalAttributeManager().setAttribute(NaturalAttributeType.STRENGTH, 40);

		smallHammerID = ItemResourceUtil.findIDbyItemResourceName(SMALL_HAMMER);
		fireExtinguisherID = ItemResourceUtil.findIDbyItemResourceName(FIRE_EXTINGUSHER);
		pipeWrenchID = ItemResourceUtil.findIDbyItemResourceName(PIPE_WRENCH);
    }

	/*
	 * Test method for 'org.mars_sim.msp.simulation.person.ai.task.LoadVehicle.LoadingPhase(double)'
	 */
	public void testBackgroundLoading() throws Exception {
		Map<Integer, Number> requiredResourcesMap = new HashMap<>();
		requiredResourcesMap.put(ResourceUtil.oxygenID, 20D);
		requiredResourcesMap.put(ResourceUtil.methanolID, 10D);

		loadSettlementResources(settlement, requiredResourcesMap);

		LoadingController controller = new LoadingController(settlement, vehicle,
															 requiredResourcesMap,
															 Collections.emptyMap(),
															 Collections.emptyMap(),
															 Collections.emptyMap());
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
	public void testLoadRequiredEquipment() throws Exception {
		Map<Integer, Integer> requiredEquipMap = new HashMap<>();
		requiredEquipMap.put(EquipmentType.getResourceID(EquipmentType.BARREL), 10);
		requiredEquipMap.put(EquipmentType.getResourceID(EquipmentType.SPECIMEN_BOX), 5);


		// Load the manifest
		testLoading(100, Collections.emptyMap(), Collections.emptyMap(),
				requiredEquipMap, Collections.emptyMap());
	}

	/*
	 * Test method loading Equipment
	 */
	public void testLoadOptionalEquipment() throws Exception {
		Map<Integer, Integer> requiredEquipMap = new HashMap<>();
		requiredEquipMap.put(EquipmentType.getResourceID(EquipmentType.BARREL), 10);
		requiredEquipMap.put(EquipmentType.getResourceID(EquipmentType.SPECIMEN_BOX), 5);

		Map<Integer, Integer> optionalEquipMap = new HashMap<>();
		optionalEquipMap.put(EquipmentType.getResourceID(EquipmentType.GAS_CANISTER), 10);

		// Load the manifest
		testLoading(100, Collections.emptyMap(), Collections.emptyMap(),
				requiredEquipMap, optionalEquipMap);
	}

	/*
	 * Test method loading Equipment
	 */
	public void testLoadMissingOptionalEquipment() throws Exception {
		Map<Integer, Integer> requiredEquipMap = new HashMap<>();
		requiredEquipMap.put(EquipmentType.getResourceID(EquipmentType.BARREL), 10);
		requiredEquipMap.put(EquipmentType.getResourceID(EquipmentType.SPECIMEN_BOX), 5);

		Map<Integer, Integer> optionalEquipMap = new HashMap<>();
		optionalEquipMap.put(EquipmentType.getResourceID(EquipmentType.GAS_CANISTER), 10);

		loadSettlementEquipment(settlement, requiredEquipMap);
		loadSettlementEquipment(settlement, optionalEquipMap);

		// Add an extra resource that will not be present
		int missingId = EquipmentType.getResourceID(EquipmentType.LARGE_BAG);
		var extraOptionalEquipment = new HashMap<>(optionalEquipMap);
		extraOptionalEquipment.put(missingId, 10);

		loadIt(100, Collections.emptyMap(), Collections.emptyMap(),
				requiredEquipMap, extraOptionalEquipment);

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
	public void testLoadRequiredItemResources() throws Exception {
		Map<Integer, Number> requiredResourcesMap = new HashMap<>();
		requiredResourcesMap.put(fireExtinguisherID, 1);
		requiredResourcesMap.put(smallHammerID, 2);

		// Load the manifest
		testLoading(100, requiredResourcesMap, Collections.emptyMap(),
					   Collections.emptyMap(), Collections.emptyMap());
	}

	/*
	 * Test method loading Resource Items
	 */
	public void testLoadOptionalItemResources() throws Exception {
		Map<Integer, Number> requiredResourcesMap = new HashMap<>();
		requiredResourcesMap.put(fireExtinguisherID, 1);
		requiredResourcesMap.put(smallHammerID, 2);

		Map<Integer, Number> optionalResourcesMap = new HashMap<>();
		optionalResourcesMap.put(pipeWrenchID, 10D);

		// Load the manifest
		testLoading(100, requiredResourcesMap, optionalResourcesMap,
					   Collections.emptyMap(), Collections.emptyMap());
	}

	/*
	 * Load with optional resource present
	 */
	public void testLoadMissingOptionalItemResources() throws Exception {
		Map<Integer, Number> requiredResourcesMap = new HashMap<>();
		requiredResourcesMap.put(fireExtinguisherID, 1);
		requiredResourcesMap.put(smallHammerID, 2);

		Map<Integer, Number> optionalResourcesMap = new HashMap<>();
		optionalResourcesMap.put(pipeWrenchID, 10D);

		testLoadOptionalResources(100, requiredResourcesMap,
								  optionalResourcesMap,
								  ItemResourceUtil.printerID);
	}

	/*
	 * Test method for 'org.mars_sim.msp.simulation.person.ai.task.LoadVehicle.LoadingPhase(double)'
	 */
	public void testLoadRequiredAmountResources() throws Exception {
		Map<Integer, Number> requiredResourcesMap = new HashMap<>();
		requiredResourcesMap.put(ResourceUtil.foodID, 30D);
		requiredResourcesMap.put(ResourceUtil.waterID, 10D);

		// Load the manifest
		testLoading(200, requiredResourcesMap, Collections.emptyMap(),
					   Collections.emptyMap(), Collections.emptyMap());
	}


	/*
	 * Test method for 'org.mars_sim.msp.simulation.person.ai.task.LoadVehicle.LoadingPhase(double)'
	 */
	public void testLoadFailedAmountResources() throws Exception {
		Map<Integer, Number> requiredResourcesMap = new HashMap<>();
		// Add 2000kg food to the manifest
		requiredResourcesMap.put(ResourceUtil.foodID, 50D);

		LoadingController controller = new LoadingController(settlement, vehicle,
				requiredResourcesMap, Collections.emptyMap(),
				Collections.emptyMap(), Collections.emptyMap());

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
	 * Test method for 'org.mars_sim.msp.simulation.person.ai.task.LoadVehicle.LoadingPhase(double)'
	 */
	public void testLoadOptionalAmountResources() throws Exception {
		Map<Integer, Number> requiredResourcesMap = new HashMap<>();
		requiredResourcesMap.put(ResourceUtil.foodID, 20D);
		requiredResourcesMap.put(ResourceUtil.waterID, 10D);

		Map<Integer, Number> optionalResourcesMap = new HashMap<>();
		optionalResourcesMap.put(ResourceUtil.co2ID, 4D);

		// Load the manifest
		testLoading(200, requiredResourcesMap, optionalResourcesMap,
					   Collections.emptyMap(), Collections.emptyMap());
	}

	/*
	 * Load with optional resource present
	 */
	public void testLoadMissingOptionalAmountResources() throws Exception {
		Map<Integer, Number> requiredResourcesMap = new HashMap<>();
		requiredResourcesMap.put(ResourceUtil.foodID, 100D);

		Map<Integer, Number> optionalResourcesMap = new HashMap<>();
		optionalResourcesMap.put(ResourceUtil.co2ID, 4D);

		testLoadOptionalResources(100, requiredResourcesMap,
								  optionalResourcesMap,
								  ResourceUtil.nitrogenID);
	}

	/*
	 * Test method loading Equipment
	 */
	public void testLoadFull() throws Exception {
		Map<Integer, Integer> requiredEquipMap = new HashMap<>();
		requiredEquipMap.put(EquipmentType.getResourceID(EquipmentType.BARREL), 5);
		requiredEquipMap.put(EquipmentType.getResourceID(EquipmentType.SPECIMEN_BOX), 5);

		Map<Integer, Integer> optionalEquipMap = new HashMap<>();
		optionalEquipMap.put(EquipmentType.getResourceID(EquipmentType.GAS_CANISTER), 5);

		Map<Integer, Number> requiredResourcesMap = new HashMap<>();
		requiredResourcesMap.put(ResourceUtil.foodID, 100D);
		requiredResourcesMap.put(fireExtinguisherID, 1);
		requiredResourcesMap.put(smallHammerID, 2);

		Map<Integer, Number> optionalResourcesMap = new HashMap<>();
		optionalResourcesMap.put(ResourceUtil.co2ID, 4D);
		optionalResourcesMap.put(pipeWrenchID, 5D);

		// Load the manifest
		testLoading(200, requiredResourcesMap, optionalResourcesMap,
				requiredEquipMap, optionalEquipMap);
	}

	/*
	 * Executes a loading for a manifest and a batch of tests.
	 */
	private void testLoading(int maxCycles,
			Map<Integer, Number> resourcesManifest,
			Map<Integer, Number> optionalResourcesManifest,
			Map<Integer, Integer> equipmentManifest,
			Map<Integer, Integer> optionalEquipmentManifest) {

		// Add resources to Settlement
		loadSettlementResources(settlement, resourcesManifest);
		loadSettlementResources(settlement, optionalResourcesManifest);
		loadSettlementEquipment(settlement, equipmentManifest);
		loadSettlementEquipment(settlement, optionalEquipmentManifest);

		// Make sure Vehicle has capacity
		setResourcesCapacity(vehicle, resourcesManifest);
		setResourcesCapacity(vehicle, optionalResourcesManifest);

		// Load the manifest
		loadIt(maxCycles, resourcesManifest, optionalResourcesManifest,
			   equipmentManifest, optionalEquipmentManifest);
		checkVehicleResources(vehicle, resourcesManifest);
		checkVehicleEquipment(vehicle, equipmentManifest);

		checkVehicleResources(vehicle, optionalResourcesManifest);
		checkVehicleEquipment(vehicle, optionalEquipmentManifest);

		// Reload the same manifest which should complete immediately
		reload(resourcesManifest, optionalResourcesManifest,
				   equipmentManifest, optionalEquipmentManifest);
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
	 * @param requiredResources Required resources to load
	 * @param optionalResources Optional resources
	 * @param missingId Missing resource that should not be loaded
	 */
	private void testLoadOptionalResources(int maxCycles,
			Map<Integer, Number> requiredResources,
			Map<Integer, Number> optionalResources, int missingId) {

		loadSettlementResources(settlement, requiredResources);
		setResourcesCapacity(vehicle, requiredResources);

		loadSettlementResources(settlement, optionalResources);
		setResourcesCapacity(vehicle, optionalResources);

		// Add an extra resource that will not be present
		var extraOptionalResources = new HashMap<>(optionalResources);
		extraOptionalResources.put(missingId, 10D);

		loadIt(maxCycles, requiredResources, extraOptionalResources,
			 Collections.emptyMap(),
			 Collections.emptyMap());

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
	private LoadingController reload(Map<Integer, Number> resourcesManifest,
			Map<Integer, Number> optionalResourcesManifest,
			Map<Integer, Integer> equipmentManifest,
			Map<Integer, Integer> optionalEquipmentManifest) {

		LoadingController controller = new LoadingController(settlement, vehicle,
						resourcesManifest,
						optionalResourcesManifest,
						equipmentManifest,
						optionalEquipmentManifest);

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
	private LoadingController loadIt(int maxCycles,
				Map<Integer, Number> resourcesManifest,
				Map<Integer, Number> optionalResourcesManifest,
				Map<Integer, Integer> equipmentManifest,
				Map<Integer, Integer> optionalEquipmentManifest) {

		LoadingController controller = new LoadingController(settlement, vehicle,
				resourcesManifest,
				optionalResourcesManifest,
				equipmentManifest,
				optionalEquipmentManifest);

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
	 * @param requiredResourcesMap
	 */
	private void checkVehicleResources(Vehicle source, Map<Integer, Number> requiredResourcesMap) {

		for (Entry<Integer, Number> resource : requiredResourcesMap.entrySet()) {
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
	 * @param requiredResourcesMap
	 */
	private void loadSettlementResources(Settlement target, Map<Integer, Number> requiredResourcesMap) {

		for (Entry<Integer, Number> resource : requiredResourcesMap.entrySet()) {
			int key = resource.getKey();
			if (key < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
				// Add extra to the stored to give a tolerance
				double amount = resource.getValue().doubleValue() + EXTRA_RESOURCE;
				// Add extra to the capacity
//				target.addAmountResourceTypeCapacity(key, amount + EXTRA_RESOURCE);
				target.storeAmountResource(key, amount);
			}
			else {
				target.storeItemResource(key, resource.getValue().intValue());
			}
		}
	}
}