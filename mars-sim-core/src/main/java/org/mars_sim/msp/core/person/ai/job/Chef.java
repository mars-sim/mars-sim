/**
 * Mars Simulation Project
 * Chef.java
 * @version 3.1.0 2018-08-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.BuildingSalvageMission;
import org.mars_sim.msp.core.person.ai.mission.EmergencySupply;
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
public class Chef extends Job implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// private static Logger logger = Logger.getLogger(Chef.class.getName());

	private final int JOB_ID = 5;
	
	private double[] roleProspects = new double[] {35.0, 5.0, 5.0, 5.0, 20.0, 15.0, 15.0};
	
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
		result += ((double) population / 16D);

		return result;
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