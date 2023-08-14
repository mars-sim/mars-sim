/*
 * Mars Simulation Project
 * LoadPersonTest.java
 * @date 2023-06-10
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import org.mars_sim.msp.core.AbstractMarsSimUnitTest;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Tests the ability of a person to carry resources.
 */
public class LoadPersonTest extends AbstractMarsSimUnitTest {

	private Settlement settlement = null;
	private String name = "Jim Loader";
	private Person person;

	@Override
    public void setUp() {
		super.setUp();

		settlement = buildSettlement();
		
		person = buildPerson(name, settlement);
		
		// Make the person strong to get loading quicker
		person.getNaturalAttributeManager().setAttribute(NaturalAttributeType.STRENGTH, 60);
		person.getNaturalAttributeManager().setAttribute(NaturalAttributeType.ENDURANCE, 60);
    }

	/*
	 * Test if a person can carry resources.
	 */
	public void testCarrying() throws Exception {
		double capacity = Math.round(person.getCarryingCapacity() * 10D)/10D;
		System.out.println(person + " has a carrying capacity of " + capacity + " kg.");
		
		double weight = Math.round(person.getMass() * 10D)/10D;
		System.out.println(person + " weighs " + weight + " kg.");
		
		assertTrue(person + " has no carrying capacity.", capacity > 0);
		
		double amount = 1D;
		
		double cap = person.getAmountResourceCapacity(ResourceUtil.foodID);
		System.out.println(person + " has a capacity of " + cap + " kg for food.");
		
		double remain0 = person.getAmountResourceRemainingCapacity(ResourceUtil.foodID);
		System.out.println("1. " + person + " has a remaining capacity of " + remain0 + " kg for food.");
		assertTrue("Incorrect remaining capacity.", remain0 == 1.0);
		
		double excess = person.storeAmountResource(ResourceUtil.foodID, amount);	
		
		double remain1 = person.getAmountResourceRemainingCapacity(ResourceUtil.foodID);
		System.out.println("2. " + person + " has a remaining capacity of " + remain1 + " kg for food.");
		assertTrue("Incorrect remaining capacity.", remain1 == 0.0);
		
		assertTrue("Can't carry " + amount + " kg of food.", excess == 0.0);
		
		double missing = person.retrieveAmountResource(ResourceUtil.foodID, amount);
		assertTrue("Can't retrieve food.", missing == 0.0);
	}
	
	/*
	 * Test if a person can transfer equipment.
	 */
	public void testTransferEquipment() {
		Equipment bag = EquipmentFactory.createEquipment(EquipmentType.BAG, settlement);

		boolean canTransfer = bag.transfer(person);
		
		assertTrue(bag + " cannot be transferred from " + settlement 
				+ " to " + person.getName() + ".", canTransfer);
	}
	
	/*
	 * Test if a person can be assigned a thermal bottle.
	 */
	public void testAssignThermalBottle() {
		Equipment bottle = EquipmentFactory.createEquipment(EquipmentType.THERMAL_BOTTLE, settlement);

		person.assignThermalBottle();
		
		boolean hasIt = person.hasThermalBottle();
		
		assertTrue(bottle + " cannot be transferred from " + settlement 
				+ " to " + person.getName() + ".", hasIt);
	}
	
}