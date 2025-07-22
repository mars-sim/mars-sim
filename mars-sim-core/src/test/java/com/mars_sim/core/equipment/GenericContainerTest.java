/*
 * Mars Simulation Project
 * GenericContainerTest.java
 * @date 2021-10-14
 * @author Barry Evans
 */

package com.mars_sim.core.equipment;

import static org.junit.Assert.assertThrows;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.resource.PhaseType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.MockSettlement;
import com.mars_sim.core.structure.Settlement;

import junit.framework.TestCase;

/**
 * Tests the logic of a Generic container
 */
public class GenericContainerTest
extends TestCase {

	// Extra amount to add to resource to handle double arithmetic mismatch
	private Settlement settlement;
	
	@Override
    public void setUp() {
		// Create new simulation instance.
        SimulationConfig.loadConfig();
        
        Simulation sim = Simulation.instance();
        sim.testRun();

        UnitManager unitManager = sim.getUnitManager();
        
		// Create test settlement.
		settlement = new MockSettlement();	
		unitManager.addUnit(settlement);
    }

	/*
	 * Tests container associated settlement assignment.
	 */
	public void testAssociatedSettlement() {
		GenericContainer c = new GenericContainer("Bag", EquipmentType.BAG, false, settlement);
	
		Settlement as = c.getAssociatedSettlement();
		assertEquals("Associated Settlement", as, settlement);
	}
	
	/*
	 * Tests container with a single resource.
	 */
	public void testSingleResource() {
		GenericContainer c = new GenericContainer("Bag", EquipmentType.BAG, false, settlement);
		
		int rockID = ResourceUtil.ROCK_SAMPLES_ID;
		double bagCap = ContainerUtil.getContainerCapacity(EquipmentType.BAG);
		assertEquals("EMpty capacity", bagCap,
						c.getSpecificCapacity(rockID));
		assertEquals("Remaining capacity", bagCap,
				c.getRemainingCombinedCapacity(rockID));
		
		// Load oxygen
		double quantity = bagCap/2D;
		assertEquals("Full store", 0D, c.storeAmountResource(rockID, quantity));
		assertEquals("Stored", quantity, c.getSpecificAmountResourceStored(rockID));
		assertEquals("Remaining capacity after load", quantity, c.getRemainingCombinedCapacity(rockID));

		// Fully overload
		assertEquals("Overload stored excess", quantity, c.storeAmountResource(rockID, bagCap));
		assertEquals("Stored after overload", bagCap, c.getSpecificAmountResourceStored(rockID));
		assertEquals("Remaining capacity after overload", 0D, c.getRemainingCombinedCapacity(rockID));
	}
	
	/*
	 * Tests container with 2 resources.
	 */
	public void testTwoResource() {
		GenericContainer c = new GenericContainer("Bag", EquipmentType.BAG, false, settlement);
		
		int rockID = ResourceUtil.ROCK_SAMPLES_ID;
		double bagCap = ContainerUtil.getContainerCapacity(EquipmentType.BAG);
		
		// Load rock
		double quantity = bagCap/2D;
		c.storeAmountResource(rockID, quantity);
		assertEquals("Stored", quantity, c.getSpecificAmountResourceStored(rockID));
		assertEquals("Stored resource", rockID, c.getResource());
		
		// Attempt to load 2nd resource
		int secondResource = ResourceUtil.ICE_ID;
		assertEquals("Stored 2nd resource", quantity, c.storeAmountResource(secondResource, quantity));
		assertEquals("2nd resource capacity", 0D, c.getSpecificAmountResourceStored(secondResource));
		assertEquals("2nd resource remaining capacity", 0D, c.getRemainingCombinedCapacity(secondResource));
	}
	
	/*
	 * Tests container with 2 resources.
	 */
	public void testEmptying() {
		GenericContainer c = new GenericContainer("Bag", EquipmentType.BAG, false, settlement);
		
		int rockID = ResourceUtil.ROCK_SAMPLES_ID;
		double bagCap = ContainerUtil.getContainerCapacity(EquipmentType.BAG);
		
		// Load rock
		double quantity = bagCap/2D;
		c.storeAmountResource(rockID, bagCap);
		assertEquals("Stored", bagCap, c.getSpecificAmountResourceStored(rockID));
		assertEquals("Partial Unload", 0D, c.retrieveAmountResource(rockID, quantity));
		assertEquals("Stored after partial unload", quantity, c.getSpecificAmountResourceStored(rockID));

		assertEquals("Full Unload", 0D, c.retrieveAmountResource(rockID, quantity));
		assertEquals("Stored after full unload", 0D, c.getSpecificAmountResourceStored(rockID));

		assertEquals("Excessive Unload", quantity, c.retrieveAmountResource(rockID, quantity));

		
		// Still fixed to original resource
		int secondResource = ResourceUtil.ICE_ID;
		assertEquals("1st resource after unload", bagCap, c.getRemainingCombinedCapacity(rockID));
		assertEquals("2nd resource after unload", 0D, c.getRemainingCombinedCapacity(secondResource));
	}
	
	/*
	 * Tests container with 2 resources.
	 */
	public void testNoneReusable() {
		GenericContainer c = new GenericContainer("Bag", EquipmentType.BAG, false, settlement);
		
		int secondResource = ResourceUtil.ICE_ID;
		int rockID = ResourceUtil.ROCK_SAMPLES_ID;
		double bagCap = ContainerUtil.getContainerCapacity(EquipmentType.BAG);
		
		// Load rock
		c.storeAmountResource(rockID, bagCap);
		c.retrieveAmountResource(rockID, bagCap);
		assertEquals("Capacity of prime resource after full unload", bagCap, c.getRemainingCombinedCapacity(rockID));
		assertEquals("Capacity of secondary resource after full unload", 0D, c.getRemainingCombinedCapacity(secondResource));
	}
	
	
	/*
	 * Tests container with 2 resources.
	 */
	public void testReusable() {
		GenericContainer c = new GenericContainer("Specimen", EquipmentType.SPECIMEN_BOX, true, settlement);
		
		int secondResource = ResourceUtil.ICE_ID;
		int rockID = ResourceUtil.ROCK_SAMPLES_ID;
		double bagCap = ContainerUtil.getContainerCapacity(EquipmentType.SPECIMEN_BOX);
		
		// Load rock
		c.storeAmountResource(rockID, bagCap);
		c.retrieveAmountResource(rockID, bagCap);
		
		// Both should be back to full capacity
		assertEquals("Capacity of prime resource after full unload", bagCap, c.getRemainingCombinedCapacity(rockID));
		assertEquals("Capacity of secondary resource after full unload", bagCap, c.getRemainingCombinedCapacity(secondResource));
	}

	/*
	 * Tests container with Liquids & Solids.
	 */
	public void testBarrelLiquid() {
		EquipmentType cType = EquipmentType.BARREL;
		GenericContainer c = new GenericContainer(cType.getName(), cType, true, settlement);

		double cap = ContainerUtil.getContainerCapacity(cType);
		int	allowedId1 = ResourceUtil.WATER_ID;
		int failedId1 = ResourceUtil.OXYGEN_ID;
		int allowedId2 = ResourceUtil.REGOLITHB_ID;
		
		// Test negatives first
		assertPhaseNotSupported(c, failedId1);
		
		assertEquals("Container capacity 1", cap, c.getRemainingCombinedCapacity(allowedId1));
		assertEquals("Container capacity 2", cap, c.getRemainingCombinedCapacity(allowedId2));
		
		// Check the correct resource can be stored
		c.storeAmountResource(allowedId1, cap/2);
		
		// Both should be back to full capacity
		assertEquals("Container " + c.getName() + " resource stored", allowedId1, c.getResource());
		assertEquals("Container " + c.getName() + " stored", cap/2, c.getStoredMass());
	}
	
	/*
	 * Test container with Gas.
	 */
	public void testCanisterGas() {
		assertPhaseSupported(EquipmentType.GAS_CANISTER, PhaseType.GAS);
	}
	
	/*
	 * Test container with Solids.
	 */
	public void testLargeBagSolid() {
		assertPhaseSupported(EquipmentType.LARGE_BAG, PhaseType.SOLID);
	}
	
	public void testBoxSolid() {
		assertPhaseSupported(EquipmentType.SPECIMEN_BOX, PhaseType.SOLID);
	}
	
	public void testBagSolid() {
		assertPhaseSupported(EquipmentType.BAG, PhaseType.SOLID);
	}
	
	/**
	 * Test that a specific container type can only support the specific PhaseType.
	 * 
	 * @param cType
	 * @param required
	 */
	private void assertPhaseSupported(EquipmentType cType, PhaseType required) {
		GenericContainer c = new GenericContainer(cType.getName(), cType, true, settlement);

		double cap = ContainerUtil.getContainerCapacity(cType);
		int allowedId = 0;
		int failedId1 = 0;
		int failedId2 = 0;
		switch(required) {
		case LIQUID:
			allowedId = ResourceUtil.WATER_ID;
			failedId1 = ResourceUtil.OXYGEN_ID;
			failedId2 = ResourceUtil.REGOLITHB_ID;
			break;
		
		case GAS:
			allowedId = ResourceUtil.OXYGEN_ID;
			failedId1 = ResourceUtil.WATER_ID;
			failedId2 = ResourceUtil.REGOLITHB_ID;
			break;
			
		case SOLID:
			allowedId = ResourceUtil.REGOLITHB_ID;
			failedId1 = ResourceUtil.OXYGEN_ID;
			failedId2 = ResourceUtil.WATER_ID;
			break;
		}
		
		// Test negatives first
		assertPhaseNotSupported(c, failedId1);
		assertPhaseNotSupported(c, failedId2);

		assertEquals("Container capacity", cap, c.getRemainingCombinedCapacity(allowedId));
		
		// Check the correct resource can be stored
		c.storeAmountResource(allowedId, cap/2);
		
		// Both should be back to full capacity
		assertEquals("Container " + c.getName() + " resource stored", allowedId, c.getResource());
		assertEquals("Container " + c.getName() + " stored", cap/2, c.getStoredMass());
	}
	
	/**
	 * Tests phase support.
	 * 
	 * @param c
	 * @param resourceId
	 */
	private void assertPhaseNotSupported(GenericContainer c, int resourceId) {
		double cap = ContainerUtil.getContainerCapacity(c.getEquipmentType());

		assertEquals("Container no capacity", 0D, c.getRemainingCombinedCapacity(resourceId));
		
		// Load resource
		assertThrows(IllegalArgumentException.class, () -> {
				c.storeAmountResource(resourceId, cap/2);
		    });
		  
		// Both should be back to full capacity
		assertEquals("Container " + c.getName() + " has no resource", -1, c.getResource());
		assertEquals("Container " + c.getName() + " nothing stored", 0D, c.getStoredMass());
	}
}