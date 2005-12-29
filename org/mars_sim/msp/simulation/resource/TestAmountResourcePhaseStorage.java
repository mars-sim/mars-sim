package org.mars_sim.msp.simulation.resource;

import junit.framework.TestCase;

public class TestAmountResourcePhaseStorage extends TestCase {

	public TestAmountResourcePhaseStorage() {
		super();
	}
	
	public void testInventoryAmountResourcePhaseCapacityGood() throws Exception {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		storage.addAmountResourcePhaseCapacity(Phase.GAS, 100D);
		double amountGas = storage.getAmountResourcePhaseCapacity(Phase.GAS);
		assertEquals("Amount resource phase capacity set correctly.", 100D, amountGas, 0D);
	}
	
	public void testInventoryAmountResourcePhaseCapacityNotSet() throws Exception {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		double amountGas = storage.getAmountResourcePhaseCapacity(Phase.GAS);
		assertEquals("Amount resource phase capacity set correctly.", 0D, amountGas, 0D);
	}
	
	public void testInventoryAmountResourcePhaseCapacityNegativeCapacity() throws Exception {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		try {
			storage.addAmountResourcePhaseCapacity(Phase.GAS, -100D);
			fail("Cannot add negative capacity for a phase.");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourcePhaseStoreGood() throws Exception {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		storage.addAmountResourcePhaseCapacity(Phase.GAS, 100D);
		storage.storeAmountResourcePhase(AmountResource.HYDROGEN, 100D);
		double amountPhaseStored = storage.getAmountResourcePhaseStored(Phase.GAS);
		assertEquals("Amount resource phase stored is correct.", 100D, amountPhaseStored, 0D);
	}
	
	public void testInventoryAmountResourcePhaseStoreOverload() throws Exception {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		storage.addAmountResourcePhaseCapacity(Phase.GAS, 100D);
		try {
			storage.storeAmountResourcePhase(AmountResource.HYDROGEN, 101D);
			fail("Throws exception if overloaded");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourcePhaseStoreNegativeAmount() throws Exception {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		storage.addAmountResourcePhaseCapacity(Phase.GAS, 100D);
		try {
			storage.storeAmountResourcePhase(AmountResource.HYDROGEN, -1D);
			fail("Throws exception if negative amount");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourcePhaseStoreNoCapacity() throws Exception {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		try {
			storage.storeAmountResourcePhase(AmountResource.HYDROGEN, 100D);
			fail("Throws exception if capacity not set (overloaded)");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourcePhaseTypeGood() throws Exception {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		storage.addAmountResourcePhaseCapacity(Phase.GAS, 100D);
		storage.storeAmountResourcePhase(AmountResource.HYDROGEN, 100D);
		AmountResource resource = storage.getAmountResourcePhaseType(Phase.GAS);
		assertEquals("Amount phase stored is of correct type.", AmountResource.HYDROGEN, resource);
	}
	
	public void testInventoryAmountResourcePhaseTypeNoResource() throws Exception {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		storage.addAmountResourcePhaseCapacity(Phase.GAS, 100D);
		AmountResource resource = storage.getAmountResourcePhaseType(Phase.GAS);
		assertNull("Amount phase type stored is null.", resource);
	}
	
	public void testInventoryAmountResourcePhaseRemainingCapacityGood() throws Exception {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		storage.addAmountResourcePhaseCapacity(Phase.GAS, 100D);
		storage.storeAmountResourcePhase(AmountResource.HYDROGEN, 50D);
		double remainingCapacity = storage.getAmountResourcePhaseRemainingCapacity(Phase.GAS);
		assertEquals("Amount phase capacity remaining is correct amount.", 50D, remainingCapacity, 0D);
	}
	
	public void testInventoryAmountResourcePhaseRemainingCapacityNoCapacity() throws Exception {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		double remainingCapacity = storage.getAmountResourcePhaseRemainingCapacity(Phase.GAS);
		assertEquals("Amount phase capacity remaining is correct amount.", 0D, remainingCapacity, 0D);
	}
	
	public void testInventoryAmountResourcePhaseRetrieveGood() throws Exception {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		storage.addAmountResourcePhaseCapacity(Phase.GAS, 100D);
		storage.storeAmountResourcePhase(AmountResource.HYDROGEN, 100D);
		storage.retrieveAmountResourcePhase(Phase.GAS, 50D);
		double remainingCapacity = storage.getAmountResourcePhaseRemainingCapacity(Phase.GAS);
		assertEquals("Amount phase capacity remaining is correct amount.", 50D, remainingCapacity, 0D);
	}
	
	public void testInventoryAmountResourcePhaseRetrieveTooMuch() throws Exception {
		try {
			AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
			storage.addAmountResourcePhaseCapacity(Phase.GAS, 100D);
			storage.storeAmountResourcePhase(AmountResource.HYDROGEN, 100D);
			storage.retrieveAmountResourcePhase(Phase.GAS, 101D);
			fail("Amount phase retrieved fails correctly.");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourcePhaseRetrieveNegative() throws Exception {
		try {
			AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
			storage.addAmountResourcePhaseCapacity(Phase.GAS, 100D);
			storage.storeAmountResourcePhase(AmountResource.HYDROGEN, 100D);
			storage.retrieveAmountResourcePhase(Phase.GAS, -100D);
			fail("Amount phase retrieved fails correctly.");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourcePhaseRetrieveNoCapacity() throws Exception {
		try {
			AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
			storage.retrieveAmountResourcePhase(Phase.GAS, 100D);
			fail("Amount phase retrieved fails correctly.");
		}
		catch (ResourceException e) {}
	}
	
	public void testInventoryAmountResourcePhaseTotalAmount() throws Exception {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		storage.addAmountResourcePhaseCapacity(Phase.GAS, 100D);
		storage.addAmountResourcePhaseCapacity(Phase.LIQUID, 100D);
		storage.storeAmountResourcePhase(AmountResource.OXYGEN, 10D);
		storage.storeAmountResourcePhase(AmountResource.WATER, 20D);
		double totalStored = storage.getTotalAmountResourcePhasesStored();
		assertEquals("Amount total stored is correct.", 30D, totalStored, 0D);
	}
}