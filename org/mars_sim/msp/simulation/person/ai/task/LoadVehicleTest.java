/**
 * Mars Simulation Project
 * LoadVehicleTest.java
 * @version 2.84 2008-05-17
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;

import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.equipment.SpecimenContainer;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.NaturalAttributeManager;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.ItemResource;
import org.mars_sim.msp.simulation.resource.Resource;
import org.mars_sim.msp.simulation.structure.MockSettlement;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.MockVehicle;
import org.mars_sim.msp.simulation.vehicle.Vehicle;

public class LoadVehicleTest extends TestCase {

	private static final String OXYGEN = "oxygen";
	private static final String WATER = "water";
	private static final String METHANE = "methane";
	private static final String FOOD = "food";
	
	/*
	 * Test method for 'org.mars_sim.msp.simulation.person.ai.task.LoadVehicle.LoadingPhase(double)'
	 */
	public void testLoadingPhase() throws Exception {
		Settlement settlement = new MockSettlement();
		Person person = new Person("test person", Person.MALE, settlement);
		person.getNaturalAttributeManager().setAttribute(NaturalAttributeManager.STRENGTH, 100);
		Vehicle vehicle = new MockVehicle(settlement);
		ItemResource hammer = ItemResource.getTestResourceHammer();
		
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
		
		Map<Resource, Number> resourcesMap = new HashMap<Resource, Number>();
		resourcesMap.put(oxygen, new Double(100D));
		resourcesMap.put(food, new Double(100D));
		resourcesMap.put(water, new Double(100D));
		resourcesMap.put(methane, new Double(100D));
		resourcesMap.put(hammer, new Integer(5));
		
		Map<Class, Integer> equipmentMap = new HashMap<Class, Integer>();
		equipmentMap.put(SpecimenContainer.class, new Integer(5));
		
		LoadVehicle loadVehicle = new LoadVehicle(person, vehicle, resourcesMap, equipmentMap);
		loadVehicle.loadingPhase(10D);
		
		assertEquals("Vehicle loaded correctly.", 5, vehicle.getInventory().findNumUnitsOfClass(SpecimenContainer.class));
	}

	/*
	 * Test method for 'org.mars_sim.msp.simulation.person.ai.task.LoadVehicle.hasEnoughSupplies(Settlement, Map, Map)'
	 */
	public void testHasEnoughSuppliesGood() throws Exception {
		Settlement settlement = new MockSettlement();
		Inventory inv = settlement.getInventory();
		ItemResource hammer = ItemResource.getTestResourceHammer();
		
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
		resourcesMap.put(hammer, new Integer(5));
		
		Map<Class, Integer> equipmentMap = new HashMap<Class, Integer>();
		equipmentMap.put(SpecimenContainer.class, new Integer(5));
		
		Vehicle vehicle = new MockVehicle(settlement);
		
		assertTrue("Enough supplies at settlement for trip.", 
				LoadVehicle.hasEnoughSupplies(settlement, vehicle, resourcesMap, equipmentMap, 0, 0D));
	}
	
	public void testHasEnoughSuppliesNoAmountResources() throws Exception {
		Settlement settlement = new MockSettlement();
		Inventory inv = settlement.getInventory();
		ItemResource hammer = ItemResource.getTestResourceHammer();
		
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
		resourcesMap.put(hammer, new Integer(5));
		
		Map<Class, Integer> equipmentMap = new HashMap<Class, Integer>();
		equipmentMap.put(SpecimenContainer.class, new Integer(5));
		
		Vehicle vehicle = new MockVehicle(settlement);
		
		assertFalse("Not enough amount resource supplies at settlement for trip.", 
				LoadVehicle.hasEnoughSupplies(settlement, vehicle, resourcesMap, equipmentMap, 0, 0D));
	}
	
	public void testHasEnoughSuppliesNoItemResources() throws Exception {
		Settlement settlement = new MockSettlement();
		Inventory inv = settlement.getInventory();
		ItemResource hammer = ItemResource.getTestResourceHammer();
		
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
		resourcesMap.put(hammer, new Integer(5));
		
		Map<Class, Integer> equipmentMap = new HashMap<Class, Integer>();
		equipmentMap.put(SpecimenContainer.class, new Integer(5));
		
		Vehicle vehicle = new MockVehicle(settlement);
		
		assertFalse("Not enough item resource supplies at settlement for trip.", 
				LoadVehicle.hasEnoughSupplies(settlement, vehicle, resourcesMap, equipmentMap, 0, 0D));
	}
	
	public void testHasEnoughSuppliesNoEquipment() throws Exception {
		Settlement settlement = new MockSettlement();
		Inventory inv = settlement.getInventory();
		ItemResource hammer = ItemResource.getTestResourceHammer();
		
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
		resourcesMap.put(hammer, new Integer(5));
		
		Map<Class, Integer> equipmentMap = new HashMap<Class, Integer>();
		equipmentMap.put(SpecimenContainer.class, new Integer(5));
		
		Vehicle vehicle = new MockVehicle(settlement);
		
		assertFalse("Not enough equipment supplies at settlement for trip.", 
				LoadVehicle.hasEnoughSupplies(settlement, vehicle, resourcesMap, equipmentMap, 0, 0D));
	}

	/*
	 * Test method for 'org.mars_sim.msp.simulation.person.ai.task.LoadVehicle.isFullyLoaded()'
	 */
	public void testIsFullyLoadedGood() throws Exception {
		Settlement settlement = new MockSettlement();
		Vehicle vehicle = new MockVehicle(settlement);
		ItemResource hammer = ItemResource.getTestResourceHammer();
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
		
		Map<Resource, Number> resourcesMap = new HashMap<Resource, Number>();
		resourcesMap.put(oxygen, new Double(100D));
		resourcesMap.put(food, new Double(100D));
		resourcesMap.put(water, new Double(100D));
		resourcesMap.put(methane, new Double(100D));
		resourcesMap.put(hammer, new Integer(5));
		
		Map<Class, Integer> equipmentMap = new HashMap<Class, Integer>();
		equipmentMap.put(SpecimenContainer.class, new Integer(5));
		
		assertTrue("Vehicle is fully loaded", LoadVehicle.isFullyLoaded(resourcesMap, equipmentMap, vehicle));
	}
	
	/*
	 * Test method for 'org.mars_sim.msp.simulation.person.ai.task.LoadVehicle.isFullyLoaded()'
	 */
	public void testIsFullyLoadedNoAmountResources() throws Exception {
		Settlement settlement = new MockSettlement();
		Vehicle vehicle = new MockVehicle(settlement);
		ItemResource hammer = ItemResource.getTestResourceHammer();
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
		
		Map<Resource, Number> resourcesMap = new HashMap<Resource, Number>();
		resourcesMap.put(oxygen, new Double(100D));
		resourcesMap.put(food, new Double(100D));
		resourcesMap.put(water, new Double(100D));
		resourcesMap.put(methane, new Double(100D));
		resourcesMap.put(hammer, new Integer(5));
		
		Map<Class, Integer> equipmentMap = new HashMap<Class, Integer>();
		equipmentMap.put(SpecimenContainer.class, new Integer(5));
		
		assertFalse("Vehicle is not fully loaded", LoadVehicle.isFullyLoaded(resourcesMap, equipmentMap, vehicle));
	}
	
	/*
	 * Test method for 'org.mars_sim.msp.simulation.person.ai.task.LoadVehicle.isFullyLoaded()'
	 */
	public void testIsFullyLoadedNoItemResources() throws Exception {
		Settlement settlement = new MockSettlement();
		Vehicle vehicle = new MockVehicle(settlement);
		ItemResource hammer = ItemResource.getTestResourceHammer();
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
		
		Map<Resource, Number> resourcesMap = new HashMap<Resource, Number>();
		resourcesMap.put(oxygen, new Double(100D));
		resourcesMap.put(food, new Double(100D));
		resourcesMap.put(water, new Double(100D));
		resourcesMap.put(methane, new Double(100D));
		resourcesMap.put(hammer, new Integer(5));
		
		Map<Class, Integer> equipmentMap = new HashMap<Class, Integer>();
		equipmentMap.put(SpecimenContainer.class, new Integer(5));
		
		assertFalse("Vehicle is not fully loaded", LoadVehicle.isFullyLoaded(resourcesMap, equipmentMap, vehicle));
	}
	
	/*
	 * Test method for 'org.mars_sim.msp.simulation.person.ai.task.LoadVehicle.isFullyLoaded()'
	 */
	public void testIsFullyLoadedNoEquipment() throws Exception {
		Settlement settlement = new MockSettlement();
		Vehicle vehicle = new MockVehicle(settlement);
		ItemResource hammer = ItemResource.getTestResourceHammer();
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
		
		Map<Resource, Number> resourcesMap = new HashMap<Resource, Number>();
		resourcesMap.put(oxygen, new Double(100D));
		resourcesMap.put(food, new Double(100D));
		resourcesMap.put(water, new Double(100D));
		resourcesMap.put(methane, new Double(100D));
		resourcesMap.put(hammer, new Integer(5));
		
		Map<Class, Integer> equipmentMap = new HashMap<Class, Integer>();
		equipmentMap.put(SpecimenContainer.class, new Integer(5));
		
		assertFalse("Vehicle is not fully loaded", LoadVehicle.isFullyLoaded(resourcesMap, equipmentMap, vehicle));
	}
}