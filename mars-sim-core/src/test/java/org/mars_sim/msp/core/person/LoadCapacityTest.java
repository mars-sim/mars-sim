/*
 * Mars Simulation Project
 * LoadCapacityTest.java
 * @date 2021-09-29
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.environment.Environment;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.MockSettlement;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.MockBuilding;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

import junit.framework.TestCase;

/**
 * Tests the loading capacity of a person
 */
public class LoadCapacityTest
extends TestCase {

	// Extra amount to add to resource to handle double arithmetic mismatch
	private static final double EXTRA_RESOURCE = 0.01D;
	
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
        
        Environment mars = sim.getMars();
        Function.initializeInstances(simConfig.getBuildingConfiguration(), sim.getMasterClock().getMarsClock(),
        							 simConfig.getPersonConfig(), simConfig.getCropConfiguration(), mars.getSurfaceFeatures(),
        							 mars.getWeather(), sim.getUnitManager());
        
		// Create test settlement.
		settlement = new MockSettlement();	
		unitManager.addUnit(settlement);
		
		MockBuilding b1 = new MockBuilding(settlement.getBuildingManager(), "A1");
    
		b1.setWidth(10D);
		b1.setLength(10D);
		
		// Mock building already added. 
//		settlement.getBuildingManager().addMockBuilding(b1);
		
		person = Person.create(name, settlement)
				.setGender(GenderType.MALE)
				.setCountry(null)
				.setSponsor(null)
				.setSkill(null)
				.setPersonality(null, null)
				.setAttribute(null)
				.build();
				
		person.initialize();
		unitManager.addUnit(person);
		
		// Make the person strong to get loading quicker
		person.getNaturalAttributeManager().setAttribute(NaturalAttributeType.STRENGTH, 60);
		person.getNaturalAttributeManager().setAttribute(NaturalAttributeType.ENDURANCE, 60);
    }

	/*
	 * Test if a person can carry an EVA suit.
	 */
	public void testLoading() throws Exception {
//		Map<Integer, Number> requiredResourcesMap = new HashMap<>();
//		requiredResourcesMap.put(ResourceUtil.oxygenID, 50D);
//		requiredResourcesMap.put(ResourceUtil.methaneID, 10D);
		
		EVASuit suit = new EVASuit("EVA Suit 001", settlement);
//		double mass = suit.getBaseMass();
//		System.out.println("EVA suit's empty mass: " + mass);
		unitManager.addUnit(suit);
		suit.setContainerUnit(person);
		
//		assertTrue("Suit's Empty Mass", answer);
		
		Inventory inventory1 = person.getInventory();
        inventory1.addGeneralCapacity(100D);
        
		boolean answer = inventory1.canStoreUnit(suit, false);
		if (answer) 
			System.out.println("Can carry an EVA suit.");
		assertTrue("Can't carry an EVA suit", answer);
		
		inventory1.addAmountResourceTypeCapacity(ResourceUtil.oxygenID, 50);
		inventory1.storeAmountResource(ResourceUtil.oxygenID, 50, false);
		
		boolean answer1 = inventory1.canStoreUnit(suit, false);
		if (answer1) 
			System.out.println(name + " can carry 50 kg oxygen and an EVA suit.");
		
		assertTrue(name + " can't carry 50 kg oxygen and an EVA suit", answer1);
		
		
	}
}