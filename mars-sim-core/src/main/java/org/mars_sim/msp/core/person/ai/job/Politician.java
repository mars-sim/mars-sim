/**
 * Mars Simulation Project
 * Politician.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.job;

import java.util.Iterator;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.person.ai.task.ReportMissionControl;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.Administration;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

public class Politician extends Job {
	
	private static double TRADING_RANGE = 1500D;
	private static double SETTLEMENT_MULTIPLIER = .3D;

	/**
	 * Constructor.
	 */
	public Politician() {
		// Use Job constructor.
		super(JobType.POLITICIAN, Job.buildRoleMap(15.0, 5.0, 25.0, 25.0, 20.0, 5.0, 5.0));
		
		// Add Manager-related missions.
		jobMissionStarts.add(Trade.class);
		jobMissionJoins.add(Trade.class);
		jobMissionStarts.add(TravelToSettlement.class);
		jobMissionJoins.add(TravelToSettlement.class);

		// Should mayor be heroic in this frontier world? Yes
//		jobMissionStarts.add(RescueSalvageVehicle.class);
//		jobMissionJoins.add(RescueSalvageVehicle.class);

	}

	/**
	 * Gets a person's capability to perform this job.
	 * 
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {

		double result = 0D;

		int managerSkill = person.getSkillManager().getSkillLevel(SkillType.MANAGEMENT);
		result = managerSkill;

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();

		// Add experience aptitude.
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		result += result * ((experienceAptitude - 50D) / 100D);

		// Add leadership aptitude.
		int leadershipAptitude = attributes.getAttribute(NaturalAttributeType.LEADERSHIP);
		result += result * ((leadershipAptitude - 50D) / 100D);

		// Add conversation.
		int conversation = attributes.getAttribute(NaturalAttributeType.CONVERSATION);
		result += result * ((conversation - 50D) / 100D);

		if (person.getPhysicalCondition().hasSeriousMedicalProblems())
			result = 0D;

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

		int population = settlement.getNumCitizens();
		
		Iterator<Settlement> i = unitManager.getSettlements().iterator();
		while (i.hasNext()) {
			Settlement otherSettlement = i.next();
			if (otherSettlement != settlement) {
				double distance = settlement.getCoordinates().getDistance(otherSettlement.getCoordinates());
				if (distance <= TRADING_RANGE)
					result += SETTLEMENT_MULTIPLIER / 12.0;
			}
		}

		Iterator<Building> j = settlement.getBuildingManager().getBuildings(FunctionType.ADMINISTRATION).iterator();
		while (j.hasNext()) {
			Building building = j.next();
			Administration admin = building.getAdministration();
			result += admin.getStaffCapacity()/24D;
		}
		
		result = (result + population / 64D) / 2.0;
		
//		System.out.println(settlement + " Politician need: " + result);
		
		return result;
	}
}
