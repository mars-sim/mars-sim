/**
 * Mars Simulation Project
 * Reporter.java
 * @version 3.1.0 2018-06-09
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.job;

import java.io.Serializable;
import java.util.Iterator;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.person.ai.task.ConnectWithEarth;
import org.mars_sim.msp.core.person.ai.task.ConsolidateContainers;
import org.mars_sim.msp.core.person.ai.task.HaveConversation;
import org.mars_sim.msp.core.person.ai.task.MeetTogether;
import org.mars_sim.msp.core.person.ai.task.RecordActivity;
import org.mars_sim.msp.core.structure.Settlement;

public class Reporter extends Job implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private final int JOB_ID = 14;
	
	private double[] roleProspects = new double[] {5.0, 5.0, 30.0, 30.0, 20.0, 5.0, 5.0};

	private static double TRADING_RANGE = 1500D;
	private static double SETTLEMENT_MULTIPLIER = 1D;

	/**
	 * Constructor.
	 */
	public Reporter() {
		// Use Job constructor.
		super(Reporter.class);

		// Add main tasks.
		jobTasks.add(MeetTogether.class);
		jobTasks.add(ConnectWithEarth.class);
		jobTasks.add(HaveConversation.class);
		jobTasks.add(RecordActivity.class);

		// Add side tasks
		jobTasks.add(ConsolidateContainers.class);

		// Add reporter-related missions.
		jobMissionStarts.add(Trade.class);
		jobMissionJoins.add(Trade.class);
		jobMissionStarts.add(TravelToSettlement.class);
		jobMissionJoins.add(TravelToSettlement.class);

		// Add missions
	}

	/**
	 * Gets a person's capability to perform this job.
	 * 
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {

		double result = 0D;
		
		int reportSkill = person.getSkillManager().getSkillLevel(SkillType.REPORTING);
		result = reportSkill;
		
		NaturalAttributeManager attributes = person.getNaturalAttributeManager();

//		if (attributes == null)
//			attributes = person.getNaturalAttributeManager();

		// Add experience aptitude.
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		result += result * ((experienceAptitude - 50D) / 100D);

		// Add leadership aptitude.
//		int leadershipAptitude = attributes.getAttribute(NaturalAttributeType.LEADERSHIP);
//		result += result * ((leadershipAptitude - 50D) / 100D);

		// Add conversation.
		int conversation = attributes.getAttribute(NaturalAttributeType.CONVERSATION);
		result += result * ((conversation - 50D) / 100D);

		// Add artistry aptitude.
		int artistry = attributes.getAttribute(NaturalAttributeType.ARTISTRY);
		result += result * ((artistry - 50D) / 100D);

		// Add attractiveness.
		int attractiveness = attributes.getAttribute(NaturalAttributeType.ATTRACTIVENESS);
		result += result * ((attractiveness - 50D) / 100D);

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
				if (distance <= TRADING_RANGE)
					result += SETTLEMENT_MULTIPLIER;
			}
		}

		return result * pop / 24D;
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