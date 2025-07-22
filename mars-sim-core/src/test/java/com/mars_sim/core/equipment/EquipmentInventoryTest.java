/*
 * Mars Simulation Project
 * EquipmentInventoryTest
 * @date 2023-06-12
 * @author Scott Davis
 */

package com.mars_sim.core.equipment;

import java.util.Set;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;

/**
 * Tests the EquipmentInventory.
 */
public class EquipmentInventoryTest extends AbstractMarsSimUnitTest {


	private static final double CAPACITY_AMOUNT = 1000D;
	private static final String PNEUMATIC_DRILL = "pneumatic drill";

	private Settlement settlement = null;

    @Override
    public void setUp() {
		super.setUp();

		settlement = buildSettlement();
    }

	/*
	 * Test method loading equipment with amount resources.
	 */
	public void testAmountInEquipmentLoading() {
		EquipmentInventory inv = new EquipmentInventory(settlement, CAPACITY_AMOUNT);
		int co2 = ResourceUtil.CO2_ID;
		int rock = ResourceUtil.ROCK_SAMPLES_ID;
		double co2Mass = CAPACITY_AMOUNT/10; // 100 kg
		double rockMass = CAPACITY_AMOUNT/20; // 50 kg
		
		// Store some CO2 directly and then a Bag containing rocks
		inv.storeAmountResource(co2, co2Mass);
		
		Equipment bag = EquipmentFactory.createEquipment(EquipmentType.BAG, settlement);
		
		double excess = ((Container)bag).storeAmountResource(rock, rockMass);
		
		assertEquals("Bag cannot store the rock.", 0D, excess);
				
		assertEquals("CO2 stored.", co2Mass, inv.getSpecificAmountResourceStored(co2));
		
		// Note: rock is stored inside a bag and not in settlement
		assertEquals("Rock stored.", 0D, inv.getSpecificAmountResourceStored(rock));
						
		assertEquals("Resources held in inventory.", Set.of(co2), inv.getSpecificResourceStoredIDs());

		// Remove some rock from bag
		bag.retrieveAmountResource(rock, rockMass/2);
		assertEquals("Rock after bag unload.", rockMass/2, bag.getSpecificAmountResourceStored(rock));
		
		// Remove the bag
		inv.removeEquipment(bag);
		assertEquals("Remaining capacity after bag remove.", CAPACITY_AMOUNT - co2Mass, inv.getRemainingSpecificCapacity(co2));
		assertEquals("Total mass after bag remove.", co2Mass, inv.getStoredMass());
		assertEquals("Resources held after bag remove.", Set.of(co2), inv.getSpecificResourceStoredIDs());
	}

	/*
	 * Test method loading amount resources.
	 */
	public void testAmountLoading() {
		EquipmentInventory inv = new EquipmentInventory(settlement, CAPACITY_AMOUNT);
		int resource = ResourceUtil.CO2_ID;

		double excess = inv.storeAmountResource(resource, CAPACITY_AMOUNT/2);
		System.out.println("excess: " + excess);
		
		assertEquals("No excess on 1st load", 0D, excess);
		
		double stored = inv.getSpecificAmountResourceStored(resource);
		System.out.println("stored: " + stored);
		
		assertEquals("Stored capacity after 1st load", CAPACITY_AMOUNT/2, stored);
		
		double cap = inv.getRemainingSpecificCapacity(resource);
		System.out.println("cap: " + cap);
				
		assertEquals("Remaining after 1st load capacity", CAPACITY_AMOUNT/2, cap);
		assertEquals("Total mass after 1st load", CAPACITY_AMOUNT/2, inv.getStoredMass());

		assertEquals("No excess on 2nd load", 0D, inv.storeAmountResource(resource, CAPACITY_AMOUNT/2));
		assertEquals("Stored capacity after 2nd load", CAPACITY_AMOUNT, inv.getSpecificAmountResourceStored(resource));
		assertEquals("Remaining after 2nd load capacity", 0D, inv.getRemainingSpecificCapacity(resource));
		assertEquals("Total mass after 2nd load", CAPACITY_AMOUNT, inv.getStoredMass());
	}

	/*
	 * Test method loading parts.
	 */
	public void testPartsOverloading() {
		Part drillPart = (Part) ItemResourceUtil.findItemResource(PNEUMATIC_DRILL);
		int maxDrills = 2;
		
		EquipmentInventory inv = new EquipmentInventory(settlement, drillPart.getMassPerItem() * 2);

		int returned = inv.storeItemResource(drillPart.getID(), maxDrills);

		assertEquals("No excess on capacity load", 0, returned);
		assertEquals("Stored Pneumatic Drills after load", maxDrills, inv.getItemResourceStored(drillPart.getID()));
		assertEquals("Stored mass after Pneumatic Drill load", maxDrills * drillPart.getMassPerItem(),
													inv.getStoredMass());

		// Try and load one more and should fail
		assertEquals("Excess on overload", 1, inv.storeItemResource(drillPart.getID(), 1));
	}


	/*
	 * Test method over-loading amount resources.
	 */
	public void testAmountOverloading() {
		int resource = ResourceUtil.CO2_ID;
		
		EquipmentInventory inv = new EquipmentInventory(settlement, 0);
	
		inv.setSpecificResourceCapacity(resource, CAPACITY_AMOUNT);

		double cargo = inv.getCargoCapacity();
		assertEquals("No cargo capacity", 0D, cargo);
		
		double cap = inv.getSpecificCapacity(resource);
		assertEquals("Check specific capacity", CAPACITY_AMOUNT, cap);
//		System.out.println("cap: " + cap);
		
		// Add half the capacity
		double excess = inv.storeAmountResource(resource, CAPACITY_AMOUNT/2);
		assertEquals("No excess on capacity load", 0D, excess);

		// Check amount stored
		double stored = inv.getSpecificAmountResourceStored(resource);
		assertEquals("Check specific amount stored", CAPACITY_AMOUNT/2, stored);
//		System.out.println("stored: " + stored);
		
		// Add twice the capacity
		excess = inv.storeAmountResource(resource, 2*CAPACITY_AMOUNT);
//		System.out.println("excess: " + excess);
		assertEquals("Excess on overload", CAPACITY_AMOUNT * 1.5, excess);
		
		stored = inv.getSpecificAmountResourceStored(resource);
		assertEquals("Stored capacity after overload", CAPACITY_AMOUNT, stored);
//		System.out.println("stored: " + stored);
		
		double remain = inv.getRemainingSpecificCapacity(resource);
//		System.out.println("remain: " + remain);
//		System.out.println("cap: " + cap);
		
		assertEquals("Remaining after overload", 0D, remain);
	}

	/*
	 * Test method unloading amount resources.
	 */
	public void testAmountUnloading() {
		EquipmentInventory inv = new EquipmentInventory(settlement, CAPACITY_AMOUNT);
		int resource = ResourceUtil.CO2_ID;
			
		double excess = inv.storeAmountResource(resource, CAPACITY_AMOUNT);
//		System.out.println("excess: " + excess);
		assertEquals("No Excess", 0.0, excess);
		
		assertEquals("Shortfall on 1st retrieve", 0D, inv.retrieveAmountResource(resource, CAPACITY_AMOUNT/2));
		
		double stored = inv.getSpecificAmountResourceStored(resource);
		System.out.println("stored: " + stored);
		
		assertEquals("Stored on 1st retrieve", CAPACITY_AMOUNT/2, stored);
		
		double remain = inv.getRemainingSpecificCapacity(resource);
		System.out.println("remain: " + remain);
		
		assertEquals("Remaining after 1st retrieve", CAPACITY_AMOUNT/2, remain);
		
		double mass = inv.getStoredMass();
		System.out.println("mass: " + mass);
		
		assertEquals("Total mass after 1st retrieve", CAPACITY_AMOUNT/2, mass);

		assertEquals("Shortfall on 2nd retrieve", 0D, inv.retrieveAmountResource(resource, CAPACITY_AMOUNT/2));
		
		stored = inv.getSpecificAmountResourceStored(resource);
		System.out.println("stored: " + stored);
		
		assertEquals("Stored on 2nd retrieve", 0D, stored);
		
		remain = inv.getRemainingSpecificCapacity(resource);
		System.out.println("remain: " + remain);
	
		assertEquals("Remaining after 2nd retrieve", CAPACITY_AMOUNT, remain);
		
		mass = inv.getStoredMass();
		System.out.println("mass: " + mass);
		
		assertEquals("Total mass after 2nd retrieve", 0.0, mass);

		assertEquals("Shortfall on empty inventory", 100D, inv.retrieveAmountResource(resource, 100D));
	}

	/*
	 * Test method loading combined amount resources.
	 */
	public void testMultiples() {
		EquipmentInventory inv = new EquipmentInventory(settlement, CAPACITY_AMOUNT);
		int resource = ResourceUtil.CO2_ID;
		int resource2  = ResourceUtil.OXYGEN_ID;

		// if Using general/cargo capacity instead of the dedicated capacity for a resource
		assertEquals("Remaining capacity 1st resource", CAPACITY_AMOUNT, inv.getRemainingCargoCapacity());
		assertEquals("Remaining capacity 2nd resource", CAPACITY_AMOUNT, inv.getRemainingCargoCapacity());

		inv.storeAmountResource(resource, CAPACITY_AMOUNT/2);
		inv.storeAmountResource(resource2, CAPACITY_AMOUNT/4);
		assertEquals("Total mass after combined load", (CAPACITY_AMOUNT/2 + CAPACITY_AMOUNT/4), inv.getStoredMass());
	}
}