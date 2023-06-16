/*
 * Mars Simulation Project
 * LoadEVASuitTest.java
 * @date 2023-06-10
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.MockSettlement;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.MockBuilding;
import org.mars_sim.msp.core.structure.building.function.Function;

import junit.framework.TestCase;

/**
 * Tests the ability of a person to carry resources.
 */
public class LoadPersonTest
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
	 * Test if a person can carry resources.
	 */
	public void testCarrying() throws Exception {
		double capacity = Math.round(person.getCarryingCapacity()* 10D)/10D;
		System.out.println(person + "'s carrying capacity: " + capacity);
		
		assertTrue(person + " has no carrying capacity.", capacity > 0);
		
		double amount = 1D;
		
		double excess = person.storeAmountResource(ResourceUtil.foodID, amount);	
		assertTrue("Can't carry food.", excess == 0.0);
		
		double missing = person.retrieveAmountResource(ResourceUtil.foodID, amount);
		assertTrue("Can't retrieve food.", missing == 0.0);
	}
}