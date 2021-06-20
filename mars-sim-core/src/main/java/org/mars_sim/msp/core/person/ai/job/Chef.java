/**
 * Mars Simulation Project
 * Chef.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.BuildingSalvageMission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;

/**
 * The Chef class represents a job for a chef.
 */
class Chef extends Job {
		
	/** constructor. */
	public Chef() {
		// Use Job constructor
		super(JobType.CHEF, Job.buildRoleMap(35.0, 5.0, 5.0, 5.0, 20.0, 15.0, 15.0));

		// Add chef-related missions.
		jobMissionJoins.add(BuildingConstructionMission.class);
		jobMissionJoins.add(BuildingSalvageMission.class);
	}

	/**
	 * Gets a person's capability to perform this job.
	 * 
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {

		double result = 0D;

		int cookingSkill = person.getSkillManager().getSkillLevel(SkillType.COOKING);
		result = cookingSkill;

		// int foodProcessingSkill =
		// person.getMind().getSkillManager().getSkillLevel(SkillType.FOODPROCESSING);
		// result = foodProcessingSkill;

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		result += result * ((experienceAptitude - 50D) / 100D);

		if (person.getPhysicalCondition().hasSeriousMedicalProblems())
			result = 0D;

//		System.out.println(person + " chef : " + Math.round(result*100.0)/100.0);
		
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

		// Add all kitchen work space in settlement.
		List<Building> kitchenBuildings = settlement.getBuildingManager().getBuildings(FunctionType.COOKING);
		Iterator<Building> i = kitchenBuildings.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Cooking kitchen = building.getCooking();
			result += (double) kitchen.getCookCapacity()/12D;
		}

		// Add total population / 10.
		int population = settlement.getIndoorPeopleCount();

		result = (result + population / 12D) / 2.0;
		
//		System.out.println(settlement + " Chef Need: " + result);

		return result;
	}
}
