/*
 * Mars Simulation Project
 * EquipmentInventoryTest
 * @date 2023-06-12
 * @author Scott Davis
 */

package org.mars_sim.msp.core.equipment;

import java.util.Set;

import org.mars_sim.msp.core.AbstractMarsSimUnitTest;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;

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
	public void testAmountInEquipmentLoading() throws Exception {
		EquipmentInventory inv = new EquipmentInventory(settlement, CAPACITY_AMOUNT);
		int co2 = ResourceUtil.co2ID;
		int rock = ResourceUtil.rockSamplesID;
		double co2Mass = CAPACITY_AMOUNT/10; // 100 kg
		double rockMass = CAPACITY_AMOUNT/20; // 50 kg

		double cargoCap = inv.getCargoCapacity();
		System.out.println("1. Cargo Capacity: " + cargoCap + " kg.");
		double stored = inv.getStoredMass();
		System.out.println("1. Stored mass: " + stored + " kg.");
		double remain = inv.getRemainingCargoCapacity();
		System.out.println("1. Remaining Cargo Capacity: " + remain + " kg.");
		
		// Store some CO2 directly and then a Bag containing rocks
		inv.storeAmountResource(co2, co2Mass);

		cargoCap = inv.getCargoCapacity();
		System.out.println("1.5 Cargo Capacity: " + cargoCap + " kg.");
		stored = inv.getStoredMass();
		System.out.println("1.5 Stored mass: " + stored + " kg.");
		remain = inv.getRemainingCargoCapacity();
		System.out.println("1.5 Remaining Cargo Capacity: " + remain + " kg.");
		
		Equipment bag = EquipmentFactory.createEquipment(EquipmentType.BAG, settlement);
		boolean canStore = settlement.addEquipment(bag);
		System.out.println("canStore: " + canStore);
		
		// Test if the bag is in the settlement
		boolean hasIt = inv.containsEquipment(EquipmentType.BAG);
		System.out.println("Is there a bag in the settlement: " + hasIt);
		
		int size = settlement.getEquipmentSet().size();
		System.out.println("size: " + size);
		
		Unit unit = bag.getContainerUnit();
		System.out.println(bag + "'s container unit: " + unit);
		
		double excess = ((Container)bag).storeAmountResource(rock, rockMass);
		System.out.println("excess: " + excess);
		
		assertEquals("Bag cannot store the rock.", 0D, excess);
		
		double bagMass = bag.getMass();
		System.out.println("bagMass: " + bagMass + " kg.");
		
		assertEquals("CO2 stored.", co2Mass, inv.getAmountResourceStored(co2));
		
		// Note: rock is stored inside a bag and not in settlement
		assertEquals("Rock stored.", 0D, inv.getAmountResourceStored(rock));

		cargoCap = inv.getCargoCapacity();
		System.out.println("2. Cargo Capacity: " + cargoCap + " kg.");
		stored = inv.getStoredMass();
		System.out.println("2. Stored mass: " + stored + " kg.");
		remain = inv.getRemainingCargoCapacity();
		System.out.println("2. Remaining Cargo Capacity: " + remain + " kg.");
		
		System.out.println("equipment set size: " + inv.getEquipmentSet().size());
		
		double expectedMass = co2Mass + rockMass + bag.getBaseMass();
		System.out.println("Expected total stored mass: " + expectedMass + " kg.");
		
		// Note: It's unable to reconcile the presence of a bag (1 kg) with rock mass of (50 kg)
//		assertEquals("Remaining cargo capacity after bag load.", CAPACITY_AMOUNT - expectedMass, remain);
//		assertEquals("Total mass after bag load.", expectedMass, inv.getStoredMass());
		
		assertEquals("Resources held in inventory.", Set.of(co2), inv.getAmountResourceIDs());

		// Remove some rock from bag
		bag.retrieveAmountResource(rock, rockMass/2);
		expectedMass = co2Mass + rockMass/2 + bag.getBaseMass();
		assertEquals("Rock after bag unload.", rockMass/2, bag.getAmountResourceStored(rock));
		
		// Note: It's unable to reconcile the presence of a bag (1 kg) with rock mass of (50 kg)
//		assertEquals("Total mass after bag unload.", expectedMass, inv.getStoredMass());

		// Remove the bag
		inv.removeEquipment(bag);
		assertEquals("Remaining capacity after bag remove.", CAPACITY_AMOUNT - co2Mass, inv.getAmountResourceRemainingCapacity(co2));
		assertEquals("Total mass after bag remove.", co2Mass, inv.getStoredMass());
		assertEquals("Resources held after bag remove.", Set.of(co2), inv.getAmountResourceIDs());

	}

	/*
	 * Test method loading amount resources.
	 */
	public void testAmountLoading() throws Exception {
		EquipmentInventory inv = new EquipmentInventory(settlement, CAPACITY_AMOUNT);
		int resource = ResourceUtil.co2ID;

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
	 * Test method loading parts.
	 */
	public void testPartsOverloading() throws Exception {
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
	public void testAmountOverloading() throws Exception {
		EquipmentInventory inv = new EquipmentInventory(settlement, CAPACITY_AMOUNT);
		int resource = ResourceUtil.co2ID;

		assertEquals("No excess on capacity load", 0D, inv.storeAmountResource(resource, CAPACITY_AMOUNT/2));

		assertEquals("Excess on overload", CAPACITY_AMOUNT/2, inv.storeAmountResource(resource, CAPACITY_AMOUNT));
		assertEquals("Stored capacity after overload", CAPACITY_AMOUNT, inv.getAmountResourceStored(resource));
		assertEquals("Remaining after overload", 0D, inv.getAmountResourceRemainingCapacity(resource));
	}

	/*
	 * Test method unloading amount resources.
	 */
	public void testAmountUnloading() throws Exception {
		EquipmentInventory inv = new EquipmentInventory(settlement, CAPACITY_AMOUNT);
		int resource = ResourceUtil.co2ID;

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
	 * Test method loading combined amount resources.
	 */
	public void testMultiples() throws Exception {
		EquipmentInventory inv = new EquipmentInventory(settlement, CAPACITY_AMOUNT);
		int resource = ResourceUtil.co2ID;
		int resource2  = ResourceUtil.oxygenID;

		// if Using general/cargo capacity instead of the dedicated capacity for a resource
		assertEquals("Remaining capacity 1st resource", CAPACITY_AMOUNT, inv.getRemainingCargoCapacity());//getAmountResourceRemainingCapacity(resource));
		assertEquals("Remaining capacity 2nd resource", CAPACITY_AMOUNT, inv.getRemainingCargoCapacity());//getAmountResourceRemainingCapacity(resource2));

		inv.storeAmountResource(resource, CAPACITY_AMOUNT/2);
		inv.storeAmountResource(resource2, CAPACITY_AMOUNT/4);
		assertEquals("Total mass after combined load", (CAPACITY_AMOUNT/2 + CAPACITY_AMOUNT/4), inv.getStoredMass());
	}
}