/**
 * Mars Simulation Project
 * LoadVehicleTest.java
 * @version 3.1.0 2017-01-21
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.MockSettlement;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

import junit.framework.TestCase;

public class LoadVehicleTest
extends TestCase {

	// Extra amount to add to resoruce to handle double arithmetic mismatch
	private static final double EXTRA_RESOURCE = 0.01D;
	
	private Settlement settlement = null;
	private UnitManager unitManager;
	private Vehicle vehicle;
	private Person person;
	
	@Override
    public void setUp() throws Exception {
        SimulationConfig.instance().loadConfig();
        Simulation.instance().testRun();
        
        unitManager = Simulation.instance().getUnitManager();
		Iterator<Settlement> i = unitManager.getSettlements().iterator();
		while (i.hasNext()) {
			unitManager.removeUnit(i.next());
		}
				
		// Create test settlement.
		settlement = new MockSettlement();	
		unitManager.addUnit(settlement);
		
		vehicle = new Rover("Test", "Cargo Rover", settlement);
		unitManager.addUnit(vehicle);
		
		person = new Person("Jim Loader", settlement);
		unitManager.addUnit(person);
		
		// Make the person strong to get loading quicker
		person.getNaturalAttributeManager().setAttribute(NaturalAttributeType.STRENGTH, 40);
    }

	/*
	 * Test method for 'org.mars_sim.msp.simulation.person.ai.task.LoadVehicle.LoadingPhase(double)'
	 */
	public void testBackgroundLoading() throws Exception {
		Map<Integer, Number> requiredResourcesMap = new HashMap<>();
		requiredResourcesMap.put(ResourceUtil.oxygenID, 10D);
		requiredResourcesMap.put(ResourceUtil.methaneID, 10D);
		
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
	 * Test method loading Resource Items
	 */
	public void testLoadingItemResources() throws Exception {
		Map<Integer, Number> requiredResourcesMap = new HashMap<>();
		requiredResourcesMap.put(ItemResourceUtil.fireExtinguisherID, 1);
		requiredResourcesMap.put(ItemResourceUtil.smallHammerID, 2);
		
		// Load the manifest
		testLoading(100, false, requiredResourcesMap, Collections.emptyMap(),
					   Collections.emptyMap(), Collections.emptyMap());
	}


	/*
	 * Load with optional resource present
	 */
	public void testLoadingOptionalItemResources() throws Exception {
		Map<Integer, Number> requiredResourcesMap = new HashMap<>();
		requiredResourcesMap.put(ItemResourceUtil.fireExtinguisherID, 1);
		requiredResourcesMap.put(ItemResourceUtil.smallHammerID, 2);
		
		Map<Integer, Number> optionalResourcesMap = new HashMap<>();
		optionalResourcesMap.put(ItemResourceUtil.pipeWrenchID, 10D);
		
		testLoadOptionalResources(100, requiredResourcesMap,
								  optionalResourcesMap,
								  ItemResourceUtil.printerID);
	}
	
	/*
	 * Test method for 'org.mars_sim.msp.simulation.person.ai.task.LoadVehicle.LoadingPhase(double)'
	 */
	public void testLoadingAmountResources() throws Exception {
		Map<Integer, Number> requiredResourcesMap = new HashMap<>();
		requiredResourcesMap.put(ResourceUtil.foodID, 20D);
		requiredResourcesMap.put(ResourceUtil.waterID, 10D);
		
		// Load the manifest
		testLoading(200, false, requiredResourcesMap, Collections.emptyMap(),
					   Collections.emptyMap(), Collections.emptyMap());
	}
	

	/*
	 * Load with optional resource present
	 */
	public void testLoadingOptionalAmountResources() throws Exception {
		Map<Integer, Number> requiredResourcesMap = new HashMap<>();
		requiredResourcesMap.put(ResourceUtil.foodID, 100D);

		Map<Integer, Number> optionalResourcesMap = new HashMap<>();
		optionalResourcesMap.put(ResourceUtil.co2ID, 10D);
		
		testLoadOptionalResources(100, requiredResourcesMap,
								  optionalResourcesMap,
								  ResourceUtil.nitrogenID);
	}
	
	/*
	 * Executes a loading for a manifest and a batch of tests. 
	 */
	private void testLoading(int maxCycles, boolean checkOptional,
			Map<Integer, Number> resourcesManifest,
			Map<Integer, Number> optionalResourcesManifest,
			Map<Integer, Integer> equipmentManifest,
			Map<Integer, Integer> optionalEquipmentManifest) {

		loadSettlementResources(settlement, resourcesManifest);
		loadSettlementResources(settlement, optionalResourcesManifest);
		
		// Load the manifest
		loadIt(maxCycles, resourcesManifest, optionalResourcesManifest,
			   equipmentManifest, optionalEquipmentManifest);
		checkVehicleResources(vehicle, resourcesManifest);

		if (checkOptional) {
			checkVehicleResources(vehicle, optionalResourcesManifest);
		}
		
		// Reload the same manifest which should complete immediately
		reload(resourcesManifest, optionalResourcesManifest,
				   equipmentManifest, optionalEquipmentManifest);
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
			optionalLoaded = vehicle.getInventory().getAmountResourceStored(missingId, false);
		}
		else {
			optionalLoaded = vehicle.getInventory().getItemResourceNum(missingId);
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
		assertTrue("Load operation not stopped", loaded);
		assertTrue("Loading controller complete", controller.isCompleted());
		
		return controller;
	}
	
	/**
	 * Check if the Vehicle has the Resources defined by a manifest
	 * @param source
	 * @param requiredResourcesMap
	 */
	private void checkVehicleResources(Vehicle source, Map<Integer, Number> requiredResourcesMap) {
		Inventory inv = vehicle.getInventory();

		for (Entry<Integer, Number> resource : requiredResourcesMap.entrySet()) {
			int key = resource.getKey();
			if (key < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
				String resourceName = ResourceUtil.findAmountResourceName(key);

				double stored = inv.getAmountResourceStored(key, false);
				double expected = resource.getValue().doubleValue();
				assertLessThan("Vehicle amount resource stored " + resourceName, expected, stored);
			}
			else {
				int stored = inv.getItemResourceNum(key);
				int expected = resource.getValue().intValue();
				assertEquals("Vehicle item resource stored " + key, expected, stored);
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
		Inventory inv = target.getInventory();

		for (Entry<Integer, Number> resource : requiredResourcesMap.entrySet()) {
			int key = resource.getKey();
			if (key < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
				inv.addAmountResourceTypeCapacity(resource.getKey(), resource.getValue().doubleValue() + EXTRA_RESOURCE);
			}
		}
	}
	
	/**
	 * Load up a Settlements Amount Resources from a manifest
	 * @param target
	 * @param requiredResourcesMap
	 */
	private void loadSettlementResources(Settlement target, Map<Integer, Number> requiredResourcesMap) {
		Inventory settlementInv = settlement.getInventory();

		for (Entry<Integer, Number> resource : requiredResourcesMap.entrySet()) {
			int key = resource.getKey();
			if (key < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
				double amount = resource.getValue().doubleValue() + EXTRA_RESOURCE;
				settlementInv.addAmountResourceTypeCapacity(key, amount);
				settlementInv.storeAmountResource(key, amount, true);
			}
			else {
				settlementInv.storeItemResources(key, resource.getValue().intValue());
			}	
		}
	}
}