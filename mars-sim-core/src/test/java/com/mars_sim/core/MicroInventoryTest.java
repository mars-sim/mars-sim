/*
 * Mars Simulation Project
 * MicroInventoryTest
 * @date 2022-09-20
 * @author Barry Evans
 */

package com.mars_sim.core;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;


import com.mars_sim.core.equipment.MicroInventory;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.MockSettlement;
import com.mars_sim.core.structure.Settlement;



/**
 * Tests the micro inventory
 */
class MicroInventoryTest {


	private static final double CAPACITY_AMOUNT = 200D;
	
	private Settlement settlement = null;
	
	@BeforeEach
	void setUp() {
        SimulationConfig.loadConfig();
        Simulation.instance().testRun();
        
        UnitManager unitManager = Simulation.instance().getUnitManager();
	
		// Create test settlement.
		settlement = new MockSettlement();	
		unitManager.addUnit(settlement);
    }


	/*
	 * Test method loading Equipment
	 */
	@Test
	void testAmountLoading() {
		int resource = ResourceUtil.CO2_ID;
		MicroInventory inv = new MicroInventory(settlement, 2*CAPACITY_AMOUNT, 0,
											Map.of(resource, CAPACITY_AMOUNT));

		assertEquals(0D, inv.storeAmountResource(resource, CAPACITY_AMOUNT/2), "No excess on 1st load");
		assertEquals(CAPACITY_AMOUNT/2, inv.getSpecificAmountResourceStored(resource), "Stored capacity after 1st load");
		assertEquals(CAPACITY_AMOUNT/2, inv.getRemainingSpecificCapacity(resource), "Remaining after 1st load capacity");
		assertEquals(CAPACITY_AMOUNT/2, inv.getStoredMass(), "Total mass after 1st load");
		
		assertEquals(0D, inv.storeAmountResource(resource, CAPACITY_AMOUNT/2), "No excess on 2nd load");
		assertEquals(CAPACITY_AMOUNT, inv.getSpecificAmountResourceStored(resource), "Stored capacity after 2nd load");
		assertEquals(0D, inv.getRemainingSpecificCapacity(resource), "Remaining after 2nd load capacity");
		assertEquals(CAPACITY_AMOUNT, inv.getStoredMass(), "Total mass after 2nd load");

	}

	
	/*
	 * Test overload storing AmountResource
	 */
	@Test
	void testAmountOverloading() {
		int resource = ResourceUtil.CO2_ID;
		MicroInventory inv = new MicroInventory(settlement, 2*CAPACITY_AMOUNT, 0,
											Map.of(resource, CAPACITY_AMOUNT));
		
		assertEquals(0D, inv.storeAmountResource(resource, CAPACITY_AMOUNT/2), "No excess on capacity load");

		assertEquals(CAPACITY_AMOUNT/2, inv.storeAmountResource(resource, CAPACITY_AMOUNT), "Excess on overload");
		assertEquals(CAPACITY_AMOUNT, inv.getSpecificAmountResourceStored(resource), "Stored capacity after overload");
		assertEquals(0D, inv.getRemainingSpecificCapacity(resource), "Remaining after overload");
	}

	/*
	 * Test stock overloading of AmountResource
	 */
	@Test
	void testAmountStockOverloading() {
		final double stockSize = CAPACITY_AMOUNT/10;
		int resource = ResourceUtil.CO2_ID;
		MicroInventory inv = new MicroInventory(settlement, 2*CAPACITY_AMOUNT, stockSize,
											Map.of(resource, CAPACITY_AMOUNT));

		assertEquals(CAPACITY_AMOUNT, inv.getRemainingSpecificCapacity(resource), "Initial capacity, no stock");
		
		assertEquals(0D, inv.storeAmountResource(resource, CAPACITY_AMOUNT), "No excess on capacity load");

		assertEquals(CAPACITY_AMOUNT - stockSize, inv.storeAmountResource(resource, CAPACITY_AMOUNT), "Excess on overload");
		assertEquals(CAPACITY_AMOUNT + stockSize, inv.getSpecificAmountResourceStored(resource), "Stored capacity after overload");
		assertEquals(0D, inv.getRemainingSpecificCapacity(resource), "Remaining after overload");
		assertEquals(0D, inv.getAmountStockAvailable(), "Stock remains after overload");
	}

	/*
	 * Test stock is shared for AmountResource
	 */
	@Test
	void testAmountSharedStock() {
		final double stockSize = CAPACITY_AMOUNT/10;
		int resource1 = ResourceUtil.CO2_ID;
		int resource2 = ResourceUtil.OXYGEN_ID;
		MicroInventory inv = new MicroInventory(settlement, 2*CAPACITY_AMOUNT, stockSize,
											Map.of(resource1, CAPACITY_AMOUNT, resource2, CAPACITY_AMOUNT));

		assertEquals(CAPACITY_AMOUNT, inv.getRemainingSpecificCapacity(resource1), "Initial resource 1 capacity");
		assertEquals(CAPACITY_AMOUNT, inv.getRemainingSpecificCapacity(resource2), "Initial resource 2 capacity");
		assertEquals(stockSize, inv.getAmountStockAvailable(), "Initial stock available");

		double overloadAmount = CAPACITY_AMOUNT + (stockSize/2);
		assertEquals(0D, inv.storeAmountResource(resource1, overloadAmount), "No excess on capacity load");
		assertEquals(0D, inv.getRemainingSpecificCapacity(resource1), "Resource 1 capacity after 1st load");
		assertEquals(stockSize/2, inv.getAmountStockAvailable(), "Half stock used");

		// Consume all stock
		assertEquals(0D, inv.storeAmountResource(resource2, overloadAmount), "No excess on capacity load");
		assertEquals(overloadAmount, inv.getSpecificAmountResourceStored(resource2), "Resource 2 stored after 2nd load");
		assertEquals(0D, inv.getRemainingSpecificCapacity(resource2), "Resource 2 capacity after 2nd load");
		assertEquals(0D, inv.getAmountStockAvailable(), "All stock used");

		// Remove some to release stock
		inv.retrieveAmountResource(resource2, stockSize/2);
		assertEquals(0D, inv.getRemainingSpecificCapacity(resource2), "Resource 2 capacity after releasing stock");
		assertEquals(stockSize/2, inv.getAmountStockAvailable(), "Half stock returned");

		// RRetrieve more does not impact stock
		inv.retrieveAmountResource(resource2, stockSize/2);
		assertEquals(stockSize/2, inv.getAmountStockAvailable(), "Only half stock returned");

		// Consume released stcok
		inv.storeAmountResource(resource1, overloadAmount);
		assertEquals(0D, inv.getAmountStockAvailable(), "All stock consumed by Resource1");
	}

	/*
	 * Test method loading unsupported AmountResource
	 */
	@Test
	void testUnsupportedAmountResource() {
		int resource = ResourceUtil.CO2_ID;
		MicroInventory inv = new MicroInventory(settlement, 2*CAPACITY_AMOUNT, 0,
											Map.of(resource, CAPACITY_AMOUNT));

		int unprovisioned = ResourceUtil.OXYGEN_ID;

		assertTrue(inv.isResourceSupported(resource), "Provisioned resource");
		assertFalse(inv.isResourceSupported(unprovisioned), "Unprovisioned resource");

		assertEquals(CAPACITY_AMOUNT, inv.storeAmountResource(unprovisioned, CAPACITY_AMOUNT), "Excess on unprovisioned");
		assertEquals(0D, inv.getRemainingSpecificCapacity(unprovisioned), "Remaining on unprovisioned");
		assertEquals(0D, inv.getSpecificCapacity(unprovisioned), "Capacity on unprovisioned");

	}
	
	/*
	 * Test method unloading AmountResource
	 */
	@Test
	void testUnloadingAmountResource()  {
		int resource = ResourceUtil.CO2_ID;
		MicroInventory inv = new MicroInventory(settlement, 2*CAPACITY_AMOUNT, 0,
											Map.of(resource, CAPACITY_AMOUNT));
		
		inv.storeAmountResource(resource, CAPACITY_AMOUNT);
		
		double shortfall = inv.retrieveAmountResource(resource, CAPACITY_AMOUNT/2);
		assertEquals(0D, shortfall, "Shortfall on 1st retrieve");
		
		double stored = inv.getSpecificAmountResourceStored(resource);
		
		assertEquals(CAPACITY_AMOUNT/2, stored, "Stored on 1st retrieve");
		assertEquals(CAPACITY_AMOUNT/2, inv.getRemainingSpecificCapacity(resource), "Remaining after 1st retrieve");
		
		double mass = inv.getStoredMass();		
		assertEquals(CAPACITY_AMOUNT/2, mass, "Total mass after 1st retrieve");

		shortfall = inv.retrieveAmountResource(resource, CAPACITY_AMOUNT/2);		
		assertEquals(0D, shortfall, "Shortfall on 2nd retrieve");
		
		stored = inv.getSpecificAmountResourceStored(resource);		
		assertEquals(0D, stored, "Stored on 2nd retrieve");
		
		double cap = inv.getRemainingSpecificCapacity(resource);		
		assertEquals(CAPACITY_AMOUNT, cap, "Remaining after 2nd retrieve");
		
		mass = inv.getStoredMass();
		
		assertEquals(0D, mass, "Total mass after 2nd retrieve");

		assertEquals(100D, inv.retrieveAmountResource(resource, 100D), "Shortfall on empty inventory");
	}
	
	/*
	 * Test method loading Equipment
	 */
	@Test
	void testMultiples()  {
		int resource = ResourceUtil.CO2_ID;
		int resource2  = ResourceUtil.OXYGEN_ID;
		MicroInventory inv = new MicroInventory(settlement, 2*CAPACITY_AMOUNT, 0,
											Map.of(resource, CAPACITY_AMOUNT,
												resource2, 100D
											));

		assertEquals(CAPACITY_AMOUNT, inv.getRemainingSpecificCapacity(resource), "Remaining capacity 1st resource");
		assertEquals(100D, inv.getRemainingSpecificCapacity(resource2), "Remaining capacity 2nd resource");
		
		inv.storeAmountResource(resource, CAPACITY_AMOUNT/2);
		inv.storeAmountResource(resource2, 100D);
		assertEquals((CAPACITY_AMOUNT/2 + 100D), inv.getStoredMass(), "Total mass after combined load");
	}
}