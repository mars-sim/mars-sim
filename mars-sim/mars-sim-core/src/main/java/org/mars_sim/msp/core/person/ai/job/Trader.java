/**
 * Mars Simulation Project
 * Trader.java
 * @version 3.05 2013-08-19
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.job;

import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.mission.*;
import org.mars_sim.msp.core.person.ai.task.DigLocalIce;
import org.mars_sim.msp.core.structure.Settlement;

import java.io.Serializable;
import java.util.Iterator;

public class Trader extends Job implements Serializable {

    private static double TRADING_RANGE = 1500D;
    private static double SETTLEMENT_MULTIPLIER = 3D;
    
	/**
	 * Constructor
	 */
	public Trader() {
		// Use Job constructor.
		super("Trader");
		
		// Add trader-related tasks.
		jobTasks.add(DigLocalIce.class);
		
		// Add trader-related missions.
		jobMissionStarts.add(Trade.class);
		jobMissionJoins.add(Trade.class);
        jobMissionStarts.add(TravelToSettlement.class);
		jobMissionJoins.add(TravelToSettlement.class);
		jobMissionStarts.add(RescueSalvageVehicle.class);
		jobMissionJoins.add(RescueSalvageVehicle.class);
        jobMissionJoins.add(BuildingConstructionMission.class);
        jobMissionJoins.add(BuildingSalvageMission.class);
        jobMissionStarts.add(EmergencySupplyMission.class);
        jobMissionJoins.add(EmergencySupplyMission.class);
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