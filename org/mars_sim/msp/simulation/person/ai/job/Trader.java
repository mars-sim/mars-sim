/**
 * Mars Simulation Project
 * Driver.java
 * @version 2.81 2007-06-12
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.job;

import java.io.Serializable;

import org.mars_sim.msp.simulation.person.NaturalAttributeManager;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.mission.CollectIce;
import org.mars_sim.msp.simulation.person.ai.mission.Exploration;
import org.mars_sim.msp.simulation.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.simulation.person.ai.mission.Trade;
import org.mars_sim.msp.simulation.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.simulation.structure.Settlement;

public class Trader extends Job implements Serializable {

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
		jobMissionJoins.add(Exploration.class);
		jobMissionJoins.add(CollectIce.class);
		jobMissionJoins.add(TravelToSettlement.class);
		jobMissionStarts.add(RescueSalvageVehicle.class);
		jobMissionJoins.add(RescueSalvageVehicle.class);
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
		
		// Gets total associated population with settlement.
		return settlement.getAllAssociatedPeople().size() / 2D;
	}
}