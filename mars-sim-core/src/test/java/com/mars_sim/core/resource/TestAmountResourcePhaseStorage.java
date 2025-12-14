package com.mars_sim.core.resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;


import com.mars_sim.core.SimulationConfig;

class TestAmountResourcePhaseStorage {
	
    @BeforeEach
	void setUp() {
        SimulationConfig.loadConfig();
    }
	
	@Test
	void testInventoryAmountResourcePhaseCapacityGood() {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		storage.addAmountResourcePhaseCapacity(PhaseType.GAS, 100D);
		double amountGas = storage.getAmountResourcePhaseCapacity(PhaseType.GAS);
		assertEquals(100D, amountGas, 0D, "Amount resource phase capacity set correctly.");
	}
	
	@Test
	void testInventoryAmountResourcePhaseCapacityNotSet() {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		double amountGas = storage.getAmountResourcePhaseCapacity(PhaseType.GAS);
		assertEquals(0D, amountGas, 0D, "Amount resource phase capacity set correctly.");
	}
	
	@Test
	void testInventoryAmountResourcePhaseCapacityNegativeCapacity() {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		try {
			storage.addAmountResourcePhaseCapacity(PhaseType.GAS, -100D);
			fail("Cannot add negative capacity for a phase.");
		}
		catch (Exception e) {}
	}
	
	@Test
	void testInventoryAmountResourcePhaseStoreGood() {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		storage.addAmountResourcePhaseCapacity(PhaseType.GAS, 100D);
		AmountResource hydrogen = ResourceUtil.findAmountResource(ResourceUtil.HYDROGEN_ID);
		storage.storeAmountResourcePhase(hydrogen, 100D);
		double amountPhaseStored = storage.getAmountResourcePhaseStored(PhaseType.GAS);
		assertEquals(100D, amountPhaseStored, 0D, "Amount resource phase stored is correct.");
	}
	
	@Test
	void testInventoryAmountResourcePhaseStoreOverload() {
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
	void testInventoryAmountResourcePhaseStoreNegativeAmount() {
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
	void testInventoryAmountResourcePhaseStoreNoCapacity() {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		AmountResource hydrogen = ResourceUtil.findAmountResource(ResourceUtil.HYDROGEN_ID);
		try {
			storage.storeAmountResourcePhase(hydrogen, 100D);
			fail("Throws exception if capacity not set (overloaded)");
		}
		catch (Exception e) {}
	}
	
	@Test
	void testInventoryAmountResourcePhaseTypeGood() {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		storage.addAmountResourcePhaseCapacity(PhaseType.GAS, 100D);
		AmountResource hydrogen = ResourceUtil.findAmountResource(ResourceUtil.HYDROGEN_ID);
		storage.storeAmountResourcePhase(hydrogen, 100D);
		AmountResource resource = storage.getAmountResourcePhaseType(PhaseType.GAS);
		assertEquals(hydrogen, resource, "Amount phase stored is of correct type.");
	}
	
	@Test
	void testInventoryAmountResourcePhaseTypeNoResource() {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		storage.addAmountResourcePhaseCapacity(PhaseType.GAS, 100D);
		AmountResource resource = storage.getAmountResourcePhaseType(PhaseType.GAS);
		assertNull(resource, "Amount phase type stored is null.");
	}
	
	@Test
	void testInventoryAmountResourcePhaseRemainingCapacityGood() {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		storage.addAmountResourcePhaseCapacity(PhaseType.GAS, 100D);
		AmountResource hydrogen = ResourceUtil.findAmountResource(ResourceUtil.HYDROGEN_ID);
		storage.storeAmountResourcePhase(hydrogen, 50D);
		double remainingCapacity = storage.getAmountResourcePhaseRemainingCapacity(PhaseType.GAS);
		assertEquals(50D, remainingCapacity, 0D, "Amount phase capacity remaining is correct amount.");
	}
	
	@Test
	void testInventoryAmountResourcePhaseRemainingCapacityNoCapacity() {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		double remainingCapacity = storage.getAmountResourcePhaseRemainingCapacity(PhaseType.GAS);
		assertEquals(0D, remainingCapacity, 0D, "Amount phase capacity remaining is correct amount.");
	}
	
	@Test
	void testInventoryAmountResourcePhaseRetrieveGood() {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		storage.addAmountResourcePhaseCapacity(PhaseType.GAS, 100D);
		AmountResource hydrogen = ResourceUtil.findAmountResource(ResourceUtil.HYDROGEN_ID);
		storage.storeAmountResourcePhase(hydrogen, 100D);
		storage.retrieveAmountResourcePhase(PhaseType.GAS, 50D);
		double remainingCapacity = storage.getAmountResourcePhaseRemainingCapacity(PhaseType.GAS);
		assertEquals(50D, remainingCapacity, 0D, "Amount phase capacity remaining is correct amount.");
	}
	
	@Test
	void testInventoryAmountResourcePhaseRetrieveTooMuch() {
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
	void testInventoryAmountResourcePhaseRetrieveNegative() {

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
	void testInventoryAmountResourcePhaseRetrieveNoCapacity() {
		try {
			AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
			storage.retrieveAmountResourcePhase(PhaseType.GAS, 100D);
			fail("Amount phase retrieved fails correctly.");
		}
		catch (Exception e) {}
	}
	
	@Test
	void testInventoryAmountResourcePhaseTotalAmount() {
		AmountResourcePhaseStorage storage = new AmountResourcePhaseStorage();
		storage.addAmountResourcePhaseCapacity(PhaseType.GAS, 100D);
		storage.addAmountResourcePhaseCapacity(PhaseType.LIQUID, 100D);
		AmountResource oxygen = ResourceUtil.findAmountResource(ResourceUtil.OXYGEN_ID);
		AmountResource water = ResourceUtil.findAmountResource(ResourceUtil.WATER_ID);
		storage.storeAmountResourcePhase(oxygen, 10D);
		storage.storeAmountResourcePhase(water, 20D);
		double totalStored = storage.getTotalAmountResourcePhasesStored(false);
		assertEquals(30D, totalStored, 0D, "Amount total stored is correct.");
	}
}