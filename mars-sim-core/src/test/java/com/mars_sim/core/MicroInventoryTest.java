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
import static org.junit.jupiter.api.Assertions.assertFalse;


import com.mars_sim.core.equipment.MicroInventory;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.MockSettlement;
import com.mars_sim.core.structure.Settlement;



/**
 * Tests the micro inventory
 */
public class MicroInventoryTest {


	private static final double CAPACITY_AMOUNT = 200D;
	
	private Settlement settlement = null;
	
	@BeforeEach

	
	@BeforeEach


	
	public void setUp() throws Exception {
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
	public void testLoading() {
		MicroInventory inv = new MicroInventory(settlement);
		int resource = ResourceUtil.CO2_ID;
		inv.setSpecificCapacity(resource, CAPACITY_AMOUNT);
		
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
	 * Test method loading Equipment
	 */
	@Test
	public void testOverloading() {
		MicroInventory inv = new MicroInventory(settlement);
		int resource = ResourceUtil.CO2_ID;
		inv.setSpecificCapacity(resource, CAPACITY_AMOUNT);
		
		assertEquals(0D, inv.storeAmountResource(resource, CAPACITY_AMOUNT/2), "No excess on capacity load");

		assertEquals(CAPACITY_AMOUNT/2, inv.storeAmountResource(resource, CAPACITY_AMOUNT), "Excess on overload");
		assertEquals(CAPACITY_AMOUNT, inv.getSpecificAmountResourceStored(resource), "Stored capacity after overload");
		assertEquals(0D, inv.getRemainingSpecificCapacity(resource), "Remaining after overload");
	}

	/*
	 * Test method loading Equipment
	 */
	@Test
	public void testUnsupported() {
		MicroInventory inv = new MicroInventory(settlement);
		int resource = ResourceUtil.CO2_ID;
		inv.setSpecificCapacity(resource, CAPACITY_AMOUNT);

		int unprovisioned = ResourceUtil.OXYGEN_ID;

		assertTrue(inv.isResourceSupported(resource), "Provisioned resource");
		assertFalse(inv.isResourceSupported(unprovisioned), "Unprovisioned resource");

		assertEquals(CAPACITY_AMOUNT, inv.storeAmountResource(unprovisioned, CAPACITY_AMOUNT), "Excess on unprovisioned");
		assertEquals(0D, inv.getRemainingSpecificCapacity(unprovisioned), "Remaining on unprovisioned");
		assertEquals(0D, inv.getSpecificCapacity(unprovisioned), "Capacity on unprovisioned");

	}
	
	/*
	 * Test method loading Equipment
	 */
	@Test
	public void testUnloading()  {
		MicroInventory inv = new MicroInventory(settlement);
		int resource = ResourceUtil.CO2_ID;
		inv.setSpecificCapacity(resource, CAPACITY_AMOUNT);
		
		double excess = inv.storeAmountResource(resource, CAPACITY_AMOUNT);
		System.out.println("excess: " + excess);
		
		double shortfall = inv.retrieveAmountResource(resource, CAPACITY_AMOUNT/2);
		System.out.println("shortfall: " + shortfall);
		
		assertEquals(0D, shortfall, "Shortfall on 1st retrieve");
		
		double stored = inv.getSpecificAmountResourceStored(resource);
		System.out.println("stored: " + stored);
		
		assertEquals(CAPACITY_AMOUNT/2, stored, "Stored on 1st retrieve");
		assertEquals(CAPACITY_AMOUNT/2, inv.getRemainingSpecificCapacity(resource), "Remaining after 1st retrieve");
		
		double mass = inv.getStoredMass();
		System.out.println("mass: " + mass);
		
		assertEquals(CAPACITY_AMOUNT/2, mass, "Total mass after 1st retrieve");

		shortfall = inv.retrieveAmountResource(resource, CAPACITY_AMOUNT/2);
		System.out.println("shortfall: " + shortfall);
		
		assertEquals(0D, shortfall, "Shortfall on 2nd retrieve");
		
		stored = inv.getSpecificAmountResourceStored(resource);
		System.out.println("stored: " + stored);
		
		assertEquals(0D, stored, "Stored on 2nd retrieve");
		
		double cap = inv.getRemainingSpecificCapacity(resource);
		System.out.println("cap: " + cap);
		
		assertEquals(CAPACITY_AMOUNT, cap, "Remaining after 2nd retrieve");
		
		double totalSpecificStored = inv.getTotalSpecificAmountResourceStored();
		System.out.println("totalSpecificStored: " + totalSpecificStored);
		
		double totalStockStored = inv.getTotalStockAmountResourceStored();
		System.out.println("totalStockStored: " + totalStockStored);
		
		inv.printStoredMass();
		mass = inv.getStoredMass();
		System.out.println("mass: " + mass);
		
		assertEquals(0D, mass, "Total mass after 2nd retrieve");

		assertEquals(100D, inv.retrieveAmountResource(resource, 100D), "Shortfall on empty inventory");
	}
	
	/*
	 * Test method loading Equipment
	 */
	@Test
	public void testMultiples()  {
		MicroInventory inv = new MicroInventory(settlement);
		int resource = ResourceUtil.CO2_ID;
		inv.setSpecificCapacity(resource, CAPACITY_AMOUNT);
		int resource2  = ResourceUtil.OXYGEN_ID;
		inv.setSpecificCapacity(resource2, 100D);

		assertEquals(CAPACITY_AMOUNT, inv.getRemainingSpecificCapacity(resource), "Remaining capacity 1st resource");
		assertEquals(100D, inv.getRemainingSpecificCapacity(resource2), "Remaining capacity 2nd resource");
		
		inv.storeAmountResource(resource, CAPACITY_AMOUNT/2);
		inv.storeAmountResource(resource2, 100D);
		assertEquals((CAPACITY_AMOUNT/2 + 100D), inv.getStoredMass(), "Total mass after combined load");

	}
}