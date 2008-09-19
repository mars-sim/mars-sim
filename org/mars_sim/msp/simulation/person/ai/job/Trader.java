/**
 * Mars Simulation Project
 * Driver.java
 * @version 2.85 2008-09-18
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.job;

import java.io.Serializable;
import java.util.Iterator;

import org.mars_sim.msp.simulation.person.NaturalAttributeManager;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.simulation.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.simulation.person.ai.mission.Trade;
import org.mars_sim.msp.simulation.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.simulation.structure.Settlement;

public class Trader extends Job implements Serializable {

    private static double TRADING_RANGE = 5000D;
    private static double SETTLEMENT_MULTIPLIER = 1D;
    
	/**
	 * Constructor
	 */
	public Trader() {
		// Use Job constructor.
		super("Trader");
		
		// Add trader-related tasks.
		// Note: no trader tasks.
		
		// Add trader-related missions.
		jobMissionStarts.add(Trade.class);
		jobMissionJoins.add(Trade.class);
		jobMissionJoins.add(TravelToSettlement.class);
		jobMissionStarts.add(RescueSalvageVehicle.class);
		jobMissionJoins.add(RescueSalvageVehicle.class);
        jobMissionJoins.add(BuildingConstructionMission.class);
	}
	
	/**
	 * Gets a person's capability to perform this job.
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {
		
		double result = 0D;
		
		int tradingSkill = person.getMind().getSkillManager().getSkillLevel(Skill.TRADING);
		result = tradingSkill;
		
		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		
		// Add experience aptitude.
		int experienceAptitude = attributes.getAttribute(NaturalAttributeManager.EXPERIENCE_APTITUDE);
		result+= result * ((experienceAptitude - 50D) / 100D);
		
		// Add conversation.
		int conversation = attributes.getAttribute(NaturalAttributeManager.CONVERSATION);
		result+= result * ((conversation - 50D) / 100D);
		
		return result;
	}

	/**
	 * Gets the base settlement need for this job.
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	public double getSettlementNeed(Settlement settlement) {
		
        double result = 0D;
        
        Iterator<Settlement> i = settlement.getUnitManager().getSettlements().iterator();
        while (i.hasNext()) {
            Settlement otherSettlement = i.next();
            if (otherSettlement != settlement) {
                double distance = settlement.getCoordinates().getDistance(otherSettlement.getCoordinates());
                if (distance <= TRADING_RANGE) result += SETTLEMENT_MULTIPLIER; 
            }
        }
        
		return result;
	}
}