/*
 * Mars Simulation Project
 * GenericContainerTest.java
 * @date 2021-10-14
 * @author Barry Evans
 */

package com.mars_sim.core.equipment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;


import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.resource.PhaseType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.MockSettlement;
import com.mars_sim.core.structure.Settlement;



/**
 * Tests the logic of a Generic container
 */
public class GenericContainerTest {

	// Extra amount to add to resource to handle double arithmetic mismatch
	private Settlement settlement;
	
	@BeforeEach

	


	
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
	@Test

	public void testAssociatedSettlement() {
		GenericContainer c = new GenericContainer("Bag", EquipmentType.BAG, false, settlement);
	
		Settlement as = c.getAssociatedSettlement();
		assertEquals(as, settlement, "Associated Settlement");
	}
	
	/*
	 * Tests container with a single resource.
	 */
	@Test

	public void testSingleResource() {
		GenericContainer c = new GenericContainer("Bag", EquipmentType.BAG, false, settlement);
		
		int rockID = ResourceUtil.ROCK_SAMPLES_ID;
		double bagCap = ContainerUtil.getContainerCapacity(EquipmentType.BAG);
		assertEquals(bagCap, c.getSpecificCapacity(rockID), "EMpty capacity");
		assertEquals(bagCap, c.getRemainingCombinedCapacity(rockID), "Remaining capacity");
		
		// Load oxygen
		double quantity = bagCap/2D;
		assertEquals(0D, c.storeAmountResource(rockID, quantity), "Full store");
		assertEquals(quantity, c.getSpecificAmountResourceStored(rockID), "Stored");
		assertEquals(quantity, c.getRemainingCombinedCapacity(rockID), "Remaining capacity after load");

		// Fully overload
		assertEquals(quantity, c.storeAmountResource(rockID, bagCap), "Overload stored excess");
		assertEquals(bagCap, c.getSpecificAmountResourceStored(rockID), "Stored after overload");
		assertEquals(0D, c.getRemainingCombinedCapacity(rockID), "Remaining capacity after overload");
	}
	
	/*
	 * Tests container with 2 resources.
	 */
	@Test

	public void testTwoResource() {
		GenericContainer c = new GenericContainer("Bag", EquipmentType.BAG, false, settlement);
		
		int rockID = ResourceUtil.ROCK_SAMPLES_ID;
		double bagCap = ContainerUtil.getContainerCapacity(EquipmentType.BAG);
		
		// Load rock
		double quantity = bagCap/2D;
		c.storeAmountResource(rockID, quantity);
		assertEquals(quantity, c.getSpecificAmountResourceStored(rockID), "Stored");
		assertEquals(rockID, c.getResource(), "Stored resource");
		
		// Attempt to load 2nd resource
		int secondResource = ResourceUtil.ICE_ID;
		assertEquals(quantity, c.storeAmountResource(secondResource, quantity), "Stored 2nd resource");
		assertEquals(0D, c.getSpecificAmountResourceStored(secondResource), "2nd resource capacity");
		assertEquals(0D, c.getRemainingCombinedCapacity(secondResource), "2nd resource remaining capacity");
	}
	
	/*
	 * Tests container with 2 resources.
	 */
	@Test

	public void testEmptying() {
		GenericContainer c = new GenericContainer("Bag", EquipmentType.BAG, false, settlement);
		
		int rockID = ResourceUtil.ROCK_SAMPLES_ID;
		double bagCap = ContainerUtil.getContainerCapacity(EquipmentType.BAG);
		
		// Load rock
		double quantity = bagCap/2D;
		c.storeAmountResource(rockID, bagCap);
		assertEquals(bagCap, c.getSpecificAmountResourceStored(rockID), "Stored");
		assertEquals(0D, c.retrieveAmountResource(rockID, quantity), "Partial Unload");
		assertEquals(quantity, c.getSpecificAmountResourceStored(rockID), "Stored after partial unload");

		assertEquals(0D, c.retrieveAmountResource(rockID, quantity), "Full Unload");
		assertEquals(0D, c.getSpecificAmountResourceStored(rockID), "Stored after full unload");

		assertEquals(quantity, c.retrieveAmountResource(rockID, quantity), "Excessive Unload");

		
		// Still fixed to original resource
		int secondResource = ResourceUtil.ICE_ID;
		assertEquals(bagCap, c.getRemainingCombinedCapacity(rockID), "1st resource after unload");
		assertEquals(0D, c.getRemainingCombinedCapacity(secondResource), "2nd resource after unload");
	}
	
	/*
	 * Tests container with 2 resources.
	 */
	@Test

	public void testNoneReusable() {
		GenericContainer c = new GenericContainer("Bag", EquipmentType.BAG, false, settlement);
		
		int secondResource = ResourceUtil.ICE_ID;
		int rockID = ResourceUtil.ROCK_SAMPLES_ID;
		double bagCap = ContainerUtil.getContainerCapacity(EquipmentType.BAG);
		
		// Load rock
		c.storeAmountResource(rockID, bagCap);
		c.retrieveAmountResource(rockID, bagCap);
		assertEquals(bagCap, c.getRemainingCombinedCapacity(rockID), "Capacity of prime resource after full unload");
		assertEquals(0D, c.getRemainingCombinedCapacity(secondResource), "Capacity of secondary resource after full unload");
	}
	
	
	/*
	 * Tests container with 2 resources.
	 */
	@Test

	public void testReusable() {
		GenericContainer c = new GenericContainer("Specimen", EquipmentType.SPECIMEN_BOX, true, settlement);
		
		int secondResource = ResourceUtil.ICE_ID;
		int rockID = ResourceUtil.ROCK_SAMPLES_ID;
		double bagCap = ContainerUtil.getContainerCapacity(EquipmentType.SPECIMEN_BOX);
		
		// Load rock
		c.storeAmountResource(rockID, bagCap);
		c.retrieveAmountResource(rockID, bagCap);
		
		// Both should be back to full capacity
		assertEquals(bagCap, c.getRemainingCombinedCapacity(rockID), "Capacity of prime resource after full unload");
		assertEquals(bagCap, c.getRemainingCombinedCapacity(secondResource), "Capacity of secondary resource after full unload");
	}

	/*
	 * Tests container with Liquids & Solids.
	 */
	@Test

	public void testBarrelLiquid() {
		EquipmentType cType = EquipmentType.BARREL;
		GenericContainer c = new GenericContainer(cType.getName(), cType, true, settlement);

		double cap = ContainerUtil.getContainerCapacity(cType);
		int	allowedId1 = ResourceUtil.WATER_ID;
		int failedId1 = ResourceUtil.OXYGEN_ID;
		int allowedId2 = ResourceUtil.REGOLITHB_ID;
		
		// Test negatives first
		assertPhaseNotSupported(c, failedId1);
		
		assertEquals(cap, c.getRemainingCombinedCapacity(allowedId1), "Container capacity 1");
		assertEquals(cap, c.getRemainingCombinedCapacity(allowedId2), "Container capacity 2");
		
		// Check the correct resource can be stored
		c.storeAmountResource(allowedId1, cap/2);
		
		// Both should be back to full capacity
		assertEquals(allowedId1, c.getResource(), "Container " + c.getName() + " resource stored");
		assertEquals(cap/2, c.getStoredMass(), "Container " + c.getName() + " stored");
	}
	
	/*
	 * Test container with Gas.
	 */
	@Test

	public void testCanisterGas() {
		assertPhaseSupported(EquipmentType.GAS_CANISTER, PhaseType.GAS);
	}
	
	/*
	 * Test container with Solids.
	 */
	@Test

	public void testLargeBagSolid() {
		assertPhaseSupported(EquipmentType.LARGE_BAG, PhaseType.SOLID);
	}
	
	@Test

	
	public void testBoxSolid() {
		assertPhaseSupported(EquipmentType.SPECIMEN_BOX, PhaseType.SOLID);
	}
	
	@Test

	
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

		assertEquals(cap, c.getRemainingCombinedCapacity(allowedId), "Container capacity");
		
		// Check the correct resource can be stored
		c.storeAmountResource(allowedId, cap/2);
		
		// Both should be back to full capacity
		assertEquals(allowedId, c.getResource(), "Container " + c.getName() + " resource stored");
		assertEquals(cap/2, c.getStoredMass(), "Container " + c.getName() + " stored");
	}
	
	/**
	 * Tests phase support.
	 * 
	 * @param c
	 * @param resourceId
	 */
	private void assertPhaseNotSupported(GenericContainer c, int resourceId) {
		double cap = ContainerUtil.getContainerCapacity(c.getEquipmentType());

		assertEquals(0D, c.getRemainingCombinedCapacity(resourceId), "Container no capacity");
		
		// Load resource
		assertThrows(IllegalArgumentException.class, () -> {
				c.storeAmountResource(resourceId, cap/2);
		    });
		  
		// Both should be back to full capacity
		assertEquals(-1, c.getResource(), "Container " + c.getName() + " has no resource");
		assertEquals(0D, c.getStoredMass(), "Container " + c.getName() + " nothing stored");
	}
}