/*
 * Mars Simulation Project
 * LoadEVASuitTest.java
 * @date 2022-09-01
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
	public void testLoading() throws Exception {
		Map<Integer, Number> requiredResourcesMap = new HashMap<>();
		requiredResourcesMap.put(ResourceUtil.oxygenID, 1D);
		requiredResourcesMap.put(ResourceUtil.waterID, 4D);
		
		EVASuit suit0 = new EVASuit("EVA Suit 001", settlement);
		settlement.addEquipment(suit0);
		
		EquipmentOwner housing = (EquipmentOwner)settlement;
		EVASuit suit = InventoryUtil.getGoodEVASuitNResource(settlement, person);
		assertTrue("Wrong EVA suit name.", suit.getName().equals(suit0.getName()));
		double mass = suit.getBaseMass();
		assertTrue("EVA suit's empty mass is NOT correct.", mass == 14.05);

		// 1. Transfer the EVA suit from settlement/vehicle to person
		suit.transfer(person);
		// 2. Set the person as the owner
		suit.setLastOwner(person);
		// 3. Register the suit the person will take into the airlock to don
		person.registerSuit(suit);
		// 4. Loads the resources into the EVA suit
		assertTrue("Loading resources into EVA suit but NOT fully loaded.", 
				(suit.loadResources(housing) < 0.9D));
	}
}