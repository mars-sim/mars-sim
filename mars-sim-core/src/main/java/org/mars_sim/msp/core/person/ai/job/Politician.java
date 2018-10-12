/**
 * Mars Simulation Project
 * Politician.java
 * @version 3.1.0 2018-06-09
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.job;

import java.io.Serializable;
import java.util.Iterator;

import org.mars_sim.msp.core.person.NaturalAttributeType;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.person.ai.task.ConnectWithEarth;
import org.mars_sim.msp.core.person.ai.task.ConsolidateContainers;
import org.mars_sim.msp.core.person.ai.task.DigLocalIce;
import org.mars_sim.msp.core.person.ai.task.DigLocalRegolith;
import org.mars_sim.msp.core.person.ai.task.HaveConversation;
import org.mars_sim.msp.core.person.ai.task.MeetTogether;
import org.mars_sim.msp.core.person.ai.task.ReviewJobReassignment;
import org.mars_sim.msp.core.person.ai.task.WriteReport;
import org.mars_sim.msp.core.structure.Settlement;

public class Politician
extends Job
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static double TRADING_RANGE = 1500D;
	private static double SETTLEMENT_MULTIPLIER = 3D;

	/**
	 * Constructor.
	 */
	public Politician() {
		// Use Job constructor.
		super(Politician.class);

		// Add Manager-related tasks.
		jobTasks.add(MeetTogether.class);
		jobTasks.add(ConnectWithEarth.class);
		jobTasks.add(HaveConversation.class);

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
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {

		double result = 0D;

		int managerSkill = person.getMind().getSkillManager().getSkillLevel(SkillType.MANAGEMENT);
		result = managerSkill;

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();

		// Add experience aptitude.
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		result+= result * ((experienceAptitude - 50D) / 100D);

		// Add leadership aptitude.
		int leadershipAptitude = attributes.getAttribute(NaturalAttributeType.LEADERSHIP);
		result+= result * ((leadershipAptitude - 50D) / 100D);

		// Add conversation.
		int conversation = attributes.getAttribute(NaturalAttributeType.CONVERSATION);
		result+= result * ((conversation - 50D) / 100D);

		if (person.getPhysicalCondition().hasSeriousMedicalProblems()) 
			result = 0D;
		
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