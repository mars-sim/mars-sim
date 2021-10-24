/*
 * Mars Simulation Project
 * LoadVehicleTest
 * @date 2021-09-29
 * @author Scott Davis
 */

package org.mars_sim.msp.core;

import org.mars_sim.msp.core.data.MicroInventory;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.MockSettlement;
import org.mars_sim.msp.core.structure.Settlement;

import junit.framework.TestCase;

/**
 * Tests the loading of a vehicle
 */
public class MicroInventoryTest
extends TestCase {


	private static final double CAPACITY_AMOUNT = 200D;
	
	private Settlement settlement = null;
	
	@Override
    public void setUp() throws Exception {
        SimulationConfig.instance().loadConfig();
        Simulation.instance().testRun();
        
        UnitManager unitManager = Simulation.instance().getUnitManager();
	
		// Create test settlement.
		settlement = new MockSettlement();	
		unitManager.addUnit(settlement);

    }


	/*
	 * Test method loading Equipment
	 */
	public void testLoading() throws Exception {
		MicroInventory inv = new MicroInventory(settlement);
		int resource = ResourceUtil.co2ID;
		inv.setCapacity(resource, CAPACITY_AMOUNT);
		
		assertEquals("No excess on 1st load", 0D, inv.storeAmountResource(resource, CAPACITY_AMOUNT/2));
		assertEquals("Stored capacity after 1st load", CAPACITY_AMOUNT/2, inv.getAmountResourceStored(resource));
		assertEquals("Remaining after 1st load capacity", CAPACITY_AMOUNT/2, inv.getAmountResourceRemainingCapacity(resource));
		assertEquals("Total mass after 1st load", CAPACITY_AMOUNT/2, inv.getStoredMass());
		
		assertEquals("No excess on 2nd load", 0D, inv.storeAmountResource(resource, CAPACITY_AMOUNT/2));
		assertEquals("Stored capacity after 2nd load", CAPACITY_AMOUNT, inv.getAmountResourceStored(resource));
		assertEquals("Remaining after 2nd load capacity", 0D, inv.getAmountResourceRemainingCapacity(resource));
		assertEquals("Total mass after 2nd load", CAPACITY_AMOUNT, inv.getStoredMass());

	}

	/*
	 * Test method loading Equipment
	 */
	public void testOverloading() throws Exception {
		MicroInventory inv = new MicroInventory(settlement);
		int resource = ResourceUtil.co2ID;
		inv.setCapacity(resource, CAPACITY_AMOUNT);
		
		assertEquals("No excess on capacity load", 0D, inv.storeAmountResource(resource, CAPACITY_AMOUNT/2));

		assertEquals("Excess on overload", CAPACITY_AMOUNT/2, inv.storeAmountResource(resource, CAPACITY_AMOUNT));
		assertEquals("Stored capacity after overload", CAPACITY_AMOUNT, inv.getAmountResourceStored(resource));
		assertEquals("Remaining after overload", 0D, inv.getAmountResourceRemainingCapacity(resource));
	}

	/*
	 * Test method loading Equipment
	 */
	public void testUnsupported() throws Exception {
		MicroInventory inv = new MicroInventory(settlement);
		int resource = ResourceUtil.co2ID;
		inv.setCapacity(resource, CAPACITY_AMOUNT);

		int unprovisioned = ResourceUtil.oxygenID;

		assertTrue("Provisioned resource", inv.isResourceSupported(resource));
		assertFalse("Unprovisioned resource", inv.isResourceSupported(unprovisioned));

		assertEquals("Excess on unprovisioned", CAPACITY_AMOUNT, inv.storeAmountResource(unprovisioned, CAPACITY_AMOUNT));
		assertEquals("Remaining on unprovisioned", 0D, inv.getAmountResourceRemainingCapacity(unprovisioned));
		assertEquals("Capacity on unprovisioned", 0D, inv.getCapacity(unprovisioned));

	}
	
	/*
	 * Test method loading Equipment
	 */
	public void testUnloading() throws Exception {
		MicroInventory inv = new MicroInventory(settlement);
		int resource = ResourceUtil.co2ID;
		inv.setCapacity(resource, CAPACITY_AMOUNT);
		
		inv.storeAmountResource(resource, CAPACITY_AMOUNT);

		assertEquals("Shortfall on 1st retrieve", 0D, inv.retrieveAmountResource(resource, CAPACITY_AMOUNT/2));
		assertEquals("Stored on 1st retrieve", CAPACITY_AMOUNT/2, inv.getAmountResourceStored(resource));
		assertEquals("Remaining after 1st retrieve", CAPACITY_AMOUNT/2, inv.getAmountResourceRemainingCapacity(resource));
		assertEquals("Total mass after 1st retrieve", CAPACITY_AMOUNT/2, inv.getStoredMass());

		assertEquals("Shortfall on 2nd retrieve", 0D, inv.retrieveAmountResource(resource, CAPACITY_AMOUNT/2));
		assertEquals("Stored on 2nd retrieve", 0D, inv.getAmountResourceStored(resource));
		assertEquals("Remaining after 2nd retrieve", CAPACITY_AMOUNT, inv.getAmountResourceRemainingCapacity(resource));
		assertEquals("Total mass after 2nd retrieve", 0D, inv.getStoredMass());

		assertEquals("Shortfall on empty inventory", 100D, inv.retrieveAmountResource(resource, 100D));
	}
	
	/*
	 * Test method loading Equipment
	 */
	public void testMultiples() throws Exception {
		MicroInventory inv = new MicroInventory(settlement);
		int resource = ResourceUtil.co2ID;
		inv.setCapacity(resource, CAPACITY_AMOUNT);
		int resource2  = ResourceUtil.oxygenID;
		inv.setCapacity(resource2, 100D);

		assertEquals("Remaining capacity 1st resource", CAPACITY_AMOUNT, inv.getAmountResourceRemainingCapacity(resource));
		assertEquals("Remaining capacity 2nd resource", 100D, inv.getAmountResourceRemainingCapacity(resource2));
		
		inv.storeAmountResource(resource, CAPACITY_AMOUNT/2);
		inv.storeAmountResource(resource2, 100D);
		assertEquals("Total mass after combined load", (CAPACITY_AMOUNT/2 + 100D), inv.getStoredMass());

	}
}