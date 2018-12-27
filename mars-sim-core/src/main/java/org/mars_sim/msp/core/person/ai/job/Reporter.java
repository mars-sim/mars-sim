/**
 * Mars Simulation Project
 * Reporter.java
 * @version 3.1.0 2018-06-09
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.job;

import java.io.Serializable;
import java.util.Iterator;

import org.mars_sim.msp.core.person.NaturalAttributeType;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
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

	private static double TRADING_RANGE = 1500D;
	private static double SETTLEMENT_MULTIPLIER = 3D;

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

		// Add Manager-related missions.
		jobMissionStarts.add(Trade.class);
		jobMissionJoins.add(Trade.class);
		jobMissionStarts.add(TravelToSettlement.class);
		jobMissionJoins.add(TravelToSettlement.class);

		// Should mayor be heroic in this frontier world? Yes
		jobMissionStarts.add(RescueSalvageVehicle.class);
		jobMissionJoins.add(RescueSalvageVehicle.class);

	}

	/**
	 * Gets a person's capability to perform this job.
	 * 
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {

		double result = 0D;

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();

//		if (attributes == null)
//			attributes = person.getNaturalAttributeManager();

		// Add experience aptitude.
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		result += result * ((experienceAptitude - 50D) / 100D);

		// Add leadership aptitude.
		int leadershipAptitude = attributes.getAttribute(NaturalAttributeType.LEADERSHIP);
		result += result * ((leadershipAptitude - 50D) / 100D) / 2D;

		// Add conversation.
		int conversation = attributes.getAttribute(NaturalAttributeType.CONVERSATION);
		result += 2D * result * ((conversation - 50D) / 100D);

		// Add artistry aptitude.
		int artistry = attributes.getAttribute(NaturalAttributeType.ARTISTRY);
		result += result * ((artistry - 50D) / 100D);

		// Add attractiveness.
		int attractiveness = attributes.getAttribute(NaturalAttributeType.ATTRACTIVENESS);
		result += 2D * result * ((attractiveness - 50D) / 100D);

		return result;
	}

	/**
	 * Gets the base settlement need for this job.
	 * 
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	public double getSettlementNeed(Settlement settlement) {

		double result = 0D;

		int pop = settlement.getNumCitizens();
		
		Iterator<Settlement> i = settlement.getUnitManager().getSettlements().iterator();
		while (i.hasNext()) {
			Settlement otherSettlement = i.next();
			if (otherSettlement != settlement) {
				double distance = settlement.getCoordinates().getDistance(otherSettlement.getCoordinates());
				if (distance <= TRADING_RANGE)
					result += SETTLEMENT_MULTIPLIER;
			}
		}

		result += pop/24;
		return result;
	}

}