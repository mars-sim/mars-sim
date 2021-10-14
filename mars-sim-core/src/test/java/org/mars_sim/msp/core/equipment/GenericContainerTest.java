/*
 * Mars Simulation Project
 * GenericContainerTest.java
 * @date 2021-10-14
 * @author Barry Evans
 */

package org.mars_sim.msp.core.equipment;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.MockSettlement;
import org.mars_sim.msp.core.structure.Settlement;

import junit.framework.TestCase;

/**
 * Tests the logic of a Generic container
 */
public class GenericContainerTest
extends TestCase {

	// Extra amount to add to resource to handle double arithmetic mismatch
	private Settlement settlement;
	
	@Override
    public void setUp() throws Exception {
		// Create new simulation instance.
        SimulationConfig simConfig = SimulationConfig.instance();
        simConfig.loadConfig();
        
        Simulation sim = Simulation.instance();
        sim.testRun();

        UnitManager unitManager = sim.getUnitManager();
        
		// Create test settlement.
		settlement = new MockSettlement();	
		unitManager.addUnit(settlement);
    }

	/*
	 * Test container with a single resource
	 */
	public void testSingleResource() throws Exception {
		GenericContainer c = new GenericContainer("Bag", EquipmentType.BAG, settlement);
		
		int rockID = ResourceUtil.rockSamplesID;
		double bagCap = ContainerUtil.getContainerCapacity(EquipmentType.BAG);
		assertEquals("EMpty capacity", bagCap,
						c.getAmountResourceCapacity(rockID));
		assertEquals("Remaining capacity", bagCap,
				c.getAmountResourceRemainingCapacity(rockID));
		
		// Load oxygen
		double quantity = bagCap/2D;
		assertEquals("Full store", 0D, c.storeAmountResource(rockID, quantity));
		assertEquals("Stored", quantity, c.getAmountResourceStored(rockID));
		assertEquals("Remaining capacity after load", quantity, c.getAmountResourceRemainingCapacity(rockID));

		// Fully overload
		assertEquals("Overload stored excess", quantity, c.storeAmountResource(rockID, bagCap));
		assertEquals("Stored after overload", bagCap, c.getAmountResourceStored(rockID));
		assertEquals("Remaining capacity after overload", 0D, c.getAmountResourceRemainingCapacity(rockID));
	}
	
	/*
	 * Test container with 2 resources
	 */
	public void testTwoResource() throws Exception {
		GenericContainer c = new GenericContainer("Bag", EquipmentType.BAG, settlement);
		
		int rockID = ResourceUtil.rockSamplesID;
		double bagCap = ContainerUtil.getContainerCapacity(EquipmentType.BAG);
		
		// Load rock
		double quantity = bagCap/2D;
		c.storeAmountResource(rockID, quantity);
		assertEquals("Stored", quantity, c.getAmountResourceStored(rockID));
		assertEquals("Stored resource", rockID, c.getResource());
		
		// Attempt to load 2nd resource
		int secondResource = ResourceUtil.iceID;
		assertEquals("Stored 2nd resource", quantity, c.storeAmountResource(secondResource, quantity));
		assertEquals("2nd resource capacity", 0D, c.getAmountResourceStored(secondResource));
		assertEquals("2nd resource remaining capacity", 0D, c.getAmountResourceRemainingCapacity(secondResource));
	}
	
	/*
	 * Test container with 2 resources
	 */
	public void testEmptying() throws Exception {
		GenericContainer c = new GenericContainer("Bag", EquipmentType.BAG, settlement);
		
		int rockID = ResourceUtil.rockSamplesID;
		double bagCap = ContainerUtil.getContainerCapacity(EquipmentType.BAG);
		
		// Load rock
		double quantity = bagCap/2D;
		c.storeAmountResource(rockID, bagCap);
		assertEquals("Stored", bagCap, c.getAmountResourceStored(rockID));
		assertEquals("Partial Unload", 0D, c.retrieveAmountResource(rockID, quantity));
		assertEquals("Stored after partial unload", quantity, c.getAmountResourceStored(rockID));

		assertEquals("Full Unload", 0D, c.retrieveAmountResource(rockID, quantity));
		assertEquals("Stored after full unload", 0D, c.getAmountResourceStored(rockID));

		assertEquals("Excessive Unload", quantity, c.retrieveAmountResource(rockID, quantity));

		
		// Still fixed to original resource
		int secondResource = ResourceUtil.iceID;
		assertEquals("1st resource after unload", bagCap, c.getAmountResourceRemainingCapacity(rockID));
		assertEquals("2nd resource after unload", 0D, c.getAmountResourceRemainingCapacity(secondResource));
	}
}