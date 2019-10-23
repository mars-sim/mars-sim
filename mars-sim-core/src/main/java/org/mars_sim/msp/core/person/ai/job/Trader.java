/**
 * Mars Simulation Project
 * Trader.java
 * @version 3.1.0 2018-08-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.io.Serializable;
import java.util.Iterator;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.person.ai.task.ConsolidateContainers;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleGarage;
import org.mars_sim.msp.core.structure.Settlement;

public class Trader extends Job implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private final int JOB_ID = 16;
	
	private double[] roleProspects = new double[] {5.0, 5.0, 30.0, 25.0, 25.0, 5.0, 5.0};

	private static double TRADING_RANGE = 500D;
	private static double SETTLEMENT_MULTIPLIER = 3D;

	/**
	 * Constructor.
	 */
	public Trader() {
		// Use Job constructor.
		super(Trader.class);

		// Add trader-related tasks.
		jobTasks.add(ConsolidateContainers.class);
		jobTasks.add(LoadVehicleEVA.class);
		jobTasks.add(LoadVehicleGarage.class);
		jobTasks.add(UnloadVehicleEVA.class);
		jobTasks.add(UnloadVehicleGarage.class);

		// Add side tasks
		// None

		// Add trader-related missions.
		jobMissionStarts.add(Trade.class);
		jobMissionJoins.add(Trade.class);

//		jobMissionJoins.add(BuildingConstructionMission.class);
//		jobMissionJoins.add(BuildingSalvageMission.class);

	}

	/**
	 * Gets a person's capability to perform this job.
	 * 
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {

		double result = 0D;

		int tradingSkill = person.getSkillManager().getSkillLevel(SkillType.TRADING);
		result = tradingSkill;

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();

		// Add experience aptitude.
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
//		result += result * ((experienceAptitude - 50D) / 100D);
		// Add conversation.
		int conversation = attributes.getAttribute(NaturalAttributeType.CONVERSATION);
//		result += result * ((conversation - 50D) / 100D);

		double averageAptitude = experienceAptitude + conversation;
		result += result * ((averageAptitude - 100D) / 100D);
		
		return result;
	}

	/**
	 * Gets the base settlement need for this job.
	 * 
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	public double getSettlementNeed(Settlement settlement) {

		double result = .1;
		
		int pop = settlement.getNumCitizens();
				
		Iterator<Settlement> i = unitManager.getSettlements().iterator();
		while (i.hasNext()) {
			Settlement otherSettlement = i.next();
			if (otherSettlement != settlement) {
				double distance = settlement.getCoordinates().getDistance(otherSettlement.getCoordinates());
				result += TRADING_RANGE / distance * SETTLEMENT_MULTIPLIER *.5;
//				if (distance <= TRADING_RANGE) {
//					result += SETTLEMENT_MULTIPLIER;
//				}
			}
		}

		return result * pop / 18D;
	}

	public double[] getRoleProspects() {
		return roleProspects;
	}
	
	public void setRoleProspects(int index, int weight) {
		roleProspects[index] = weight;
	}
	
	public int getJobID() {
		return JOB_ID;
	}
}