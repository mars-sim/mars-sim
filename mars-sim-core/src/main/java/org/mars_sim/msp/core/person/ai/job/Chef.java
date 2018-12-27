/**
 * Mars Simulation Project
 * Chef.java
 * @version 3.07 2014-11-23
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.person.NaturalAttributeType;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.BuildingSalvageMission;
import org.mars_sim.msp.core.person.ai.mission.EmergencySupplyMission;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.person.ai.task.ConsolidateContainers;
import org.mars_sim.msp.core.person.ai.task.CookMeal;
import org.mars_sim.msp.core.person.ai.task.PrepareDessert;
import org.mars_sim.msp.core.person.ai.task.ProduceFood;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;

/**
 * The Chef class represents a job for a chef.
 */
public class Chef
extends Job
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	//	private static Logger logger = Logger.getLogger(Chef.class.getName());

	/** constructor. */
	public Chef() {
		// Use Job constructor
		super(Chef.class);

		// Add chef-related tasks.
		jobTasks.add(CookMeal.class);
		jobTasks.add(PrepareDessert.class);
		jobTasks.add(ProduceFood.class);

		// Add side tasks
		jobTasks.add(ConsolidateContainers.class);

		// Add chef-related missions.
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

		int cookingSkill = person.getMind().getSkillManager().getSkillLevel(SkillType.COOKING);
		result = cookingSkill;

		//int foodProcessingSkill = person.getMind().getSkillManager().getSkillLevel(SkillType.FOODPROCESSING);
		//result = foodProcessingSkill;

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		result+= result * ((experienceAptitude - 50D) / 100D);

		if (person.getPhysicalCondition().hasSeriousMedicalProblems()) result = 0D;

		return result;
	}

	/**
	 * Gets the base settlement need for this job.
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	public double getSettlementNeed(Settlement settlement) {
		double result = 0D;

		// Add all kitchen work space in settlement.
		List<Building> kitchenBuildings = settlement.getBuildingManager().getBuildings(FunctionType.COOKING);
		Iterator<Building> i = kitchenBuildings.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Cooking kitchen = building.getCooking();
			result += (double) kitchen.getCookCapacity();
		}

		// Add total population / 10.
		int population = settlement.getIndoorPeopleCount();
		result+= ((double) population / 10D);

		return result;
	}

}