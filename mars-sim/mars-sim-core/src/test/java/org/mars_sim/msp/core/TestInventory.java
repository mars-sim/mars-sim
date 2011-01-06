package org.mars_sim.msp.core;

import java.util.Collection;
import java.util.Set;

import junit.framework.TestCase;

import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.Phase;

public class TestInventory extends TestCase {

	private static final String CARBON_DIOXIDE = "carbon dioxide";
	private static final String HYDROGEN = "hydrogen";
	private static final String METHANE = "methane";
	private static final String FOOD = "food";
	
	public TestInventory() {
		super();
		SimulationConfig.instance();
	}
	
	public void testInventoryAmountResourceTypeCapacityGood() throws Exception {
		Inventory inventory = new Inventory(null);
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		inventory.addAmountResourceTypeCapacity(carbonDioxide, 100D);
		double amountCO2 = inventory.getAmountResourceCapacity(carbonDioxide);
		assertEquals("Amount resource type capacity set correctly.", 100D, amountCO2, 0D);
	}
	
	public void testInventoryAmountResourceTypeCapacityNegativeCapacity() throws Exception {
		Inventory inventory = new Inventory(null);
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		try {
			inventory.addAmountResourceTypeCapacity(carbonDioxide, -100D);
			fail("Cannot add negative capacity for a type.");
		}
		catch (Exception e) {}
	}
	
	public void testInventoryAmountResourcePhaseCapacityGood() throws Exception {
		Inventory inventory = new Inventory(null);
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		inventory.addAmountResourcePhaseCapacity(Phase.GAS, 100D);
		double amountCO2 = inventory.getAmountResourceCapacity(carbonDioxide);
		assertEquals("Amount resource type capacity set correctly.", 100D, amountCO2, 0D);
	}
	
	public void testInventoryAmountResourcePhaseCapacityNegativeCapacity() throws Exception {
		Inventory inventory = new Inventory(null);
		try {
			inventory.addAmountResourcePhaseCapacity(Phase.GAS, -100D);
			fail("Cannot add negative capacity for a phase.");
		}
		catch (Exception e) {}
	}
	
	public void testInventoryAmountResourceComboCapacityGood() throws Exception {
		Inventory inventory = new Inventory(null);
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		inventory.addAmountResourcePhaseCapacity(Phase.GAS, 50D);
		inventory.addAmountResourceTypeCapacity(carbonDioxide, 50D);
		double amountCO2 = inventory.getAmountResourceCapacity(carbonDioxide);
		assertEquals("Amount resource type capacity set correctly.", 100D, amountCO2, 0D);
	}
	
	public void testInventoryAmountResourceCapacityNotSet() throws Exception {
		Inventory inventory = new Inventory(null);
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		double amountCO2 = inventory.getAmountResourceCapacity(carbonDioxide);
		assertEquals("Amount resource capacity set correctly.", 0D, amountCO2, 0D);
	}
	
	public void testInventoryAmountResourceTypeStoreGood() throws Exception {
		Inventory inventory = new Inventory(null);
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		inventory.addAmountResourceTypeCapacity(carbonDioxide, 100D);
		inventory.storeAmountResource(carbonDioxide, 100D, true);
		double amountTypeStored = inventory.getAmountResourceStored(carbonDioxide);
		assertEquals("Amount resource type stored is correct.", 100D, amountTypeStored, 0D);
	}
	
	public void testInventoryAmountResourceTypeStoreOverload() throws Exception {
		Inventory inventory = new Inventory(null);
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		inventory.addAmountResourceTypeCapacity(carbonDioxide, 100D);
		try {
			inventory.storeAmountResource(carbonDioxide, 101D, true);
			fail("Throws exception if overloaded");
		}
		catch (Exception e) {}
	}
	
	public void testInventoryAmountResourcePhaseStoreGood() throws Exception {
		Inventory inventory = new Inventory(null);
		AmountResource hydrogen = AmountResource.findAmountResource(HYDROGEN);
		inventory.addAmountResourcePhaseCapacity(Phase.GAS, 100D);
		inventory.storeAmountResource(hydrogen, 100D, true);
		double amountPhaseStored = inventory.getAmountResourceStored(hydrogen);
		assertEquals("Amount resource phase stored is correct.", 100D, amountPhaseStored, 0D);
	}
	
	public void testInventoryAmountResourcePhaseStoreOverload() throws Exception {
		Inventory inventory = new Inventory(null);
		AmountResource hydrogen = AmountResource.findAmountResource(HYDROGEN);
		inventory.addAmountResourcePhaseCapacity(Phase.GAS, 100D);
		try {
			inventory.storeAmountResource(hydrogen, 101D, true);
			fail("Throws exception if overloaded");
		}
		catch (Exception e) {}
	}
	
	public void testInventoryAmountResourceStoreNegativeAmount() throws Exception {
		Inventory inventory = new Inventory(null);
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		inventory.addAmountResourceTypeCapacity(carbonDioxide, 100D);
		try {
			inventory.storeAmountResource(carbonDioxide, -1D, true);
			fail("Throws exception if negative amount");
		}
		catch (Exception e) {}
	}
	
	public void testInventoryAmountResourceStoreNoCapacity() throws Exception {
		Inventory inventory = new Inventory(null);
		try {
			AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
			inventory.storeAmountResource(carbonDioxide, 100D, true);
			fail("Throws exception if capacity not set (overloaded)");
		}
		catch (Exception e) {}
	}
	
	public void testInventoryAmountResourcePhaseStoreDeep() throws Exception {
		Inventory inventory = new Inventory(null);
		AmountResource hydrogen = AmountResource.findAmountResource(HYDROGEN);
		inventory.addGeneralCapacity(110D);
		Unit testUnit = new MockUnit1();
		testUnit.getInventory().addAmountResourcePhaseCapacity(Phase.GAS, 100D);
		inventory.storeUnit(testUnit);
		inventory.storeAmountResource(hydrogen, 100D, true);
		double amountPhaseStored = inventory.getAmountResourceStored(hydrogen);
		assertEquals("Amount resource phase stored is correct.", 100D, amountPhaseStored, 0D);
	}
	
	public void testInventoryAmountResourceTypeStoreDeep() throws Exception {
		Inventory inventory = new Inventory(null);
		AmountResource hydrogen = AmountResource.findAmountResource(HYDROGEN);
		inventory.addGeneralCapacity(110D);
		Unit testUnit = new MockUnit1();
		testUnit.getInventory().addAmountResourceTypeCapacity(hydrogen, 100D);
		inventory.storeUnit(testUnit);
		inventory.storeAmountResource(hydrogen, 100D, true);
		double amountPhaseStored = inventory.getAmountResourceStored(hydrogen);
		assertEquals("Amount resource phase stored is correct.", 100D, amountPhaseStored, 0D);
	}
	
	public void testInventoryAmountResourceTypeStoreDeepOverload() throws Exception {
		try {
			Unit testUnit1 = new MockUnit1();
			testUnit1.getInventory().addGeneralCapacity(20D);
			Unit testUnit2 = new MockUnit1();
			AmountResource hydrogen = AmountResource.findAmountResource(HYDROGEN);
			testUnit2.getInventory().addAmountResourceTypeCapacity(hydrogen, 100D);
			testUnit1.getInventory().storeUnit(testUnit2);
			testUnit2.getInventory().storeAmountResource(hydrogen, 100D, true);
			fail("Fails properly when parent unit's general capacity is overloaded.");	
		}
		catch (Exception e) {}
	}
	
	public void testInventoryAmountResourceRemainingCapacityGood() throws Exception {
		Inventory inventory = new Inventory(null);
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		inventory.addAmountResourceTypeCapacity(carbonDioxide, 50D);
		inventory.addAmountResourcePhaseCapacity(Phase.GAS, 50D);
		inventory.storeAmountResource(carbonDioxide, 60D, true);
		double remainingCapacity = inventory.getAmountResourceRemainingCapacity(AmountResource.findAmountResource(CARBON_DIOXIDE), true);
		assertEquals("Amount type capacity remaining is correct amount.", 40D, remainingCapacity, 0D);
	}
	
	public void testInventoryAmountResourceRemainingCapacityMultiple() throws Exception {
		Inventory inventory = new Inventory(null);
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		AmountResource methane = AmountResource.findAmountResource(METHANE);
		inventory.addAmountResourceTypeCapacity(carbonDioxide, 40D);
		inventory.addAmountResourceTypeCapacity(methane, 20D);
		inventory.getAmountResourceRemainingCapacity(methane, true);
		double remainingCapacity = inventory.getAmountResourceRemainingCapacity(AmountResource.findAmountResource(CARBON_DIOXIDE), true);
		assertEquals("Amount type capacity remaining is correct amount.", 40D, remainingCapacity, 0D);
	}
	
	public void testInventoryAmountResourceTypeRemainingCapacityNoCapacity() throws Exception {
		Inventory inventory = new Inventory(null);
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		double remainingCapacity = inventory.getAmountResourceRemainingCapacity(carbonDioxide, true);
		assertEquals("Amount type capacity remaining is correct amount.", 0D, remainingCapacity, 0D);
	}
	
	public void testInventoryAmountResourceRetrieveGood() throws Exception {
		Inventory inventory = new Inventory(null);
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		inventory.addAmountResourceTypeCapacity(carbonDioxide, 50D);
		inventory.addAmountResourcePhaseCapacity(Phase.GAS, 50D);
		inventory.storeAmountResource(carbonDioxide, 100D, true);
		inventory.retrieveAmountResource(carbonDioxide, 50D);
		double remainingCapacity = inventory.getAmountResourceRemainingCapacity(AmountResource.findAmountResource(CARBON_DIOXIDE), true);
		assertEquals("Amount type capacity remaining is correct amount.", 50D, remainingCapacity, 0D);
	}
	
	public void testInventoryAmountResourceRetrieveTooMuch() throws Exception {
		try {
			Inventory inventory = new Inventory(null);
			AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
			inventory.addAmountResourceTypeCapacity(carbonDioxide, 50D);
			inventory.addAmountResourcePhaseCapacity(Phase.GAS, 50D);
			inventory.storeAmountResource(carbonDioxide, 100D, true);
			inventory.retrieveAmountResource(carbonDioxide, 101D);
			fail("Amount type retrieved fails correctly.");
		}
		catch (Exception e) {}
	}
	
	public void testInventoryAmountResourceRetrieveNegative() throws Exception {
		try {
			Inventory inventory = new Inventory(null);
			AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
			inventory.addAmountResourceTypeCapacity(carbonDioxide, 50D);
			inventory.addAmountResourcePhaseCapacity(Phase.GAS, 50D);
			inventory.storeAmountResource(carbonDioxide, 100D, true);
			inventory.retrieveAmountResource(carbonDioxide, -100D);
			fail("Amount type retrieved fails correctly.");
		}
		catch (Exception e) {}
	}
	
	public void testInventoryAmountResourceRetrieveNoCapacity() throws Exception {
		try {
			Inventory inventory = new Inventory(null);
			AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
			inventory.retrieveAmountResource(carbonDioxide, 100D);
			fail("Amount type retrieved fails correctly.");
		}
		catch (Exception e) {}
	}
	
	public void testInventoryAmountResourcePhaseRetrieveDeep() throws Exception {
		Inventory inventory = new Inventory(null);
		AmountResource hydrogen = AmountResource.findAmountResource(HYDROGEN);
		inventory.addGeneralCapacity(110D);
		Unit testUnit = new MockUnit1();
		testUnit.getInventory().addAmountResourcePhaseCapacity(Phase.GAS, 100D);
		inventory.storeUnit(testUnit);
		inventory.storeAmountResource(hydrogen, 100D, true);
		inventory.retrieveAmountResource(hydrogen, 50D);
		double remainingCapacity = inventory.getAmountResourceRemainingCapacity(hydrogen, true);
		assertEquals("Amount type capacity remaining is correct amount.", 50D, remainingCapacity, 0D);
	}
	
	public void testInventoryAmountResourceTypeRetrieveDeep() throws Exception {
		Inventory inventory = new Inventory(null);
		AmountResource hydrogen = AmountResource.findAmountResource(HYDROGEN);
		inventory.addGeneralCapacity(110D);
		Unit testUnit = new MockUnit1();
		testUnit.getInventory().addAmountResourceTypeCapacity(hydrogen, 100D);
		inventory.storeUnit(testUnit);
		inventory.storeAmountResource(hydrogen, 100D, true);
		inventory.retrieveAmountResource(hydrogen, 50D);
		double remainingCapacity = inventory.getAmountResourceRemainingCapacity(hydrogen, true);
		assertEquals("Amount type capacity remaining is correct amount.", 50D, remainingCapacity, 0D);
	}
	
	public void testInventoryAmountResourceAllResources() throws Exception {
		Inventory inventory = new Inventory(null);
		AmountResource hydrogen = AmountResource.findAmountResource(HYDROGEN);
		AmountResource food = AmountResource.findAmountResource(FOOD);
		inventory.addGeneralCapacity(110D);
		inventory.addAmountResourcePhaseCapacity(Phase.GAS, 20D);
		inventory.addAmountResourceTypeCapacity(food, 30D);
		Unit testUnit = new MockUnit1();
		testUnit.getInventory().addAmountResourceTypeCapacity(hydrogen, 100D);
		inventory.storeUnit(testUnit);
		inventory.storeAmountResource(hydrogen, 120D, true);
		inventory.storeAmountResource(food, 30D, true);
		Set resources = inventory.getAllAmountResourcesStored();
		assertEquals("Number of resources is correct.", 2, resources.size());
		assertTrue("Resources contains hydrogen", resources.contains(hydrogen));
		assertTrue("Resources contains food", resources.contains(food));
	}
	
	public void testInventoryAddGeneralCapacity() throws Exception {
		Inventory inventory = new Inventory(null);
		inventory.addGeneralCapacity(100D);
		assertEquals("General capacity set correctly.", 100D, inventory.getGeneralCapacity(), 0D);
	}
	
	public void testInventoryItemResourceStoreGood() throws Exception {
		ItemResource pipeWrench = ItemResource.getTestResourcePipeWrench();
		Inventory inventory = new Inventory(null);
		inventory.addGeneralCapacity(50D);
		inventory.storeItemResources(pipeWrench, 20);
		int storedResource = inventory.getItemResourceNum(pipeWrench);
		assertEquals("Item resources correct number.", 20, storedResource);
		double storedMass = inventory.getGeneralStoredMass();
		assertEquals("Item resources correct mass.", 50D, storedMass, 0D);
	}
	
	public void testInventoryItemResourceStoreDeep() throws Exception {
		ItemResource pipeWrench = ItemResource.getTestResourcePipeWrench();
		Inventory inventory = new Inventory(null);
		inventory.addGeneralCapacity(60D);
		Unit testUnit = new MockUnit1();
		testUnit.getInventory().addGeneralCapacity(50D);
		inventory.storeUnit(testUnit);
		testUnit.getInventory().storeItemResources(pipeWrench, 20);
		int storedResource = inventory.getItemResourceNum(pipeWrench);
		assertEquals("Item resources correct number.", 20, storedResource);
		double storedMass = inventory.getGeneralStoredMass();
		assertEquals("Item resources correct mass.", 60D, storedMass, 0D);
	}
	
	public void testInventoryItemResourceStoreOverload() throws Exception {
		ItemResource pipeWrench = ItemResource.getTestResourcePipeWrench();
		Inventory inventory = new Inventory(null);
		inventory.addGeneralCapacity(50D);
		try {
			inventory.storeItemResources(pipeWrench, 21);
			fail("Throws exception if overloaded");
		}
		catch (Exception e) {}
	}
	
	public void testInventoryItemResourceStoreNegativeNumber() throws Exception {
		ItemResource pipeWrench = ItemResource.getTestResourcePipeWrench();
		Inventory inventory = new Inventory(null);
		inventory.addGeneralCapacity(50D);
		try {
			inventory.storeItemResources(pipeWrench, -1);
			fail("Throws exception if negative number");
		}
		catch (Exception e) {}
	}
	
	public void testInventoryItemResourceStoreNoCapacity() throws Exception {
		ItemResource pipeWrench = ItemResource.getTestResourcePipeWrench();
		Inventory inventory = new Inventory(null);
		try {
			inventory.storeItemResources(pipeWrench, 1);
			fail("Throws exception if capacity not set (overloaded)");
		}
		catch (Exception e) {}
	}
	
	public void testInventoryItemResourceStoreDeepOverload() throws Exception {
		try {
			ItemResource pipeWrench = ItemResource.getTestResourcePipeWrench();
			Unit testUnit1 = new MockUnit1();
			testUnit1.getInventory().addGeneralCapacity(50D);
			Unit testUnit2 = new MockUnit2();
			testUnit2.getInventory().addGeneralCapacity(100D);
			testUnit1.getInventory().storeUnit(testUnit2);
			testUnit2.getInventory().storeItemResources(pipeWrench, 21);
			fail("Fails properly when parent unit's general capacity is overloaded.");	
		}
		catch (Exception e) {}
	}
	
	public void testInventoryItemResourceRemainingCapacityGood() throws Exception {
		ItemResource pipeWrench = ItemResource.getTestResourcePipeWrench();
		Inventory inventory = new Inventory(null);
		inventory.addGeneralCapacity(50D);
		inventory.storeItemResources(pipeWrench, 10);
		double remainingCapacity = inventory.getRemainingGeneralCapacity();
		assertEquals("Remaining capacity is correct.", 25D, remainingCapacity, 0D);
	}
	
	public void testInventoryItemResourceRemainingCapacityNoCapacity() throws Exception {
		Inventory inventory = new Inventory(null);
		double remainingCapacity = inventory.getRemainingGeneralCapacity();
		assertEquals("General capacity remaining is correct.", 0D, remainingCapacity, 0D);
	}
	
	public void testInventoryItemResourceRetrieveGood() throws Exception {
		ItemResource pipeWrench = ItemResource.getTestResourcePipeWrench();
		Inventory inventory = new Inventory(null);
		inventory.addGeneralCapacity(50D);
		inventory.storeItemResources(pipeWrench, 10);
		inventory.retrieveItemResources(pipeWrench, 5);
		int remainingNum = inventory.getItemResourceNum(pipeWrench);
		assertEquals("Item resource remaining is correct number.", 5, remainingNum);
	}
	
	public void testInventoryItemResourceRetrieveDeep() throws Exception {
		ItemResource pipeWrench = ItemResource.getTestResourcePipeWrench();
		Inventory inventory = new Inventory(null);
		inventory.addGeneralCapacity(60D);
		Unit testUnit = new MockUnit1();
		inventory.storeUnit(testUnit);
		testUnit.getInventory().addGeneralCapacity(50D);
		testUnit.getInventory().storeItemResources(pipeWrench, 10);
		inventory.retrieveItemResources(pipeWrench, 5);
		int remainingNum = inventory.getItemResourceNum(pipeWrench);
		assertEquals("Item resource remaining is correct number.", 5, remainingNum);
	}
	
	public void testInventoryItemResourceRetrieveTooMuch() throws Exception {
		try {
			ItemResource pipeWrench = ItemResource.getTestResourcePipeWrench();
			Inventory inventory = new Inventory(null);
			inventory.addGeneralCapacity(50D);
			inventory.storeItemResources(pipeWrench, 10);
			inventory.retrieveItemResources(pipeWrench, 11);
			fail("Item resource retrieved fails correctly.");
		}
		catch (Exception e) {}
	}
	
	public void testInventoryItemResourceRetrieveNegative() throws Exception {
		try {
			ItemResource pipeWrench = ItemResource.getTestResourcePipeWrench();
			Inventory inventory = new Inventory(null);
			inventory.addGeneralCapacity(50D);
			inventory.storeItemResources(pipeWrench, 10);
			inventory.retrieveItemResources(pipeWrench, -1);
			fail("Item resource retrieved fails correctly.");
		}
		catch (Exception e) {}
	}
	
	public void testInventoryItemResourceRetrieveNoItem() throws Exception {
		try {
			ItemResource pipeWrench = ItemResource.getTestResourcePipeWrench();
			Inventory inventory = new Inventory(null);
			inventory.retrieveItemResources(pipeWrench, 1);
			fail("Item resource retrieved fails correctly.");
		}
		catch (Exception e) {}
	}
	
	public void testInventoryUnitStoreGood() throws Exception {
		Inventory inventory = new Inventory(null);
		inventory.addGeneralCapacity(10D);
		Unit testUnit = new MockUnit1();
		inventory.storeUnit(testUnit);
	}
	
	public void testInventoryUnitStoredDuplicate() throws Exception {
		try {
			Inventory inventory = new Inventory(null);
			inventory.addGeneralCapacity(20D);
			Unit testUnit = new MockUnit1();
			inventory.storeUnit(testUnit);
			inventory.storeUnit(testUnit);
			fail("Duplicate unit stored fails correctly.");
		}
		catch (Exception e) {}
	}
	
	public void testInventoryUnitStoredNoCapacity() throws Exception {
		try {
			Inventory inventory = new Inventory(null);
			Unit testUnit = new MockUnit1();
			inventory.storeUnit(testUnit);
			fail("Unit stored with insufficient capacity fails correctly.");
		}
		catch (Exception e) {}
	}
	
	public void testInventoryUnitStoredUnitContains() throws Exception {
		try {
			Unit testUnit1 = new MockUnit1();
			testUnit1.getInventory().addGeneralCapacity(10D);
			Unit testUnit2 = new MockUnit1();
			testUnit2.getInventory().addGeneralCapacity(10D);
			testUnit1.getInventory().storeUnit(testUnit2);
			testUnit2.getInventory().storeUnit(testUnit1);
			fail("Unit cannot store another unit that stores it.");
		}
		catch (Exception e) {}
	}
	
	public void testInventoryUnitStoredItself() throws Exception {
		try {
			Unit testUnit1 = new MockUnit1();
			testUnit1.getInventory().addGeneralCapacity(10D);
			testUnit1.getInventory().storeUnit(testUnit1);
			fail("Unit cannot store itself.");
		}
		catch (Exception e) {}
	}
	
	public void testInventoryUnitStoredNull() throws Exception {
		try {
			Inventory inventory = new Inventory(null);
			inventory.addGeneralCapacity(10D);
			inventory.storeUnit(null);
			fail("Unit cannot store null unit.");
		}
		catch (Exception e) {}
	}
	
	public void testInventoryUnitStoreDeepOverload() throws Exception {
		try {
			Unit testUnit1 = new MockUnit1();
			testUnit1.getInventory().addGeneralCapacity(30D);
			Unit testUnit2 = new MockUnit2();
			testUnit2.getInventory().addGeneralCapacity(100D);
			testUnit1.getInventory().storeUnit(testUnit2);
			Unit testUnit3 = new MockUnit2();
			testUnit2.getInventory().storeUnit(testUnit3);
			fail("Fails properly when parent unit's general capacity is overloaded.");	
		}
		catch (Exception e) {}
	}
	
	public void testInventoryGetTotalUnitMassStored() throws Exception {
		Inventory inventory = new Inventory(null);
		inventory.addGeneralCapacity(50D);
		Unit testUnit1 = new MockUnit1();
		testUnit1.getInventory().addGeneralCapacity(20D);
		Unit testUnit2 = new MockUnit2();
		testUnit1.getInventory().storeUnit(testUnit2);
		inventory.storeUnit(testUnit1);
		double totalMass = inventory.getUnitTotalMass();
		assertEquals("Correct total unit mass.", 30D, totalMass, 0D);	
	}
	
	public void testInventoryGetContainedUnits() throws Exception {
		Inventory inventory = new Inventory(null);
		inventory.addGeneralCapacity(30D);
		Unit testUnit1 = new MockUnit1();
		inventory.storeUnit(testUnit1);
		Unit testUnit2 = new MockUnit2();
		inventory.storeUnit(testUnit2);
		int numUnits = inventory.getContainedUnits().size();
		assertEquals("Correct number of units stored.", 2, numUnits);
	}
	
	public void testInventoryContainsUnitGood() throws Exception {
		Inventory inventory = new Inventory(null);
		inventory.addGeneralCapacity(10D);
		Unit testUnit = new MockUnit1();
		inventory.storeUnit(testUnit);
		assertTrue("Contains test unit.", inventory.containsUnit(testUnit));
	}
	
	public void testInventoryContainsUnitFail() throws Exception {
		Inventory inventory = new Inventory(null);
		inventory.addGeneralCapacity(10D);
		Unit testUnit = new MockUnit1();
		assertTrue("Does not contain unit", !inventory.containsUnit(testUnit));
	}
	
	public void testInventoryContainsUnitClassGood() throws Exception {
		Inventory inventory = new Inventory(null);
		inventory.addGeneralCapacity(10D);
		Unit testUnit = new MockUnit1();
		inventory.storeUnit(testUnit);
		assertTrue("Contains MockUnit1 class.", inventory.containsUnitClass(MockUnit1.class));
	}
	
	public void testInventoryContainsUnitClassFail() throws Exception {
		Inventory inventory = new Inventory(null);
		inventory.addGeneralCapacity(10D);
		assertTrue("Does not contain MockUnit1 class", !inventory.containsUnitClass(MockUnit1.class));
	}
	
	public void testInventoryFindUnitGood() throws Exception {
		Inventory inventory = new Inventory(null);
		inventory.addGeneralCapacity(10D);
		Unit testUnit = new MockUnit1();
		inventory.storeUnit(testUnit);
		Unit found = inventory.findUnitOfClass(MockUnit1.class);
		assertEquals("Found unit correctly.", testUnit, found);
	}
	
	public void testInventoryFindUnitFail() throws Exception {
		Inventory inventory = new Inventory(null);
		inventory.addGeneralCapacity(10D);
		Unit found = inventory.findUnitOfClass(MockUnit1.class);
		assertEquals("Could not find unit of class.", null, found);
	}
	
	public void testInventoryFindAllUnitsGood() throws Exception {
		Inventory inventory = new Inventory(null);
		inventory.addGeneralCapacity(20D);
		Unit testUnit1 = new MockUnit1();
		Unit testUnit2 = new MockUnit1();
		inventory.storeUnit(testUnit1);
		inventory.storeUnit(testUnit2);
		Collection units = inventory.findAllUnitsOfClass(MockUnit1.class);
		assertEquals("Found correct number of units.", 2, units.size());
		assertTrue("Found test unit 1", units.contains(testUnit1));
		assertTrue("Found test unit 2", units.contains(testUnit2));
	}
	
	public void testInventoryFindAllUnitsFail() throws Exception {
		Inventory inventory = new Inventory(null);
		inventory.addGeneralCapacity(20D);
		Collection units = inventory.findAllUnitsOfClass(MockUnit1.class);
		assertEquals("Could not fine units of class", 0, units.size());
	}
	
	public void testInventoryFindNumUnitsGood() throws Exception {
		Inventory inventory = new Inventory(null);
		inventory.addGeneralCapacity(20D);
		Unit testUnit1 = new MockUnit1();
		Unit testUnit2 = new MockUnit1();
		inventory.storeUnit(testUnit1);
		inventory.storeUnit(testUnit2);
		int numUnits = inventory.findNumUnitsOfClass(MockUnit1.class);
		assertEquals("Found correct number of units.", 2, numUnits);
	}
	
	public void testInventoryFindNumUnitsFail() throws Exception {
		Inventory inventory = new Inventory(null);
		inventory.addGeneralCapacity(20D);
		int numUnits = inventory.findNumUnitsOfClass(MockUnit1.class);
		assertEquals("Could not fine units of class", 0, numUnits);
	}
	
	public void testInventoryRetrieveUnitGood() throws Exception {
		Inventory inventory = new Inventory(null);
		inventory.addGeneralCapacity(10D);
		Unit testUnit = new MockUnit1();
		inventory.storeUnit(testUnit);
		inventory.retrieveUnit(testUnit);
	}
	
	public void testInventoryRetrieveUnitBad() throws Exception {
		try {
			Inventory inventory = new Inventory(null);
			inventory.addGeneralCapacity(10D);
			Unit testUnit = new MockUnit1();
			inventory.retrieveUnit(testUnit);
			fail("testUnit not found.");
		}
		catch (Exception e) {}
	}
	
	public void testInventoryRetrieveUnitDouble() throws Exception {
		try {
			Inventory inventory = new Inventory(null);
			inventory.addGeneralCapacity(10D);
			Unit testUnit = new MockUnit1();
			inventory.storeUnit(testUnit);
			inventory.retrieveUnit(testUnit);
			inventory.retrieveUnit(testUnit);
			fail("testUnit retrieved twice.");
		}
		catch (Exception e) {}
	}
}