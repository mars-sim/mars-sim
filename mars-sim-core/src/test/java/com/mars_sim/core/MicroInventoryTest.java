/*
 * Mars Simulation Project
 * MicroInventoryTest
 * @date 2022-09-20
 * @author Barry Evans
 */

package com.mars_sim.core;

import com.mars_sim.core.equipment.MicroInventory;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.MockSettlement;
import com.mars_sim.core.structure.Settlement;

import junit.framework.TestCase;

/**
 * Tests the micro inventory
 */
public class MicroInventoryTest
extends TestCase {


	private static final double CAPACITY_AMOUNT = 200D;
	
	private Settlement settlement = null;
	
	@Override
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
	public void testLoading() {
		MicroInventory inv = new MicroInventory(settlement);
		int resource = ResourceUtil.CO2_ID;
		inv.setSpecificCapacity(resource, CAPACITY_AMOUNT);
		
		assertEquals("No excess on 1st load", 0D, inv.storeAmountResource(resource, CAPACITY_AMOUNT/2));
		assertEquals("Stored capacity after 1st load", CAPACITY_AMOUNT/2, inv.getSpecificAmountResourceStored(resource));
		assertEquals("Remaining after 1st load capacity", CAPACITY_AMOUNT/2, inv.getRemainingSpecificCapacity(resource));
		assertEquals("Total mass after 1st load", CAPACITY_AMOUNT/2, inv.getStoredMass());
		
		assertEquals("No excess on 2nd load", 0D, inv.storeAmountResource(resource, CAPACITY_AMOUNT/2));
		assertEquals("Stored capacity after 2nd load", CAPACITY_AMOUNT, inv.getSpecificAmountResourceStored(resource));
		assertEquals("Remaining after 2nd load capacity", 0D, inv.getRemainingSpecificCapacity(resource));
		assertEquals("Total mass after 2nd load", CAPACITY_AMOUNT, inv.getStoredMass());

	}

	/*
	 * Test method loading Equipment
	 */
	public void testOverloading() {
		MicroInventory inv = new MicroInventory(settlement);
		int resource = ResourceUtil.CO2_ID;
		inv.setSpecificCapacity(resource, CAPACITY_AMOUNT);
		
		assertEquals("No excess on capacity load", 0D, inv.storeAmountResource(resource, CAPACITY_AMOUNT/2));

		assertEquals("Excess on overload", CAPACITY_AMOUNT/2, inv.storeAmountResource(resource, CAPACITY_AMOUNT));
		assertEquals("Stored capacity after overload", CAPACITY_AMOUNT, inv.getSpecificAmountResourceStored(resource));
		assertEquals("Remaining after overload", 0D, inv.getRemainingSpecificCapacity(resource));
	}

	/*
	 * Test method loading Equipment
	 */
	public void testUnsupported() {
		MicroInventory inv = new MicroInventory(settlement);
		int resource = ResourceUtil.CO2_ID;
		inv.setSpecificCapacity(resource, CAPACITY_AMOUNT);

		int unprovisioned = ResourceUtil.OXYGEN_ID;

		assertTrue("Provisioned resource", inv.isResourceSupported(resource));
		assertFalse("Unprovisioned resource", inv.isResourceSupported(unprovisioned));

		assertEquals("Excess on unprovisioned", CAPACITY_AMOUNT, inv.storeAmountResource(unprovisioned, CAPACITY_AMOUNT));
		assertEquals("Remaining on unprovisioned", 0D, inv.getRemainingSpecificCapacity(unprovisioned));
		assertEquals("Capacity on unprovisioned", 0D, inv.getSpecificCapacity(unprovisioned));

	}
	
	/*
	 * Test method loading Equipment
	 */
	public void testUnloading()  {
		MicroInventory inv = new MicroInventory(settlement);
		int resource = ResourceUtil.CO2_ID;
		inv.setSpecificCapacity(resource, CAPACITY_AMOUNT);
		
		double excess = inv.storeAmountResource(resource, CAPACITY_AMOUNT);
		System.out.println("excess: " + excess);
		
		double shortfall = inv.retrieveAmountResource(resource, CAPACITY_AMOUNT/2);
		System.out.println("shortfall: " + shortfall);
		
		assertEquals("Shortfall on 1st retrieve", 0D, shortfall);
		
		double stored = inv.getSpecificAmountResourceStored(resource);
		System.out.println("stored: " + stored);
		
		assertEquals("Stored on 1st retrieve", CAPACITY_AMOUNT/2, stored);
		assertEquals("Remaining after 1st retrieve", CAPACITY_AMOUNT/2, inv.getRemainingSpecificCapacity(resource));
		
		double mass = inv.getStoredMass();
		System.out.println("mass: " + mass);
		
		assertEquals("Total mass after 1st retrieve", CAPACITY_AMOUNT/2, mass);

		shortfall = inv.retrieveAmountResource(resource, CAPACITY_AMOUNT/2);
		System.out.println("shortfall: " + shortfall);
		
		assertEquals("Shortfall on 2nd retrieve", 0D, shortfall);
		
		stored = inv.getSpecificAmountResourceStored(resource);
		System.out.println("stored: " + stored);
		
		assertEquals("Stored on 2nd retrieve", 0D, stored);
		
		double cap = inv.getRemainingSpecificCapacity(resource);
		System.out.println("cap: " + cap);
		
		assertEquals("Remaining after 2nd retrieve", CAPACITY_AMOUNT, cap);
		
		double totalSpecificStored = inv.getTotalSpecificAmountResourceStored();
		System.out.println("totalSpecificStored: " + totalSpecificStored);
		
		double totalStockStored = inv.getTotalStockAmountResourceStored();
		System.out.println("totalStockStored: " + totalStockStored);
		
		inv.printStoredMass();
		mass = inv.getStoredMass();
		System.out.println("mass: " + mass);
		
		assertEquals("Total mass after 2nd retrieve", 0D, mass);

		assertEquals("Shortfall on empty inventory", 100D, inv.retrieveAmountResource(resource, 100D));
	}
	
	/*
	 * Test method loading Equipment
	 */
	public void testMultiples()  {
		MicroInventory inv = new MicroInventory(settlement);
		int resource = ResourceUtil.CO2_ID;
		inv.setSpecificCapacity(resource, CAPACITY_AMOUNT);
		int resource2  = ResourceUtil.OXYGEN_ID;
		inv.setSpecificCapacity(resource2, 100D);

		assertEquals("Remaining capacity 1st resource", CAPACITY_AMOUNT, inv.getRemainingSpecificCapacity(resource));
		assertEquals("Remaining capacity 2nd resource", 100D, inv.getRemainingSpecificCapacity(resource2));
		
		inv.storeAmountResource(resource, CAPACITY_AMOUNT/2);
		inv.storeAmountResource(resource2, 100D);
		assertEquals("Total mass after combined load", (CAPACITY_AMOUNT/2 + 100D), inv.getStoredMass());

	}
}