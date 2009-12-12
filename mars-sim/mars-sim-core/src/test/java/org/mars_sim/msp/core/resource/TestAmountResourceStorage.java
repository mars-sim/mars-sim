package org.mars_sim.msp.core.resource;

import java.util.Set;
import junit.framework.TestCase;

public class TestAmountResourceStorage extends TestCase {

	private static final String CARBON_DIOXIDE = "carbon dioxide";
	private static final String HYDROGEN = "hydrogen";
	private static final String WATER = "water";
	
	public TestAmountResourceStorage() {
		super();
	}
	
	public void testInventoryAmountResourceTypeCapacityGood() throws Exception {
		AmountResourceStorage storage = new AmountResourceStorage();
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
		double amountCO2 = storage.getAmountResourceCapacity(carbonDioxide);
		assertEquals("Amount resource type capacity set correctly.", 100D, amountCO2, 0D);
	}
	
	public void testInventoryAmountResourceTypeCapacityNegativeCapacity() throws Exception {
		AmountResourceStorage storage = new AmountResourceStorage();
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		try {
			storage.addAmountResourceTypeCapacity(carbonDioxide, -100D);
			fail("Cannot add negative capacity for a type.");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourcePhaseCapacityGood() throws Exception {
		AmountResourceStorage storage = new AmountResourceStorage();
		storage.addAmountResourcePhaseCapacity(Phase.GAS, 100D);
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		double amountCO2 = storage.getAmountResourceCapacity(carbonDioxide);
		assertEquals("Amount resource type capacity set correctly.", 100D, amountCO2, 0D);
	}
	
	public void testInventoryAmountResourcePhaseCapacityNegativeCapacity() throws Exception {
		AmountResourceStorage storage = new AmountResourceStorage();
		try {
			storage.addAmountResourcePhaseCapacity(Phase.GAS, -100D);
			fail("Cannot add negative capacity for a phase.");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourceComboCapacityGood() throws Exception {
		AmountResourceStorage storage = new AmountResourceStorage();
		storage.addAmountResourcePhaseCapacity(Phase.GAS, 50D);
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		storage.addAmountResourceTypeCapacity(carbonDioxide, 50D);
		double amountCO2 = storage.getAmountResourceCapacity(carbonDioxide);
		assertEquals("Amount resource type capacity set correctly.", 100D, amountCO2, 0D);
	}
	
	public void testInventoryAmountResourceCapacityNotSet() throws Exception {
		AmountResourceStorage storage = new AmountResourceStorage();
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		double amountCO2 = storage.getAmountResourceCapacity(carbonDioxide);
		assertEquals("Amount resource capacity set correctly.", 0D, amountCO2, 0D);
	}
	
	public void testInventoryAmountResourceTypeStoreGood() throws Exception {
		AmountResourceStorage storage = new AmountResourceStorage();
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
		storage.storeAmountResource(carbonDioxide, 100D);
		double amountTypeStored = storage.getAmountResourceStored(carbonDioxide);
		assertEquals("Amount resource type stored is correct.", 100D, amountTypeStored, 0D);
	}
	
	public void testInventoryAmountResourceTypeStoreOverload() throws Exception {
		AmountResourceStorage storage = new AmountResourceStorage();
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
		try {
			storage.storeAmountResource(carbonDioxide, 101D);
			fail("Throws exception if overloaded");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourcePhaseStoreGood() throws Exception {
		AmountResourceStorage storage = new AmountResourceStorage();
		storage.addAmountResourcePhaseCapacity(Phase.GAS, 100D);
		AmountResource hydrogen = AmountResource.findAmountResource(HYDROGEN);
		storage.storeAmountResource(hydrogen, 100D);
		double amountPhaseStored = storage.getAmountResourceStored(hydrogen);
		assertEquals("Amount resource phase stored is correct.", 100D, amountPhaseStored, 0D);
	}
	
	public void testInventoryAmountResourcePhaseStoreOverload() throws Exception {
		AmountResourceStorage storage = new AmountResourceStorage();
		storage.addAmountResourcePhaseCapacity(Phase.GAS, 100D);
		AmountResource hydrogen = AmountResource.findAmountResource(HYDROGEN);
		try {
			storage.storeAmountResource(hydrogen, 101D);
			fail("Throws exception if overloaded");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourceStoreNegativeAmount() throws Exception {
		AmountResourceStorage storage = new AmountResourceStorage();
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
		try {
			storage.storeAmountResource(carbonDioxide, -1D);
			fail("Throws exception if negative amount");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourceStoreNoCapacity() throws Exception {
		AmountResourceStorage storage = new AmountResourceStorage();
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		try {
			storage.storeAmountResource(carbonDioxide, 100D);
			fail("Throws exception if capacity not set (overloaded)");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourceRemainingCapacityGood() throws Exception {
		AmountResourceStorage storage = new AmountResourceStorage();
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		storage.addAmountResourceTypeCapacity(carbonDioxide, 50D);
		storage.addAmountResourcePhaseCapacity(Phase.GAS, 50D);
		storage.storeAmountResource(carbonDioxide, 60D);
		double remainingCapacity = storage.getAmountResourceRemainingCapacity(carbonDioxide);
		assertEquals("Amount type capacity remaining is correct amount.", 40D, remainingCapacity, 0D);
	}
	
	public void testInventoryAmountResourceTypeRemainingCapacityNoCapacity() throws Exception {
		AmountResourceStorage storage = new AmountResourceStorage();
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		double remainingCapacity = storage.getAmountResourceRemainingCapacity(carbonDioxide);
		assertEquals("Amount type capacity remaining is correct amount.", 0D, remainingCapacity, 0D);
	}
	
	public void testInventoryAmountResourceRetrieveGood() throws Exception {
		AmountResourceStorage storage = new AmountResourceStorage();
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		storage.addAmountResourceTypeCapacity(carbonDioxide, 50D);
		storage.addAmountResourcePhaseCapacity(Phase.GAS, 50D);
		storage.storeAmountResource(carbonDioxide, 100D);
		storage.retrieveAmountResource(carbonDioxide, 50D);
		double remainingCapacity = storage.getAmountResourceRemainingCapacity(carbonDioxide);
		assertEquals("Amount type capacity remaining is correct amount.", 50D, remainingCapacity, 0D);
	}
	
	public void testInventoryAmountResourceRetrieveTooMuch() throws Exception {
		try {
			AmountResourceStorage storage = new AmountResourceStorage();
			AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
			storage.addAmountResourceTypeCapacity(carbonDioxide, 50D);
			storage.addAmountResourcePhaseCapacity(Phase.GAS, 50D);
			storage.storeAmountResource(carbonDioxide, 100D);
			storage.retrieveAmountResource(carbonDioxide, 101D);
			fail("Amount type retrieved fails correctly.");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourceRetrieveNegative() throws Exception {
		try {
			AmountResourceStorage storage = new AmountResourceStorage();
			AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
			storage.addAmountResourceTypeCapacity(carbonDioxide, 50D);
			storage.addAmountResourcePhaseCapacity(Phase.GAS, 50D);
			storage.storeAmountResource(carbonDioxide, 100D);
			storage.retrieveAmountResource(carbonDioxide, -100D);
			fail("Amount type retrieved fails correctly.");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourceRetrieveNoCapacity() throws Exception {
		try {
			AmountResourceStorage storage = new AmountResourceStorage();
			AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
			storage.retrieveAmountResource(carbonDioxide, 100D);
			fail("Amount type retrieved fails correctly.");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourceTotalAmount() throws Exception {
		AmountResourceStorage storage = new AmountResourceStorage();
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		AmountResource water = AmountResource.findAmountResource(WATER);
		storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
		storage.addAmountResourcePhaseCapacity(Phase.LIQUID, 100D);
		storage.storeAmountResource(carbonDioxide, 10D);
		storage.storeAmountResource(water, 20D);
		double totalStored = storage.getTotalAmountResourcesStored();
		assertEquals("Amount total stored is correct.", 30D, totalStored, 0D);
	}
	
	public void testInventoryAmountResourceAllResources() throws Exception {
		AmountResourceStorage storage = new AmountResourceStorage();
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		AmountResource water = AmountResource.findAmountResource(WATER);
		storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
		storage.addAmountResourcePhaseCapacity(Phase.LIQUID, 100D);
		storage.storeAmountResource(carbonDioxide, 10D);
		storage.storeAmountResource(water, 20D);
		Set allResources = storage.getAllAmountResourcesStored();
		assertTrue("All resources contains carbon dioxide.", allResources.contains(carbonDioxide));
		assertTrue("All resources contains oxygen.", allResources.contains(water));
	}
}