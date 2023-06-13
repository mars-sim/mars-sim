/*
 * Mars Simulation Project
 * LoadEVASuitTest.java
 * @date 2023-06-10
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import java.util.HashMap;
import java.util.Map;

import org.mars_sim.msp.core.InventoryUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.EquipmentOwner;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.MockSettlement;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.MockBuilding;
import org.mars_sim.msp.core.structure.building.function.Function;

import junit.framework.TestCase;

/**
 * Tests the ability of a person to load resources into an EVA suit.
 */
public class LoadEVASuitTest
extends TestCase {

	private Settlement settlement = null;
	private UnitManager unitManager;
	private Person person;
	private String name = "Jim Loader";
	
	@Override
    public void setUp() throws Exception {
		// Create new simulation instance.
        SimulationConfig simConfig = SimulationConfig.instance();
        simConfig.loadConfig();
        
        Simulation sim = Simulation.instance();
        sim.testRun();

        unitManager = sim.getUnitManager();

        Function.initializeInstances(simConfig.getBuildingConfiguration(), sim.getMasterClock().getMarsClock(),
        							 simConfig.getPersonConfig(), simConfig.getCropConfiguration(), sim.getSurfaceFeatures(),
        							 sim.getWeather(), sim.getUnitManager());
        
		// Create test settlement.
		settlement = new MockSettlement();	
		unitManager.addUnit(settlement);
		
		MockBuilding b1 = new MockBuilding(settlement.getBuildingManager(), "A1");
    
		b1.setWidth(10D);
		b1.setLength(10D);
			
		person = Person.create(name, settlement)
				.setGender(GenderType.MALE)
				.setCountry(null)
				.setSponsor(null)
				.setSkill(null)
				.setPersonality(null, null)
				.setAttribute(null)
				.build();
				
		person.initializeForMaven();
		unitManager.addUnit(person);
		
		// Make the person strong to get loading quicker
		person.getNaturalAttributeManager().setAttribute(NaturalAttributeType.STRENGTH, 60);
		person.getNaturalAttributeManager().setAttribute(NaturalAttributeType.ENDURANCE, 60);
    }

	
	/*
	 * Test if a person don an EVA suit and load it with resources.
	 */
	public void testLoadingEVA() throws Exception {
		double capacity = Math.round(person.getCarryingCapacity()* 10D)/10D;
		System.out.println(person + "'s carrying capacity: " + capacity);
		
		assertTrue(person + " has no carrying capacity.", capacity > 0);
		
		Map<Integer, Number> requiredResourcesMap = new HashMap<>();
		requiredResourcesMap.put(ResourceUtil.oxygenID, 1D);
		requiredResourcesMap.put(ResourceUtil.waterID, 4D);
		
		for (int i: requiredResourcesMap.keySet()) {
			double amount = (double) requiredResourcesMap.get(i);
			person.storeAmountResource(i, amount) ; 
		}
		
		EVASuit suitSettlement = new EVASuit("EVA Suit 001", settlement);
		
		settlement.addEquipment(suitSettlement);
		
		EquipmentOwner personOwner = (EquipmentOwner)person;
		
		EVASuit suitPerson = InventoryUtil.getGoodEVASuitNResource(settlement, person);
		
		assertEquals("EVA suit name not matched.", suitSettlement.getName(), suitPerson.getName());
		
		double mass = Math.round(suitPerson.getBaseMass() * 100D)/100D;
		
		System.out.println(suitSettlement.getName() + " has empty mass: " + mass + " kg");
		
		assertEquals("EVA suit's empty mass is incorrect.", 13.6, mass);

		// 1. Transfer the EVA suit from settlement/vehicle to person
		suitSettlement.transfer(person);
		// 2. Set the person as the owner
		suitSettlement.setLastOwner(person);
		// 3. Register the suit the person will take into the airlock to don
		person.registerSuit(suitSettlement);
		
		double percentageFull = suitSettlement.loadResources(personOwner);
		
		System.out.println(person.getSuit().getName() + "'s percent of lowest resource: " + Math.round(percentageFull* 100D)/100D + " %");
		
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
		// 2b. Doff this suit. Deregister the suit from the person
		person.registerSuit(null);
		
		suitSettlement = InventoryUtil.getGoodEVASuitNResource(settlement, person);
		
		if (suitSettlement == null) {	
			String suitName = suitPerson.getName();
			System.out.println(suitName + " can't be transferred.");
		}
	
		percentageFull = suitSettlement.loadResources(settlementOwner);
		
		System.out.println(suitSettlement.getName() + "'s percent of lowest resource: " + Math.round(percentageFull* 100D)/100D + " %");
		
		// 4. Loads the resources into the EVA suit
		assertTrue("Loading resources into EVA suit but NOT fully loaded.", 
				(percentageFull > 0.0));

				
	}
}