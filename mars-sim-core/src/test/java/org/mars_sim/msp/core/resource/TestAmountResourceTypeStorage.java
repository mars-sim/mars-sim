package org.mars_sim.msp.core.resource;

import java.util.Set;

import org.mars_sim.msp.core.SimulationConfig;

import junit.framework.TestCase;

public class TestAmountResourceTypeStorage extends TestCase {

	private static final String CARBON_DIOXIDE = "carbon dioxide";
	private static final String OXYGEN = "oxygen";
	
	public TestAmountResourceTypeStorage() {
		super();
		SimulationConfig.instance();
	}
	
	public void testInventoryAmountResourceTypeCapacityGood() throws Exception {
		AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
		double amountCO2 = storage.getAmountResourceTypeCapacity(carbonDioxide);
		assertEquals("Amount resource type capacity set correctly.", 100D, amountCO2, 0D);
	}
	
	public void testInventoryAmountResourceTypeCapacityNotSet() throws Exception {
		AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		double amountCO2 = storage.getAmountResourceTypeCapacity(carbonDioxide);
		assertEquals("Amount resource type capacity set correctly.", 0D, amountCO2, 0D);
	}
	
	public void testInventoryAmountResourceTypeCapacityNegativeCapacity() throws Exception {
		AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		try {
			storage.addAmountResourceTypeCapacity(carbonDioxide, -100D);
			fail("Cannot add negative capacity for a type.");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourceTypeStoreGood() throws Exception {
		AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
		storage.storeAmountResourceType(carbonDioxide, 100D);
		double amountTypeStored = storage.getAmountResourceTypeStored(carbonDioxide);
		assertEquals("Amount resource type stored is correct.", 100D, amountTypeStored, 0D);
	}
	
	public void testInventoryAmountResourceTypeStoreOverload() throws Exception {
		AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
		try {
			storage.storeAmountResourceType(carbonDioxide, 101D);
			fail("Throws exception if overloaded");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourceTypeStoreNegativeAmount() throws Exception {
		AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
		try {
			storage.storeAmountResourceType(carbonDioxide, -1D);
			fail("Throws exception if negative amount");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourceTypeStoreNoCapacity() throws Exception {
		AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		try {
			storage.storeAmountResourceType(carbonDioxide, 100D);
			fail("Throws exception if capacity not set (overloaded)");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourceTypeRemainingCapacityGood() throws Exception {
		AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
		storage.storeAmountResourceType(carbonDioxide, 50D);
		double remainingCapacity = storage.getAmountResourceTypeRemainingCapacity(carbonDioxide);
		assertEquals("Amount type capacity remaining is correct amount.", 50D, remainingCapacity, 0D);
	}
	
	public void testInventoryAmountResourceTypeRemainingCapacityNoCapacity() throws Exception {
		AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		double remainingCapacity = storage.getAmountResourceTypeRemainingCapacity(carbonDioxide);
		assertEquals("Amount type capacity remaining is correct amount.", 0D, remainingCapacity, 0D);
	}
	
	public void testInventoryAmountResourceTypeRetrieveGood() throws Exception {
		AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
		storage.storeAmountResourceType(carbonDioxide, 100D);
		storage.retrieveAmountResourceType(carbonDioxide, 50D);
		double remainingCapacity = storage.getAmountResourceTypeRemainingCapacity(carbonDioxide);
		assertEquals("Amount type capacity remaining is correct amount.", 50D, remainingCapacity, 0D);
	}
	
	public void testInventoryAmountResourceTypeRetrieveTooMuch() throws Exception {
		try {
			AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
			AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
			storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
			storage.storeAmountResourceType(carbonDioxide, 100D);
			storage.retrieveAmountResourceType(carbonDioxide, 101D);
			fail("Amount type retrieved fails correctly.");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourceTypeRetrieveNegative() throws Exception {
		try {
			AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
			AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
			storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
			storage.storeAmountResourceType(carbonDioxide, 100D);
			storage.retrieveAmountResourceType(carbonDioxide, -100D);
			fail("Amount type retrieved fails correctly.");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourceTypeRetrieveNoCapacity() throws Exception {
		try {
			AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
			AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
			storage.retrieveAmountResourceType(carbonDioxide, 100D);
			fail("Amount type retrieved fails correctly.");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourceTypeTotalAmount() throws Exception {
		AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		AmountResource oxygen = AmountResource.findAmountResource(OXYGEN);
		storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
		storage.addAmountResourceTypeCapacity(oxygen, 100D);
		storage.storeAmountResourceType(carbonDioxide, 10D);
		storage.storeAmountResourceType(oxygen, 20D);
		double totalStored = storage.getTotalAmountResourceTypesStored();
		assertEquals("Amount total stored is correct.", 30D, totalStored, 0D);
	}
	
	public void testInventoryAmountResourceTypeAllStored() throws Exception {
		AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
		AmountResource carbonDioxide = AmountResource.findAmountResource(CARBON_DIOXIDE);
		AmountResource oxygen = AmountResource.findAmountResource(OXYGEN);
		storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
		storage.addAmountResourceTypeCapacity(oxygen, 100D);
		storage.storeAmountResourceType(carbonDioxide, 10D);
		storage.storeAmountResourceType(oxygen, 20D);
		Set allResources = storage.getAllAmountResourcesStored();
		assertTrue("All resources contains carbon dioxide.", allResources.contains(carbonDioxide));
		assertTrue("All resources contains oxygen.", allResources.contains(oxygen));
	}
}