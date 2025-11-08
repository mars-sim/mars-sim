package com.mars_sim.core.resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;


import com.mars_sim.core.SimulationConfig;



public class TestAmountResourcePhaseStorage {
	
    @BeforeEach

	
    @BeforeEach


	
    public void setUp() {
        SimulationConfig.loadConfig();
    }
	
	@Test

	
	public void testInventoryAmountResourcePhaseCapacityGood() {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		storage.addAmountResourcePhaseCapacity(PhaseType.GAS, 100D);
		double amountGas = storage.getAmountResourcePhaseCapacity(PhaseType.GAS);
		assertEquals("Amount resource phase capacity set correctly.", 100D, amountGas, 0D);
	}
	
	@Test

	
	public void testInventoryAmountResourcePhaseCapacityNotSet() {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		double amountGas = storage.getAmountResourcePhaseCapacity(PhaseType.GAS);
		assertEquals("Amount resource phase capacity set correctly.", 0D, amountGas, 0D);
	}
	
	@Test

	
	public void testInventoryAmountResourcePhaseCapacityNegativeCapacity() {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		try {
			storage.addAmountResourcePhaseCapacity(PhaseType.GAS, -100D);
			fail("Cannot add negative capacity for a phase.");
		}
		catch (Exception e) {}
	}
	
	@Test

	
	public void testInventoryAmountResourcePhaseStoreGood() {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		storage.addAmountResourcePhaseCapacity(PhaseType.GAS, 100D);
		AmountResource hydrogen = ResourceUtil.findAmountResource(ResourceUtil.HYDROGEN_ID);
		storage.storeAmountResourcePhase(hydrogen, 100D);
		double amountPhaseStored = storage.getAmountResourcePhaseStored(PhaseType.GAS);
		assertEquals("Amount resource phase stored is correct.", 100D, amountPhaseStored, 0D);
	}
	
	@Test

	
	public void testInventoryAmountResourcePhaseStoreOverload() {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		storage.addAmountResourcePhaseCapacity(PhaseType.GAS, 100D);
		AmountResource hydrogen = ResourceUtil.findAmountResource(ResourceUtil.HYDROGEN_ID);
		try {
			storage.storeAmountResourcePhase(hydrogen, 101D);
			fail("Throws exception if overloaded");
		}
		catch (Exception e) {}
	}
	
	@Test

	
	public void testInventoryAmountResourcePhaseStoreNegativeAmount() {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		storage.addAmountResourcePhaseCapacity(PhaseType.GAS, 100D);
		AmountResource hydrogen = ResourceUtil.findAmountResource(ResourceUtil.HYDROGEN_ID);
		try {
			storage.storeAmountResourcePhase(hydrogen, -1D);
			fail("Throws exception if negative amount");
		}
		catch (Exception e) {}
	}
	
	@Test

	
	public void testInventoryAmountResourcePhaseStoreNoCapacity() {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		AmountResource hydrogen = ResourceUtil.findAmountResource(ResourceUtil.HYDROGEN_ID);
		try {
			storage.storeAmountResourcePhase(hydrogen, 100D);
			fail("Throws exception if capacity not set (overloaded)");
		}
		catch (Exception e) {}
	}
	
	@Test

	
	public void testInventoryAmountResourcePhaseTypeGood() {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		storage.addAmountResourcePhaseCapacity(PhaseType.GAS, 100D);
		AmountResource hydrogen = ResourceUtil.findAmountResource(ResourceUtil.HYDROGEN_ID);
		storage.storeAmountResourcePhase(hydrogen, 100D);
		AmountResource resource = storage.getAmountResourcePhaseType(PhaseType.GAS);
		assertEquals("Amount phase stored is of correct type.", hydrogen, resource);
	}
	
	@Test

	
	public void testInventoryAmountResourcePhaseTypeNoResource() {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		storage.addAmountResourcePhaseCapacity(PhaseType.GAS, 100D);
		AmountResource resource = storage.getAmountResourcePhaseType(PhaseType.GAS);
		assertNull("Amount phase type stored is null.", resource);
	}
	
	@Test

	
	public void testInventoryAmountResourcePhaseRemainingCapacityGood() {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		storage.addAmountResourcePhaseCapacity(PhaseType.GAS, 100D);
		AmountResource hydrogen = ResourceUtil.findAmountResource(ResourceUtil.HYDROGEN_ID);
		storage.storeAmountResourcePhase(hydrogen, 50D);
		double remainingCapacity = storage.getAmountResourcePhaseRemainingCapacity(PhaseType.GAS);
		assertEquals("Amount phase capacity remaining is correct amount.", 50D, remainingCapacity, 0D);
	}
	
	@Test

	
	public void testInventoryAmountResourcePhaseRemainingCapacityNoCapacity() {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		double remainingCapacity = storage.getAmountResourcePhaseRemainingCapacity(PhaseType.GAS);
		assertEquals("Amount phase capacity remaining is correct amount.", 0D, remainingCapacity, 0D);
	}
	
	@Test

	
	public void testInventoryAmountResourcePhaseRetrieveGood() {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		storage.addAmountResourcePhaseCapacity(PhaseType.GAS, 100D);
		AmountResource hydrogen = ResourceUtil.findAmountResource(ResourceUtil.HYDROGEN_ID);
		storage.storeAmountResourcePhase(hydrogen, 100D);
		storage.retrieveAmountResourcePhase(PhaseType.GAS, 50D);
		double remainingCapacity = storage.getAmountResourcePhaseRemainingCapacity(PhaseType.GAS);
		assertEquals("Amount phase capacity remaining is correct amount.", 50D, remainingCapacity, 0D);
	}
	
	@Test

	
	public void testInventoryAmountResourcePhaseRetrieveTooMuch() {
		try {
			AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
			storage.addAmountResourcePhaseCapacity(PhaseType.GAS, 100D);
			AmountResource hydrogen = ResourceUtil.findAmountResource(ResourceUtil.HYDROGEN_ID);
			storage.storeAmountResourcePhase(hydrogen, 100D);
			storage.retrieveAmountResourcePhase(PhaseType.GAS, 101D);
			fail("Amount phase retrieved fails correctly.");
		}
		catch (Exception e) {}
	}
	
	@Test

	
	public void testInventoryAmountResourcePhaseRetrieveNegative() {
		try {
			AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
			storage.addAmountResourcePhaseCapacity(PhaseType.GAS, 100D);
			AmountResource hydrogen = ResourceUtil.findAmountResource(ResourceUtil.HYDROGEN_ID);
			storage.storeAmountResourcePhase(hydrogen, 100D);
			storage.retrieveAmountResourcePhase(PhaseType.GAS, -100D);
			fail("Amount phase retrieved fails correctly.");
		}
		catch (Exception e) {}
	}
	
	@Test

	
	public void testInventoryAmountResourcePhaseRetrieveNoCapacity() {
		try {
			AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
			storage.retrieveAmountResourcePhase(PhaseType.GAS, 100D);
			fail("Amount phase retrieved fails correctly.");
		}
		catch (Exception e) {}
	}
	
	@Test

	
	public void testInventoryAmountResourcePhaseTotalAmount() {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		storage.addAmountResourcePhaseCapacity(PhaseType.GAS, 100D);
		storage.addAmountResourcePhaseCapacity(PhaseType.LIQUID, 100D);
		AmountResource oxygen = ResourceUtil.findAmountResource(ResourceUtil.OXYGEN_ID);
		AmountResource water = ResourceUtil.findAmountResource(ResourceUtil.WATER_ID);
		storage.storeAmountResourcePhase(oxygen, 10D);
		storage.storeAmountResourcePhase(water, 20D);
		double totalStored = storage.getTotalAmountResourcePhasesStored(false);
		assertEquals("Amount total stored is correct.", 30D, totalStored, 0D);
	}
}