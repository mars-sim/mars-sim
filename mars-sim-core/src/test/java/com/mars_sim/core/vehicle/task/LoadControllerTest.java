/*
 * Mars Simulation Project
 * LoadControllerTest
 * @date 2023-04-16
 * @author Scott Davis
 */

package com.mars_sim.core.vehicle.task;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentInventory;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.resource.SuppliesManifest;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;



/**
 * Tests the loading controller operation for vehicles.
 */
public class LoadControllerTest extends MarsSimUnitTest {

	// Extra amount to add to resource to handle double arithmetic mismatch
	private static final String SMALL_HAMMER = "small hammer";
	private static final String FIRE_EXTINGUSHER = "fire extinguisher";
	private static final String PIPE_WRENCH = "pipe wrench";

	private Settlement settlement = null;
	private Vehicle vehicle;
	private Person person;
	private Integer pipeWrenchID;
	private Integer smallHammerID;
	private Integer fireExtinguisherID;

	@BeforeEach
	void setUp() {
		super.init();

		// Create test settlement.
		settlement = buildSettlement("Mock");
		vehicle = buildRover(settlement, "V", LocalPosition.DEFAULT_POSITION, CARGO_ROVER);
		person = buildPerson("Fred", settlement);
	
		// Make the person strong to get loading quicker
		person.getNaturalAttributeManager().setAttribute(NaturalAttributeType.STRENGTH, 40);

		smallHammerID = ItemResourceUtil.findIDbyItemResourceName(SMALL_HAMMER);
		fireExtinguisherID = ItemResourceUtil.findIDbyItemResourceName(FIRE_EXTINGUSHER);
		pipeWrenchID = ItemResourceUtil.findIDbyItemResourceName(PIPE_WRENCH);
    }

	/*
	 * Test method for 'com.mars_sim.simulation.person.ai.task.LoadVehicle.LoadingPhase(double)'
	 */
	@Test
	void testBackgroundLoading() {
		var requiredResourcesMap = new SuppliesManifest();
		requiredResourcesMap.addAmount(ResourceUtil.OXYGEN_ID, 20D, true);
		requiredResourcesMap.addAmount(ResourceUtil.METHANOL_ID, 10D, true);

		loadSettlement(settlement, requiredResourcesMap);

		LoadingController controller = vehicle.setLoading(requiredResourcesMap);
		int loadingCount = 0;
		while (loadingCount < 100) {
			controller.backgroundLoad(80);
			loadingCount++;
		}
		assertTrue((loadingCount > 1), "Multiple loadings");
		assertTrue(controller.isCompleted(), "Loading controller complete");

		checkVehicleResources(vehicle.getEquipmentInventory(), requiredResourcesMap);
	}

	/*
	 * Test method loading Equipment
	 */
	@Test
	void testLoadRequiredEquipment() {
		var manifest = new SuppliesManifest();
		manifest.addEquipment(EquipmentType.BARREL.getResourceID(), 10, true);
		manifest.addEquipment(EquipmentType.SPECIMEN_BOX.getResourceID(), 5, true);
		manifest.addEquipment(EquipmentType.EVA_SUIT.getResourceID(), 2, true);

		// Load the manifest
		testLoading(100, manifest);
	}

	/*
	 * Test method loading Equipment
	 */
	@Test
	void testLoadOptionalEquipment() {
		var manifest = new SuppliesManifest();
		manifest.addEquipment(EquipmentType.BARREL.getResourceID(), 10, true);
		manifest.addEquipment(EquipmentType.SPECIMEN_BOX.getResourceID(), 5, true);

		manifest.addEquipment(EquipmentType.GAS_CANISTER.getResourceID(), 10, false);

		// Load the manifest
		testLoading(100, manifest);
	}

	/*
	 * Test method loading Equipment
	 */
	@Test
	void testLoadMissingOptionalEquipment() {
		var manifest = new SuppliesManifest();
		manifest.addEquipment(EquipmentType.BARREL.getResourceID(), 10, true);
		manifest.addEquipment(EquipmentType.SPECIMEN_BOX.getResourceID(), 5, true);

		manifest.addEquipment(EquipmentType.GAS_CANISTER.getResourceID(), 10, false);

		loadSettlement(settlement, manifest);

		// Add an extra resource that will not be present
		int missingId = EquipmentType.LARGE_BAG.getResourceID();
		var expanded = new SuppliesManifest(manifest);
		expanded.addEquipment(missingId, 10, false);

		loadIt(100, expanded);

		// Check Equipment that was present in settlement
		var vechEO = vehicle.getEquipmentInventory();
		checkVehicleInventory(vechEO, manifest);

		EquipmentType eType = EquipmentType.convertID2Type(missingId);
		long optionalLoaded = vechEO.getEquipmentSet().stream()
				.filter(e -> (e.getEquipmentType() == eType))
				.count();
		assertEquals(0, optionalLoaded, "Optional Equipment loaded");
	}



	/*
	 * Test method loading Resource Items
	 */
	@Test
	 void testLoadRequiredItemResources() {
		var manifest = new SuppliesManifest();
		manifest.addItem(fireExtinguisherID, 1, true);
		manifest.addItem(smallHammerID, 2, true);

		// Load the manifest
		testLoading(100, manifest);
	}

	/*
	 * Test method loading Resource Items
	 */
	@Test
	void testLoadOptionalItemResources() {
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
	@Test
	void testLoadMissingOptionalItemResources() {
		var manifest = new SuppliesManifest();
		manifest.addItem(fireExtinguisherID, 1, true);
		manifest.addItem(smallHammerID, 2, true);

		manifest.addItem(pipeWrenchID, 10, false);

		testLoadOptionalResources(100, manifest,
								  ItemResourceUtil.SLS_3D_PRINTER_ID);
	}

	/*
	 * Test method for 'com.mars_sim.simulation.person.ai.task.LoadVehicle.LoadingPhase(double)'
	 */
	@Test
	void testLoadRequiredAmountResources() {
		var manifest = new SuppliesManifest();
		manifest.addAmount(ResourceUtil.FOOD_ID, 30D, true);
		manifest.addAmount(ResourceUtil.WATER_ID, 10D, true);

		// Load the manifest
		testLoading(200, manifest);
	}


	/*
	 * Test method for 'com.mars_sim.simulation.person.ai.task.LoadVehicle.LoadingPhase(double)'
	 */
	@Test
	void testLoadFailedAmountResources() {
		var requiredResourcesMap = new SuppliesManifest();
		// Add 2000kg food to the manifest
		requiredResourcesMap.addAmount(ResourceUtil.FOOD_ID, 50D, true);

		LoadingController controller = vehicle.setLoading(requiredResourcesMap);

		// Run the loader but do not load an resources into the settlement
		for(int i = 0 ; i < (LoadingController.MAX_SETTLEMENT_ATTEMPTS - 1); i++) {
			controller.load(person, 1D);
			assertFalse(controller.isCompleted(), "Load completed #" + i);
			assertFalse(controller.isFailure(), "Load failed #" + i);
		}

		// Do the last load and it should fail
		controller.load(person, 1D);

		// Vehicle is not loaded and failed
		assertFalse(controller.isCompleted(), "Vehicle loaded");
		assertTrue(controller.isFailure(), "Vehicle load did not failed");
	}

	/*
	 * Test method for 'com.mars_sim.simulation.person.ai.task.LoadVehicle.LoadingPhase(double)'
	 */
	@Test
	void testLoadOptionalAmountResources() {
		var manifest = new SuppliesManifest();
		manifest.addAmount(ResourceUtil.FOOD_ID, 20D, true);
		manifest.addAmount(ResourceUtil.WATER_ID, 10D, true);

		manifest.addAmount(ResourceUtil.CO2_ID, 4D, false);

		// Load the manifest
		testLoading(200, manifest);
	}

	/*
	 * Load with optional resource present
	 */
	@Test
	void testLoadMissingOptionalAmountResources() {
		var manifest = new SuppliesManifest();
		manifest.addAmount(ResourceUtil.FOOD_ID, 100D, true);

		manifest.addAmount(ResourceUtil.CO2_ID, 4D, false);

		testLoadOptionalResources(100, manifest,
								  ResourceUtil.NITROGEN_ID);
	}

	/*
	 * Test method loading Equipment
	 */
	@Test
	void testLoadFull() {
		var manifest = new SuppliesManifest();
		manifest.addEquipment(EquipmentType.getResourceID(EquipmentType.BARREL), 5, true);
		manifest.addEquipment(EquipmentType.getResourceID(EquipmentType.SPECIMEN_BOX), 5, true);

		manifest.addEquipment(EquipmentType.getResourceID(EquipmentType.GAS_CANISTER), 5, false);

		manifest.addAmount(ResourceUtil.FOOD_ID, 100D, true);
		manifest.addItem(fireExtinguisherID, 1, true);
		manifest.addItem(smallHammerID, 2, true);

		manifest.addAmount(ResourceUtil.CO2_ID, 4D, false);
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

		var vechEO = vehicle.getEquipmentInventory();

		// Make sure Vehicle has capacity
		setVehicleCapacity(vechEO, manifest);

		// Load the manifest
		loadIt(maxCycles, manifest);
		checkVehicleInventory(vechEO, manifest);

		// Reload the same manifest which should complete immediately
		reload(manifest);
	}

	private void checkVehicleInventory(EquipmentOwner v, SuppliesManifest manifest) {
		checkVehicleEquipment(v, manifest);
		checkVehicleResources(v, manifest);
	}

	private void setVehicleCapacity(EquipmentOwner v, SuppliesManifest manifest) {
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

		var vehEO = vehicle.getEquipmentInventory();
		setVehicleCapacity(vehEO, manifest);

		// Add an extra resource that will not be present
		var extraOptionalResources = new SuppliesManifest(manifest);
		extraOptionalResources.addAmount(missingId, 10D, false);

		loadIt(maxCycles, manifest);

		checkVehicleInventory(vehEO, manifest);

		double optionalLoaded;
		if (ResourceType.getType(missingId) == ResourceType.AMOUNT_RESOURCE) {
			optionalLoaded = vehEO.getSpecificAmountResourceStored(missingId);
		}
		else {
			optionalLoaded = vehEO.getItemResourceStored(missingId);
		}

		assertEquals(0D, optionalLoaded, "Optional resource loaded");
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
		assertTrue(controller.isCompleted(), "Vehicle already loaded");
		assertTrue(controller.load(person, 1), "Reload completes on first attempt");

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
		assertTrue(vehicle.haveStatusType(StatusType.LOADING), "Vehicle has of LOADING");
		assertEquals(vehicle, controller.getVehicle(), "Vehicle of the controller");

		int loadingCount = 0;
		boolean loaded = false;
		while (!loaded && (loadingCount < maxCycles)) {
			loaded  = controller.load(person, 1);
			loadingCount++;
		}
		assertTrue((loadingCount > 1), "Multiple loadings");
		assertTrue(loaded, "Load operation stopped on load complete");
		assertFalse(controller.isFailure(), "Loading controller successful");
		assertTrue(controller.isCompleted(), "Loading controller complete");
		assertFalse(vehicle.haveStatusType(StatusType.LOADING), "Vehicle clear of LOADING");

		return controller;
	}

	/**
	 * Check if a vehicle has the required Equipment
	 * @param source
	 * @param equipmentManifest
	 */
	private void checkVehicleEquipment(EquipmentOwner source, SuppliesManifest manifest) {
		var eqm = new HashMap<>(manifest.getEquipment(true));
		eqm.putAll(manifest.getEquipment(false));

		for(Entry<Integer, Integer> item : eqm.entrySet()) {
			EquipmentType eType = EquipmentType.convertID2Type(item.getKey());
			long stored = source.getEquipmentSet().stream()
					.filter(e -> (e.getEquipmentType() == eType))
					.count();
			assertEquals(item.getValue().intValue(), stored, "Equipment in vehicle " + eType.name());
		}
	}


	/**
	 * Check if the Vehicle has the Resources defined by a manifest
	 * @param source
	 * @param manifest
	 */
	private void checkVehicleResources(EquipmentOwner source, SuppliesManifest manifest) {

		var requiredResourcesMap = new HashMap<>(manifest.getAmounts(true));
		requiredResourcesMap.putAll(manifest.getAmounts(false));
		for (Entry<Integer, Double> resource : requiredResourcesMap.entrySet()) {
			int key = resource.getKey();
			if (ResourceType.getType(key) == ResourceType.AMOUNT_RESOURCE) {
				String resourceName = ResourceUtil.findAmountResourceName(key);

				double stored = source.getSpecificAmountResourceStored(key);
				double expected = resource.getValue().doubleValue();
				assertLessThan("Vehicle amount resource stored " + resourceName, expected, stored);
			}
			else {
				int stored = source.getItemResourceStored(key);
				int expected = resource.getValue().intValue();
				String itemName = ItemResourceUtil.findItemResourceName(key);
				assertEquals(expected, stored, "Vehicle item resource stored " + itemName);
			}
		}
	}

	private static void assertLessThan(String message, double expected, double stored) {
		boolean test = expected <= stored;
		if (!test) {
			assertTrue(test, message + ":" + expected + " <= " + stored);
		}
	}

	/**
	 * Set the capacity of the Vehicle to support the manifest
	 * @param target
	 * @param requiredResourcesMap
	 */
	private void setResourcesCapacity(EquipmentOwner target, Map<Integer, Double> requiredResourcesMap) {
		
		// Not nice, EquipmentOwner needs to support capacity changes
		EquipmentInventory inv = (EquipmentInventory)target;
		for (Entry<Integer, Double> v : requiredResourcesMap.entrySet()) {
			inv.setSpecificResourceCapacity(v.getKey(), v.getValue().doubleValue() * 1.01D);
		}
	}

	/**
	 * Load up a Settlements Amount Resources from a manifest
	 * @param target
	 * @param requiredResourcesMap
	 */
	public static void loadSettlement(Settlement target, SuppliesManifest requiredResourcesMap) {
		var settlementEO = target.getEquipmentInventory();
		loadAmounts(settlementEO, requiredResourcesMap.getAmounts(true));
		loadAmounts(settlementEO, requiredResourcesMap.getAmounts(false));
		loadSettlementEquipment(target, requiredResourcesMap.getEquipment(true));
		loadSettlementEquipment(target, requiredResourcesMap.getEquipment(false));
		loadItems(settlementEO, requiredResourcesMap.getItems(true));
		loadItems(settlementEO, requiredResourcesMap.getItems(false));
	}
}