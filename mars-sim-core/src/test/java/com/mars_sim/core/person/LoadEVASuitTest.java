/*
 * Mars Simulation Project
 * LoadEVASuitTest.java
 * @date 2023-06-30
 * @author Manny Kung
 */

package com.mars_sim.core.person;

import java.util.HashMap;
import java.util.Map;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.equipment.EVASuit;
import com.mars_sim.core.equipment.EVASuitUtil;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;

/**
 * Tests the ability of a person to load resources into an EVA suit.
 */
public class LoadEVASuitTest extends AbstractMarsSimUnitTest {

	/*
	 * Test if a person don an EVA suit and load it with resources.
	 */
	public void testLoadingEVA() throws Exception {
		Settlement settlement = buildSettlement();
		Person person = buildPerson("Loader", settlement);

		double capacity = Math.round(person.getCarryingCapacity()* 10D)/10D;
		
		assertTrue(person + " has no carrying capacity.", capacity > 0);
		
		Map<Integer, Number> requiredResourcesMap = new HashMap<>();
		requiredResourcesMap.put(ResourceUtil.OXYGEN_ID, 1D);
		requiredResourcesMap.put(ResourceUtil.WATER_ID, 4D);
		
		for (int i: requiredResourcesMap.keySet()) {
			double amount = (double) requiredResourcesMap.get(i);
			person.storeAmountResource(i, amount) ; 
		}
		
		EVASuit suitSettlement = (EVASuit)EquipmentFactory.createEquipment(EquipmentType.EVA_SUIT, settlement);
		
		settlement.addEquipment(suitSettlement);
		
		EquipmentOwner personOwner = (EquipmentOwner)person;
		
		EVASuit suitPerson = EVASuitUtil.findEVASuitWithResources(settlement, person);
		
		assertEquals("EVA suit name not matched.", suitSettlement.getName(), suitPerson.getName());
		
		double mass = Math.round(suitPerson.getBaseMass() * 100D)/100D;
		
		System.out.println(suitSettlement.getName() + " has empty mass of " + mass + " kg");
		
		assertEquals("EVA suit's empty mass is incorrect.", 16.18, mass);

		// 1. Transfer the EVA suit from settlement/vehicle to person
		suitSettlement.transfer(person);
		// 2. Set the person as the owner
		suitSettlement.setRegisteredOwner(person);
		// 3. Load resources 
		double percentageFull = suitSettlement.loadResources(personOwner);
				
		// 4. Loads the resources into the EVA suit
		assertTrue("Loading resources into EVA suit but NOT fully loaded.", 
				(percentageFull > 0.0));
		
		for (int i: requiredResourcesMap.keySet()) {
			double amount = (double) requiredResourcesMap.get(i);
			settlement.storeAmountResource(i, amount) ; 
		}
		
		EquipmentOwner settlementOwner = (EquipmentOwner)settlement;
		
		// 1. Transfer the EVA suit from to person to settlement
		suitPerson.transfer(settlement);
		// 2. Get the instance of the suit
		suitSettlement = EVASuitUtil.findEVASuitWithResources(settlement, person);
		assertNotNull("Selected Suit from Settlement", suitSettlement);		

		// 3. Load resources
		percentageFull = suitSettlement.loadResources(settlementOwner);
		
		
		// 4. Loads the resources into the EVA suit
		assertTrue("Loading resources into EVA suit but NOT fully loaded.", 
				(percentageFull > 0.0));
	}
}