package org.mars_sim.msp.simulation.resource;

import java.util.Set;
import junit.framework.TestCase;

public class TestAmountResourceTypeStorage extends TestCase {

	public TestAmountResourceTypeStorage() {
		super();
	}
	
	public void testInventoryAmountResourceTypeCapacityGood() throws Exception {
		AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
		storage.addAmountResourceTypeCapacity(AmountResource.CARBON_DIOXIDE, 100D);
		double amountCO2 = storage.getAmountResourceTypeCapacity(AmountResource.CARBON_DIOXIDE);
		assertEquals("Amount resource type capacity set correctly.", 100D, amountCO2, 0D);
	}
	
	public void testInventoryAmountResourceTypeCapacityNotSet() throws Exception {
		AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
		double amountCO2 = storage.getAmountResourceTypeCapacity(AmountResource.CARBON_DIOXIDE);
		assertEquals("Amount resource type capacity set correctly.", 0D, amountCO2, 0D);
	}
	
	public void testInventoryAmountResourceTypeCapacityNegativeCapacity() throws Exception {
		AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
		try {
			storage.addAmountResourceTypeCapacity(AmountResource.CARBON_DIOXIDE, -100D);
			fail("Cannot add negative capacity for a type.");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourceTypeStoreGood() throws Exception {
		AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
		storage.addAmountResourceTypeCapacity(AmountResource.CARBON_DIOXIDE, 100D);
		storage.storeAmountResourceType(AmountResource.CARBON_DIOXIDE, 100D);
		double amountTypeStored = storage.getAmountResourceTypeStored(AmountResource.CARBON_DIOXIDE);
		assertEquals("Amount resource type stored is correct.", 100D, amountTypeStored, 0D);
	}
	
	public void testInventoryAmountResourceTypeStoreOverload() throws Exception {
		AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
		storage.addAmountResourceTypeCapacity(AmountResource.CARBON_DIOXIDE, 100D);
		try {
			storage.storeAmountResourceType(AmountResource.CARBON_DIOXIDE, 101D);
			fail("Throws exception if overloaded");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourceTypeStoreNegativeAmount() throws Exception {
		AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
		storage.addAmountResourceTypeCapacity(AmountResource.CARBON_DIOXIDE, 100D);
		try {
			storage.storeAmountResourceType(AmountResource.CARBON_DIOXIDE, -1D);
			fail("Throws exception if negative amount");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourceTypeStoreNoCapacity() throws Exception {
		AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
		try {
			storage.storeAmountResourceType(AmountResource.CARBON_DIOXIDE, 100D);
			fail("Throws exception if capacity not set (overloaded)");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourceTypeRemainingCapacityGood() throws Exception {
		AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
		storage.addAmountResourceTypeCapacity(AmountResource.CARBON_DIOXIDE, 100D);
		storage.storeAmountResourceType(AmountResource.CARBON_DIOXIDE, 50D);
		double remainingCapacity = storage.getAmountResourceTypeRemainingCapacity(AmountResource.CARBON_DIOXIDE);
		assertEquals("Amount type capacity remaining is correct amount.", 50D, remainingCapacity, 0D);
	}
	
	public void testInventoryAmountResourceTypeRemainingCapacityNoCapacity() throws Exception {
		AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
		double remainingCapacity = storage.getAmountResourceTypeRemainingCapacity(AmountResource.CARBON_DIOXIDE);
		assertEquals("Amount type capacity remaining is correct amount.", 0D, remainingCapacity, 0D);
	}
	
	public void testInventoryAmountResourceTypeRetrieveGood() throws Exception {
		AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
		storage.addAmountResourceTypeCapacity(AmountResource.CARBON_DIOXIDE, 100D);
		storage.storeAmountResourceType(AmountResource.CARBON_DIOXIDE, 100D);
		storage.retrieveAmountResourceType(AmountResource.CARBON_DIOXIDE, 50D);
		double remainingCapacity = storage.getAmountResourceTypeRemainingCapacity(AmountResource.CARBON_DIOXIDE);
		assertEquals("Amount type capacity remaining is correct amount.", 50D, remainingCapacity, 0D);
	}
	
	public void testInventoryAmountResourceTypeRetrieveTooMuch() throws Exception {
		try {
			AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
			storage.addAmountResourceTypeCapacity(AmountResource.CARBON_DIOXIDE, 100D);
			storage.storeAmountResourceType(AmountResource.CARBON_DIOXIDE, 100D);
			storage.retrieveAmountResourceType(AmountResource.CARBON_DIOXIDE, 101D);
			fail("Amount type retrieved fails correctly.");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourceTypeRetrieveNegative() throws Exception {
		try {
			AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
			storage.addAmountResourceTypeCapacity(AmountResource.CARBON_DIOXIDE, 100D);
			storage.storeAmountResourceType(AmountResource.CARBON_DIOXIDE, 100D);
			storage.retrieveAmountResourceType(AmountResource.CARBON_DIOXIDE, -100D);
			fail("Amount type retrieved fails correctly.");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourceTypeRetrieveNoCapacity() throws Exception {
		try {
			AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
			storage.retrieveAmountResourceType(AmountResource.CARBON_DIOXIDE, 100D);
			fail("Amount type retrieved fails correctly.");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourceTypeTotalAmount() throws Exception {
		AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
		storage.addAmountResourceTypeCapacity(AmountResource.CARBON_DIOXIDE, 100D);
		storage.addAmountResourceTypeCapacity(AmountResource.OXYGEN, 100D);
		storage.storeAmountResourceType(AmountResource.CARBON_DIOXIDE, 10D);
		storage.storeAmountResourceType(AmountResource.OXYGEN, 20D);
		double totalStored = storage.getTotalAmountResourceTypesStored();
		assertEquals("Amount total stored is correct.", 30D, totalStored, 0D);
	}
	
	public void testInventoryAmountResourceTypeAllStored() throws Exception {
		AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
		storage.addAmountResourceTypeCapacity(AmountResource.CARBON_DIOXIDE, 100D);
		storage.addAmountResourceTypeCapacity(AmountResource.OXYGEN, 100D);
		storage.storeAmountResourceType(AmountResource.CARBON_DIOXIDE, 10D);
		storage.storeAmountResourceType(AmountResource.OXYGEN, 20D);
		Set allResources = storage.getAllAmountResourcesStored();
		assertTrue("All resources contains carbon dioxide.", allResources.contains(AmountResource.CARBON_DIOXIDE));
		assertTrue("All resources contains oxygen.", allResources.contains(AmountResource.OXYGEN));
	}
}