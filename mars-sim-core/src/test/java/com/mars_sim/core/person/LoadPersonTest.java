/*
 * Mars Simulation Project
 * LoadPersonTest.java
 * @date 2023-06-10
 * @author Manny Kung
 */

package com.mars_sim.core.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
class LoadPersonTest extends MarsSimUnitTest {

	private Settlement settlement = null;
	private String name = "Jim Loader";
	private Person person;

	@BeforeEach
    void setUpPerson() {
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
	void testCarrying() {
		double capacity = Math.round(person.getCarryingCapacity() * 10D)/10D;		
		
		assertTrue(capacity > 0, person + " has no carrying capacity.");
		
		double amount = 1D;
				
		double remain0 = person.getRemainingSpecificCapacity(ResourceUtil.FOOD_ID);
		assertEquals(1.0, remain0, "Incorrect remaining capacity.");
		
		double excess = person.storeAmountResource(ResourceUtil.FOOD_ID, amount);	
		
		double remain1 = person.getRemainingSpecificCapacity(ResourceUtil.FOOD_ID);
		assertEquals(0.0, remain1, "Incorrect remaining capacity.");
		
		assertEquals(0.0, excess, "Can't carry " + amount + " kg of food.");
		
		double missing = person.retrieveAmountResource(ResourceUtil.FOOD_ID, amount);
		assertEquals(0.0, missing, "Can't retrieve food.");
	}
	
	/*
	 * Test if a person can transfer equipment.
	 */
	@Test
	void testTransferEquipment() {
		Equipment bag = EquipmentFactory.createEquipment(EquipmentType.BAG, settlement);

		boolean canTransfer = bag.transfer(person);
		
		assertTrue(canTransfer, bag + " cannot be transferred from " + settlement 
				+ " to " + person.getName() + ".");
	}
	
	/*
	 * Test if a person can be assigned a thermal bottle.
	 */
	@Test
	void testAssignThermalBottle() {
		Equipment bottle = EquipmentFactory.createEquipment(EquipmentType.THERMAL_BOTTLE, settlement);

		person.assignThermalBottle();
		
		boolean hasIt = person.hasThermalBottle();
		
		assertTrue(hasIt, bottle + " cannot be transferred from " + settlement 
				+ " to " + person.getName() + ".");
	}	
}