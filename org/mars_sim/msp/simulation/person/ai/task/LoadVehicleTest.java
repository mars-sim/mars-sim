/**
 * Mars Simulation Project
 * LoadVehicleTest.java
 * @version 2.81 2007-08-12
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

	/*
	 * Test method for 'org.mars_sim.msp.simulation.person.ai.task.LoadVehicle.LoadingPhase(double)'
	 */
	public void testLoadingPhase() throws Exception {
		Settlement settlement = new MockSettlement();
		Person person = new Person("test person", Person.MALE, settlement);
		person.getNaturalAttributeManager().setAttribute(NaturalAttributeManager.STRENGTH, 100);
		Vehicle vehicle = new MockVehicle(settlement);
		
		Inventory vehicleInv = vehicle.getInventory();
		
		vehicleInv.addAmountResourceTypeCapacity(AmountResource.OXYGEN, 100D);
		vehicleInv.addAmountResourceTypeCapacity(AmountResource.FOOD, 100D);
		vehicleInv.addAmountResourceTypeCapacity(AmountResource.WATER, 100D);
		vehicleInv.addAmountResourceTypeCapacity(AmountResource.METHANE, 100D);
		vehicleInv.addGeneralCapacity(100D);
		
		Inventory settlementInv = settlement.getInventory();
		
		settlementInv.addAmountResourceTypeCapacity(AmountResource.OXYGEN, 100D);
		settlementInv.storeAmountResource(AmountResource.OXYGEN, 100D);
		settlementInv.addAmountResourceTypeCapacity(AmountResource.FOOD, 100D);
		settlementInv.storeAmountResource(AmountResource.FOOD, 100D);
		settlementInv.addAmountResourceTypeCapacity(AmountResource.WATER, 100D);
		settlementInv.storeAmountResource(AmountResource.WATER, 100D);
		settlementInv.addAmountResourceTypeCapacity(AmountResource.METHANE, 100D);
		settlementInv.storeAmountResource(AmountResource.METHANE, 100D);
		settlementInv.storeItemResources(ItemResource.HAMMER, 5);
		
		for (int x = 0; x < 5; x++) {
			settlementInv.storeUnit(new SpecimenContainer(settlement.getCoordinates()));
		}
		
		Map<Resource, Number> resourcesMap = new HashMap<Resource, Number>();
		resourcesMap.put(AmountResource.OXYGEN, new Double(100D));
		resourcesMap.put(AmountResource.FOOD, new Double(100D));
		resourcesMap.put(AmountResource.WATER, new Double(100D));
		resourcesMap.put(AmountResource.METHANE, new Double(100D));
		resourcesMap.put(ItemResource.HAMMER, new Integer(5));
		
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
		
		inv.addAmountResourceTypeCapacity(AmountResource.OXYGEN, 100D);
		inv.storeAmountResource(AmountResource.OXYGEN, 100D);
		inv.addAmountResourceTypeCapacity(AmountResource.FOOD, 100D);
		inv.storeAmountResource(AmountResource.FOOD, 100D);
		inv.addAmountResourceTypeCapacity(AmountResource.WATER, 100D);
		inv.storeAmountResource(AmountResource.WATER, 100D);
		inv.addAmountResourceTypeCapacity(AmountResource.METHANE, 100D);
		inv.storeAmountResource(AmountResource.METHANE, 100D);
		
		inv.storeItemResources(ItemResource.HAMMER, 5);
		
		for (int x = 0; x < 5; x++) {
			inv.storeUnit(new SpecimenContainer(settlement.getCoordinates()));
		}
		
		Map<Resource, Number> resourcesMap = new HashMap<Resource, Number>();
		resourcesMap.put(AmountResource.OXYGEN, new Double(100D));
		resourcesMap.put(AmountResource.FOOD, new Double(100D));
		resourcesMap.put(AmountResource.WATER, new Double(100D));
		resourcesMap.put(AmountResource.METHANE, new Double(100D));
		resourcesMap.put(ItemResource.HAMMER, new Integer(5));
		
		Map<Class, Integer> equipmentMap = new HashMap<Class, Integer>();
		equipmentMap.put(SpecimenContainer.class, new Integer(5));
		
		assertTrue("Enough supplies at settlement for trip.", 
				LoadVehicle.hasEnoughSupplies(settlement, resourcesMap, equipmentMap, 0, 0D));
	}
	
	public void testHasEnoughSuppliesNoAmountResources() throws Exception {
		Settlement settlement = new MockSettlement();
		Inventory inv = settlement.getInventory();
		
		inv.storeItemResources(ItemResource.HAMMER, 5);
		
		for (int x = 0; x < 5; x++) {
			inv.storeUnit(new SpecimenContainer(settlement.getCoordinates()));
		}
		
		Map<Resource, Number> resourcesMap = new HashMap<Resource, Number>();
		resourcesMap.put(AmountResource.OXYGEN, new Double(100D));
		resourcesMap.put(AmountResource.FOOD, new Double(100D));
		resourcesMap.put(AmountResource.WATER, new Double(100D));
		resourcesMap.put(AmountResource.METHANE, new Double(100D));
		resourcesMap.put(ItemResource.HAMMER, new Integer(5));
		
		Map<Class, Integer> equipmentMap = new HashMap<Class, Integer>();
		equipmentMap.put(SpecimenContainer.class, new Integer(5));
		
		assertFalse("Not enough amount resource supplies at settlement for trip.", 
				LoadVehicle.hasEnoughSupplies(settlement, resourcesMap, equipmentMap, 0, 0D));
	}
	
	public void testHasEnoughSuppliesNoItemResources() throws Exception {
		Settlement settlement = new MockSettlement();
		Inventory inv = settlement.getInventory();
		
		inv.addAmountResourceTypeCapacity(AmountResource.OXYGEN, 100D);
		inv.storeAmountResource(AmountResource.OXYGEN, 100D);
		inv.addAmountResourceTypeCapacity(AmountResource.FOOD, 100D);
		inv.storeAmountResource(AmountResource.FOOD, 100D);
		inv.addAmountResourceTypeCapacity(AmountResource.WATER, 100D);
		inv.storeAmountResource(AmountResource.WATER, 100D);
		inv.addAmountResourceTypeCapacity(AmountResource.METHANE, 100D);
		inv.storeAmountResource(AmountResource.METHANE, 100D);
		
		for (int x = 0; x < 5; x++) {
			inv.storeUnit(new SpecimenContainer(settlement.getCoordinates()));
		}
		
		Map<Resource, Number> resourcesMap = new HashMap<Resource, Number>();
		resourcesMap.put(AmountResource.OXYGEN, new Double(100D));
		resourcesMap.put(AmountResource.FOOD, new Double(100D));
		resourcesMap.put(AmountResource.WATER, new Double(100D));
		resourcesMap.put(AmountResource.METHANE, new Double(100D));
		resourcesMap.put(ItemResource.HAMMER, new Integer(5));
		
		Map<Class, Integer> equipmentMap = new HashMap<Class, Integer>();
		equipmentMap.put(SpecimenContainer.class, new Integer(5));
		
		assertFalse("Not enough item resource supplies at settlement for trip.", 
				LoadVehicle.hasEnoughSupplies(settlement, resourcesMap, equipmentMap, 0, 0D));
	}
	
	public void testHasEnoughSuppliesNoEquipment() throws Exception {
		Settlement settlement = new MockSettlement();
		Inventory inv = settlement.getInventory();
		
		inv.addAmountResourceTypeCapacity(AmountResource.OXYGEN, 100D);
		inv.storeAmountResource(AmountResource.OXYGEN, 100D);
		inv.addAmountResourceTypeCapacity(AmountResource.FOOD, 100D);
		inv.storeAmountResource(AmountResource.FOOD, 100D);
		inv.addAmountResourceTypeCapacity(AmountResource.WATER, 100D);
		inv.storeAmountResource(AmountResource.WATER, 100D);
		inv.addAmountResourceTypeCapacity(AmountResource.METHANE, 100D);
		inv.storeAmountResource(AmountResource.METHANE, 100D);
		
		inv.storeItemResources(ItemResource.HAMMER, 5);
		
		Map<Resource, Number> resourcesMap = new HashMap<Resource, Number>();
		resourcesMap.put(AmountResource.OXYGEN, new Double(100D));
		resourcesMap.put(AmountResource.FOOD, new Double(100D));
		resourcesMap.put(AmountResource.WATER, new Double(100D));
		resourcesMap.put(AmountResource.METHANE, new Double(100D));
		resourcesMap.put(ItemResource.HAMMER, new Integer(5));
		
		Map<Class, Integer> equipmentMap = new HashMap<Class, Integer>();
		equipmentMap.put(SpecimenContainer.class, new Integer(5));
		
		assertFalse("Not enough equipment supplies at settlement for trip.", 
				LoadVehicle.hasEnoughSupplies(settlement, resourcesMap, equipmentMap, 0, 0D));
	}

	/*
	 * Test method for 'org.mars_sim.msp.simulation.person.ai.task.LoadVehicle.isFullyLoaded()'
	 */
	public void testIsFullyLoadedGood() throws Exception {
		Settlement settlement = new MockSettlement();
		Vehicle vehicle = new MockVehicle(settlement);
		Inventory inv = vehicle.getInventory();
		inv.addGeneralCapacity(100D);
		
		inv.addAmountResourceTypeCapacity(AmountResource.OXYGEN, 100D);
		inv.storeAmountResource(AmountResource.OXYGEN, 100D);
		inv.addAmountResourceTypeCapacity(AmountResource.FOOD, 100D);
		inv.storeAmountResource(AmountResource.FOOD, 100D);
		inv.addAmountResourceTypeCapacity(AmountResource.WATER, 100D);
		inv.storeAmountResource(AmountResource.WATER, 100D);
		inv.addAmountResourceTypeCapacity(AmountResource.METHANE, 100D);
		inv.storeAmountResource(AmountResource.METHANE, 100D);
		inv.storeItemResources(ItemResource.HAMMER, 5);
		
		for (int x = 0; x < 5; x++) {
			inv.storeUnit(new SpecimenContainer(settlement.getCoordinates()));
		}
		
		Map<Resource, Number> resourcesMap = new HashMap<Resource, Number>();
		resourcesMap.put(AmountResource.OXYGEN, new Double(100D));
		resourcesMap.put(AmountResource.FOOD, new Double(100D));
		resourcesMap.put(AmountResource.WATER, new Double(100D));
		resourcesMap.put(AmountResource.METHANE, new Double(100D));
		resourcesMap.put(ItemResource.HAMMER, new Integer(5));
		
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
		Inventory inv = vehicle.getInventory();
		inv.addGeneralCapacity(100D);
		
		inv.storeItemResources(ItemResource.HAMMER, 5);
		
		for (int x = 0; x < 5; x++) {
			inv.storeUnit(new SpecimenContainer(settlement.getCoordinates()));
		}
		
		Map<Resource, Number> resourcesMap = new HashMap<Resource, Number>();
		resourcesMap.put(AmountResource.OXYGEN, new Double(100D));
		resourcesMap.put(AmountResource.FOOD, new Double(100D));
		resourcesMap.put(AmountResource.WATER, new Double(100D));
		resourcesMap.put(AmountResource.METHANE, new Double(100D));
		resourcesMap.put(ItemResource.HAMMER, new Integer(5));
		
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
		Inventory inv = vehicle.getInventory();
		inv.addGeneralCapacity(100D);
		
		inv.addAmountResourceTypeCapacity(AmountResource.OXYGEN, 100D);
		inv.storeAmountResource(AmountResource.OXYGEN, 100D);
		inv.addAmountResourceTypeCapacity(AmountResource.FOOD, 100D);
		inv.storeAmountResource(AmountResource.FOOD, 100D);
		inv.addAmountResourceTypeCapacity(AmountResource.WATER, 100D);
		inv.storeAmountResource(AmountResource.WATER, 100D);
		inv.addAmountResourceTypeCapacity(AmountResource.METHANE, 100D);
		inv.storeAmountResource(AmountResource.METHANE, 100D);
		
		for (int x = 0; x < 5; x++) {
			inv.storeUnit(new SpecimenContainer(settlement.getCoordinates()));
		}
		
		Map<Resource, Number> resourcesMap = new HashMap<Resource, Number>();
		resourcesMap.put(AmountResource.OXYGEN, new Double(100D));
		resourcesMap.put(AmountResource.FOOD, new Double(100D));
		resourcesMap.put(AmountResource.WATER, new Double(100D));
		resourcesMap.put(AmountResource.METHANE, new Double(100D));
		resourcesMap.put(ItemResource.HAMMER, new Integer(5));
		
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
		Inventory inv = vehicle.getInventory();
		inv.addGeneralCapacity(100D);
		
		inv.addAmountResourceTypeCapacity(AmountResource.OXYGEN, 100D);
		inv.storeAmountResource(AmountResource.OXYGEN, 100D);
		inv.addAmountResourceTypeCapacity(AmountResource.FOOD, 100D);
		inv.storeAmountResource(AmountResource.FOOD, 100D);
		inv.addAmountResourceTypeCapacity(AmountResource.WATER, 100D);
		inv.storeAmountResource(AmountResource.WATER, 100D);
		inv.addAmountResourceTypeCapacity(AmountResource.METHANE, 100D);
		inv.storeAmountResource(AmountResource.METHANE, 100D);
		inv.storeItemResources(ItemResource.HAMMER, 5);
		
		Map<Resource, Number> resourcesMap = new HashMap<Resource, Number>();
		resourcesMap.put(AmountResource.OXYGEN, new Double(100D));
		resourcesMap.put(AmountResource.FOOD, new Double(100D));
		resourcesMap.put(AmountResource.WATER, new Double(100D));
		resourcesMap.put(AmountResource.METHANE, new Double(100D));
		resourcesMap.put(ItemResource.HAMMER, new Integer(5));
		
		Map<Class, Integer> equipmentMap = new HashMap<Class, Integer>();
		equipmentMap.put(SpecimenContainer.class, new Integer(5));
		
		assertFalse("Vehicle is not fully loaded", LoadVehicle.isFullyLoaded(resourcesMap, equipmentMap, vehicle));
	}
}