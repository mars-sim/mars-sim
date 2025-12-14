/*
 * Mars Simulation Project
 * LoadPersonTest.java
 * @date 2023-06-10
 * @author Manny Kung
 */

package com.mars_sim.core.person;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;

/**
 * Tests the ability of a person to carry resources.
 */
public class LoadPersonTest extends MarsSimUnitTest {

	private Settlement settlement = null;
	private String name = "Jim Loader";
	private Person person;

	@BeforeEach
    public void setUpPerson() {
		settlement = buildSettlement("mock");
		
		person = buildPerson(name, settlement);
		
		// Make the person strong to get loading quicker
		person.getNaturalAttributeManager().setAttribute(NaturalAttributeType.STRENGTH, 60);
		person.getNaturalAttributeManager().setAttribute(NaturalAttributeType.ENDURANCE, 60);
    }

	/*
	 * Test if a person can carry resources.
	 */
	@Test
	public void testCarrying() throws Exception {
		double capacity = Math.round(person.getCarryingCapacity() * 10D)/10D;
		System.out.println(person + " has a carrying capacity of " + capacity + " kg.");
		
		double weight = Math.round(person.getMass() * 10D)/10D;
		System.out.println(person + " weighs " + weight + " kg.");
		
		assertTrue(capacity > 0, person + " has no carrying capacity.");
		
		double amount = 1D;
		
		double cap = person.getSpecificCapacity(ResourceUtil.FOOD_ID);
		System.out.println(person + " has a capacity of " + cap + " kg for food.");
		
		double remain0 = person.getRemainingSpecificCapacity(ResourceUtil.FOOD_ID);
		System.out.println("1. " + person + " has a remaining capacity of " + remain0 + " kg for food.");
		assertTrue(remain0 == 1.0, "Incorrect remaining capacity.");
		
		double excess = person.storeAmountResource(ResourceUtil.FOOD_ID, amount);	
		
		double remain1 = person.getRemainingSpecificCapacity(ResourceUtil.FOOD_ID);
		System.out.println("2. " + person + " has a remaining capacity of " + remain1 + " kg for food.");
		assertTrue(remain1 == 0.0, "Incorrect remaining capacity.");
		
		assertTrue(excess == 0.0, "Can't carry " + amount + " kg of food.");
		
		double missing = person.retrieveAmountResource(ResourceUtil.FOOD_ID, amount);
		assertTrue(missing == 0.0, "Can't retrieve food.");
	}
	
	/*
	 * Test if a person can transfer equipment.
	 */
	@Test
	public void testTransferEquipment() {
		Equipment bag = EquipmentFactory.createEquipment(EquipmentType.BAG, settlement);

		boolean canTransfer = bag.transfer(person);
		
		assertTrue(canTransfer, bag + " cannot be transferred from " + settlement 
				+ " to " + person.getName() + ".");
	}
	
	/*
	 * Test if a person can be assigned a thermal bottle.
	 */
	@Test
	public void testAssignThermalBottle() {
		Equipment bottle = EquipmentFactory.createEquipment(EquipmentType.THERMAL_BOTTLE, settlement);

		person.assignThermalBottle();
		
		boolean hasIt = person.hasThermalBottle();
		
		assertTrue(hasIt, bottle + " cannot be transferred from " + settlement 
				+ " to " + person.getName() + ".");
	}
	
}
