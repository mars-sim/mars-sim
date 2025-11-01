/*
 * Mars Simulation Project
 * EquipmentInventoryTest
 * @date 2023-06-12
 * @author Scott Davis
 */

package com.mars_sim.core.equipment;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.test.MarsSimUnitTest;

/**
 * Tests the EquipmentInventory.
 */
class EquipmentInventoryTest extends MarsSimUnitTest {


	private static final double CAPACITY_AMOUNT = 1000D;
	private static final String PNEUMATIC_DRILL = "pneumatic drill";

	private Settlement settlement = null;

    @BeforeEach
    public void init() {
		super.init();

		settlement = buildSettlement("Eqm Inv");
    }

	/*
	 * Test method loading equipment with amount resources.
	 */
	@Test
	void testAmountInEquipmentLoading() {
		EquipmentInventory inv = new EquipmentInventory(settlement, CAPACITY_AMOUNT);
		int co2 = ResourceUtil.CO2_ID;
		int rock = ResourceUtil.ROCK_SAMPLES_ID;
		double co2Mass = CAPACITY_AMOUNT/10; // 100 kg
		double rockMass = CAPACITY_AMOUNT/20; // 50 kg
		
		// Store some CO2 directly and then a Bag containing rocks
		inv.storeAmountResource(co2, co2Mass);
		
		Equipment bag = EquipmentFactory.createEquipment(EquipmentType.BAG, settlement);
		
		double excess = ((Container)bag).storeAmountResource(rock, rockMass);
		
		assertEquals(0D, excess, "Bag cannot store the rock.");
				
		assertEquals(co2Mass, inv.getSpecificAmountResourceStored(co2), "CO2 stored.");
		
		// Note: rock is stored inside a bag and not in settlement
		assertEquals(0D, inv.getSpecificAmountResourceStored(rock), "Rock stored.");
						
		assertEquals(Set.of(co2), inv.getSpecificResourceStoredIDs(), "Resources held in inventory.");

		// Remove some rock from bag
		bag.retrieveAmountResource(rock, rockMass/2);
		assertEquals(rockMass/2, bag.getSpecificAmountResourceStored(rock), "Rock after bag unload.");
		
		// Remove the bag
		inv.removeEquipment(bag);
		assertEquals(CAPACITY_AMOUNT - co2Mass, inv.getRemainingSpecificCapacity(co2), "Remaining capacity after bag remove.");
		assertEquals(co2Mass, inv.getStoredMass(), "Total mass after bag remove.");
		assertEquals(Set.of(co2), inv.getSpecificResourceStoredIDs(), "Resources held after bag remove.");
	}

	/*
	 * Test method loading amount resources.
	 */
	@Test
	void testAmountLoading() {
		EquipmentInventory inv = new EquipmentInventory(settlement, CAPACITY_AMOUNT);
		int resource = ResourceUtil.CO2_ID;

		double excess = inv.storeAmountResource(resource, CAPACITY_AMOUNT/2);
//		System.out.println("excess: " + excess);
		
		assertEquals(0D, excess, "No excess on 1st load");
		
		double stored = inv.getSpecificAmountResourceStored(resource);
//		System.out.println("stored: " + stored);
		
		assertEquals(CAPACITY_AMOUNT/2, stored, "Stored capacity after 1st load");
		
		double cap = inv.getRemainingSpecificCapacity(resource);
//		System.out.println("cap: " + cap);
				
		assertEquals(CAPACITY_AMOUNT/2, cap, "Remaining after 1st load capacity");
		assertEquals(CAPACITY_AMOUNT/2, inv.getStoredMass(), "Total mass after 1st load");

		assertEquals(0D, inv.storeAmountResource(resource, CAPACITY_AMOUNT/2), "No excess on 2nd load");
		assertEquals(CAPACITY_AMOUNT, inv.getSpecificAmountResourceStored(resource), "Stored capacity after 2nd load");
		assertEquals(0D, inv.getRemainingSpecificCapacity(resource), "Remaining after 2nd load capacity");
		assertEquals(CAPACITY_AMOUNT, inv.getStoredMass(), "Total mass after 2nd load");
	}

	/*
	 * Test method loading parts.
	 */
	@Test
	void testPartsOverloading() {
		Part drillPart = (Part) ItemResourceUtil.findItemResource(PNEUMATIC_DRILL);
		int maxDrills = 2;
		
		EquipmentInventory inv = new EquipmentInventory(settlement, drillPart.getMassPerItem() * 2);

		int returned = inv.storeItemResource(drillPart.getID(), maxDrills);

		assertEquals(0, returned, "No excess on capacity load");
		assertEquals(maxDrills, inv.getItemResourceStored(drillPart.getID()), "Stored Pneumatic Drills after load");
		assertEquals(maxDrills * drillPart.getMassPerItem(), inv.getStoredMass(), "Stored mass after Pneumatic Drill load");

		// Try and load one more and should fail
		assertEquals(1, inv.storeItemResource(drillPart.getID(), 1), "Excess on overload");
	}


	/*
	 * Test method over-loading amount resources.
	 */
	@Test
	void testAmountOverloading() {
		int resource = ResourceUtil.CO2_ID;
		
		EquipmentInventory inv = new EquipmentInventory(settlement, 0);
		inv.setSpecificResourceCapacity(resource, CAPACITY_AMOUNT);
		
		double sCap = inv.getSpecificCapacity(resource);
		assertEquals(CAPACITY_AMOUNT, sCap, "Check specific capacity");
		
		double cargo = inv.getCargoCapacity();
		assertEquals(0D, cargo, "No cargo capacity");
		
		double stock = inv.getStockCapacity();
		assertEquals(0D, stock, "No stock capacity");
		
		double rCCap = inv.getRemainingCombinedCapacity(resource);
		assertEquals(CAPACITY_AMOUNT, rCCap, "Check remaining combined capacity");
			
		// Add half the capacity
		double excess = inv.storeAmountResource(resource, CAPACITY_AMOUNT/2);
		assertEquals(0D, excess, "No excess on capacity load");
		
		// Check amount stored
		double stored = inv.getSpecificAmountResourceStored(resource);
		assertEquals(CAPACITY_AMOUNT/2, stored, "Check specific amount stored");
		
		// Add twice the capacity
		excess = inv.storeAmountResource(resource, 2*CAPACITY_AMOUNT);
		assertEquals(CAPACITY_AMOUNT * 1.5, excess, "Excess on overload");
		
		stored = inv.getSpecificAmountResourceStored(resource);
		assertEquals(CAPACITY_AMOUNT, stored, "Stored capacity after overload");
		
		double rsCap = inv.getRemainingSpecificCapacity(resource);
		
		assertEquals(0D, rsCap, "Remaining after overload");
	}

	/*
	 * Test method unloading amount resources.
	 */
	@Test
	void testAmountUnloading() {
		EquipmentInventory inv = new EquipmentInventory(settlement, CAPACITY_AMOUNT);
		int resource = ResourceUtil.CO2_ID;
			
		double excess = inv.storeAmountResource(resource, CAPACITY_AMOUNT);
		assertEquals(0.0, excess, "No Excess");
		
		assertEquals(0D, inv.retrieveAmountResource(resource, CAPACITY_AMOUNT/2), "Shortfall on 1st retrieve");
		
		double stored = inv.getSpecificAmountResourceStored(resource);
		
		assertEquals(CAPACITY_AMOUNT/2, stored, "Stored on 1st retrieve");
		
		double remain = inv.getRemainingSpecificCapacity(resource);
		
		assertEquals(CAPACITY_AMOUNT/2, remain, "Remaining after 1st retrieve");
		
		double mass = inv.getStoredMass();
		
		assertEquals(CAPACITY_AMOUNT/2, mass, "Total mass after 1st retrieve");

		assertEquals(0D, inv.retrieveAmountResource(resource, CAPACITY_AMOUNT/2), "Shortfall on 2nd retrieve");
		
		stored = inv.getSpecificAmountResourceStored(resource);
		
		assertEquals(0D, stored, "Stored on 2nd retrieve");
		
		remain = inv.getRemainingSpecificCapacity(resource);
	
		assertEquals(CAPACITY_AMOUNT, remain, "Remaining after 2nd retrieve");
		
		mass = inv.getStoredMass();
		
		assertEquals(0.0, mass, "Total mass after 2nd retrieve");

		assertEquals(100D, inv.retrieveAmountResource(resource, 100D), "Shortfall on empty inventory");
	}

	/*
	 * Test method loading combined amount resources.
	 */
	@Test
	void testMultiples() {
		EquipmentInventory inv = new EquipmentInventory(settlement, CAPACITY_AMOUNT);
		int resource = ResourceUtil.CO2_ID;
		int resource2  = ResourceUtil.OXYGEN_ID;

		// if Using general/cargo capacity instead of the dedicated capacity for a resource
		assertEquals(CAPACITY_AMOUNT, inv.getRemainingCargoCapacity(), "Remaining capacity 1st resource");
		assertEquals(CAPACITY_AMOUNT, inv.getRemainingCargoCapacity(), "Remaining capacity 2nd resource");

		inv.storeAmountResource(resource, CAPACITY_AMOUNT/2);
		inv.storeAmountResource(resource2, CAPACITY_AMOUNT/4);
		assertEquals((CAPACITY_AMOUNT/2 + CAPACITY_AMOUNT/4), inv.getStoredMass(), "Total mass after combined load");
	}
}