package com.mars_sim.core.resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.structure.MockSettlement;
import com.mars_sim.core.structure.Settlement;



public class TestAmountResourceStorage {

	private Settlement settlement;
	
    @BeforeEach

	


	
    public void setUp() {
        SimulationConfig.loadConfig();
        Simulation.instance().testRun();
        settlement = new MockSettlement();
    }

    @Test
    void testInventoryAmountResourceTypeCapacityGood() {
		AmountResourceStorage storage = new AmountResourceStorage(settlement);
		AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
		storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
		double amountCO2 = storage.getAmountResourceCapacity(carbonDioxide);
		assertEquals(100D, amountCO2, 0D, "Amount resource type capacity set correctly.");
	}
	
	@Test
	void testInventoryAmountResourceTypeCapacityNegativeCapacity() {
		AmountResourceStorage storage = new AmountResourceStorage(settlement);
		AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
		assertThrows(IllegalStateException.class, () -> {
			storage.addAmountResourceTypeCapacity(carbonDioxide, -100D);
		});
	}
	
	/**
	 * Test the removeAmountResourceTypeCapacity method.
	 */
	@Test
	void testRemoveAmountResourceTypeCapacity() {
	    AmountResourceStorage storage = new AmountResourceStorage(settlement);
	    AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
	    
	    storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        double amountCarbonDioxide1 = storage.getAmountResourceCapacity(carbonDioxide);
        assertEquals(100D, amountCarbonDioxide1);
        
        // Test removing 50 kg of CO2 capacity.
        storage.removeAmountResourceTypeCapacity(carbonDioxide, 50D);
        double amountCarbonDioxide2 = storage.getAmountResourceCapacity(carbonDioxide);
        assertEquals(50D, amountCarbonDioxide2);
        
        // Test removing another 50 kg of CO2 capacity.
        storage.removeAmountResourceTypeCapacity(carbonDioxide, 50D);
        double amountCarbonDioxide3 = storage.getAmountResourceCapacity(carbonDioxide);
        assertEquals(0D, amountCarbonDioxide3);
        
        // Test removing another 50 kg of CO2 capacity (should throw IllegalStateException).
		assertThrows(IllegalStateException.class, () -> {
        	storage.removeAmountResourceTypeCapacity(carbonDioxide, 50D);
		});
	}
	
	@Test
	void testInventoryAmountResourcePhaseCapacityGood() {
		AmountResourceStorage storage = new AmountResourceStorage(settlement);
		storage.addAmountResourcePhaseCapacity(PhaseType.GAS, 100D);
		AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
		double amountCO2 = storage.getAmountResourceCapacity(carbonDioxide);
		assertEquals(100D, amountCO2, 0D, "Amount resource type capacity set correctly.");
	}
	
	@Test
	void testInventoryAmountResourcePhaseCapacityNegativeCapacity() {
		AmountResourceStorage storage = new AmountResourceStorage(settlement);
		assertThrows(IllegalStateException.class, () -> {
			storage.addAmountResourcePhaseCapacity(PhaseType.GAS, -100D);
		});
	}
	
	@Test
	void testInventoryAmountResourceComboCapacityGood() {
		AmountResourceStorage storage = new AmountResourceStorage(settlement);
		storage.addAmountResourcePhaseCapacity(PhaseType.GAS, 50D);
		AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
		storage.addAmountResourceTypeCapacity(carbonDioxide, 50D);
		double amountCO2 = storage.getAmountResourceCapacity(carbonDioxide);
		assertEquals(100D, amountCO2, 0D, "Amount resource type capacity set correctly.");
	}
	
	@Test
	void testInventoryAmountResourceCapacityNotSet() {
		AmountResourceStorage storage = new AmountResourceStorage(settlement);
		AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
		double amountCO2 = storage.getAmountResourceCapacity(carbonDioxide);
		assertEquals(0D, amountCO2, 0D, "Amount resource capacity set correctly.");
	}
	
	@Test
	void testInventoryAmountResourceTypeStoreGood() {
		AmountResourceStorage storage = new AmountResourceStorage(settlement);
		AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
		storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
		storage.storeAmountResource(carbonDioxide, 100D);
		double amountTypeStored = storage.getAmountResourceStored(carbonDioxide);
		assertEquals(100D, amountTypeStored, 0D, "Amount resource type stored is correct.");
	}
	
	@Test
	void testInventoryAmountResourcePhaseStoreGood() {
		AmountResourceStorage storage = new AmountResourceStorage(settlement);
		storage.addAmountResourcePhaseCapacity(PhaseType.GAS, 100D);
		AmountResource hydrogen = ResourceUtil.findAmountResource(ResourceUtil.HYDROGEN_ID);
		storage.storeAmountResource(hydrogen, 100D);
		double amountPhaseStored = storage.getAmountResourceStored(hydrogen);
		assertEquals(100D, amountPhaseStored, 0D, "Amount resource phase stored is correct.");
	}
	
	@Test
	void testInventoryAmountResourceStoreNegativeAmount() {
		AmountResourceStorage storage = new AmountResourceStorage(settlement);
		AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
		storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
		assertThrows(IllegalStateException.class, () -> {
			storage.storeAmountResource(carbonDioxide, -1D);
		});
	}
	
	@Test
	void testInventoryAmountResourceRemainingCapacityGood() {
		AmountResourceStorage storage = new AmountResourceStorage(settlement);
		AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
		storage.addAmountResourceTypeCapacity(carbonDioxide, 50D);
		storage.addAmountResourcePhaseCapacity(PhaseType.GAS, 50D);
		storage.storeAmountResource(carbonDioxide, 60D);
		double remainingCapacity = storage.getAmountResourceRemainingCapacity(carbonDioxide);
		assertEquals(40D, remainingCapacity, 0D, "Amount type capacity remaining is correct amount.");
	}
	
	@Test
	void testInventoryAmountResourceTypeRemainingCapacityNoCapacity() {
		AmountResourceStorage storage = new AmountResourceStorage(settlement);
		AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
		double remainingCapacity = storage.getAmountResourceRemainingCapacity(carbonDioxide);
		assertEquals(0D, remainingCapacity, 0D, "Amount type capacity remaining is correct amount.");
	}
	
	void testInventoryAmountResourceRetrieveGood()  {
		AmountResourceStorage storage = new AmountResourceStorage(settlement);
		AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
		storage.addAmountResourceTypeCapacity(carbonDioxide, 50D);
		storage.addAmountResourcePhaseCapacity(PhaseType.GAS, 50D);
		storage.storeAmountResource(carbonDioxide, 100D);
		storage.retrieveAmountResource(carbonDioxide, 50D);
		double remainingCapacity = storage.getAmountResourceRemainingCapacity(carbonDioxide);
		assertEquals(50D, remainingCapacity, 0D, "Amount type capacity remaining is correct amount.");
	}
	
	void testInventoryAmountResourceRetrieveNegative()  {
		AmountResourceStorage storage = new AmountResourceStorage(settlement);
		AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
		storage.addAmountResourceTypeCapacity(carbonDioxide, 50D);
		storage.addAmountResourcePhaseCapacity(PhaseType.GAS, 50D);
		storage.storeAmountResource(carbonDioxide, 100D);
		assertThrows(IllegalStateException.class, () -> {
			storage.retrieveAmountResource(carbonDioxide, -100D);
		});
	}
	
	@Test
	void testInventoryAmountResourceTotalAmount() {
		AmountResourceStorage storage = new AmountResourceStorage(settlement);
		AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
		AmountResource water = ResourceUtil.findAmountResource(ResourceUtil.WATER_ID);
		storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
		storage.addAmountResourcePhaseCapacity(PhaseType.LIQUID, 100D);
		storage.storeAmountResource(carbonDioxide, 10D);
		storage.storeAmountResource(water, 20D);
		double totalStored = storage.getTotalAmountResourcesStored(false);
		assertEquals(30D, totalStored, 0D, "Amount total stored is correct.");
	}
	
	@Test
	void testInventoryAmountResourceAllResources() {
		AmountResourceStorage storage = new AmountResourceStorage(settlement);
		AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
		AmountResource water = ResourceUtil.findAmountResource(ResourceUtil.WATER_ID);
		storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
		storage.addAmountResourcePhaseCapacity(PhaseType.LIQUID, 100D);
		storage.storeAmountResource(carbonDioxide, 10D);
		storage.storeAmountResource(water, 20D);
		Set<AmountResource> allResources = storage.getAllAmountResourcesStored(false);
		assertTrue(allResources.contains(carbonDioxide), "All resources contains carbon dioxide.");
		assertTrue(allResources.contains(water), "All resources contains oxygen.");
	}
}