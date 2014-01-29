/**
 * Mars Simulation Project
 * Chef.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.BuildingSalvageMission;
import org.mars_sim.msp.core.person.ai.mission.EmergencySupplyMission;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.person.ai.task.CookMeal;
import org.mars_sim.msp.core.person.ai.task.DigLocalIce;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.Cooking;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/** 
 * The Chef class represents a job for a chef.
 */
public class Chef extends Job implements Serializable {
	
	private static Logger logger = Logger.getLogger(Chef.class.getName());

	public Chef() {
		// Use Job constructor
		super("Chef");
		
		// Add chef-related tasks.
		jobTasks.add(CookMeal.class);
		jobTasks.add(DigLocalIce.class);
		
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
		
		int cookingSkill = person.getMind().getSkillManager().getSkillLevel(Skill.COOKING);
		result = cookingSkill;
		
		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int experienceAptitude = attributes.getAttribute(NaturalAttributeManager.EXPERIENCE_APTITUDE);
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
		List<Building> kitchenBuildings = settlement.getBuildingManager().getBuildings(Cooking.NAME);
		Iterator<Building> i = kitchenBuildings.iterator();
		while (i.hasNext()) {
		    Building building = i.next();
		    Cooking kitchen = (Cooking) building.getFunction(Cooking.NAME); 
		    result += (double) kitchen.getCookCapacity();
		}
		
		// Add total population / 10.
		int population = settlement.getCurrentPopulationNum();
		result+= ((double) population / 10D);
		
		return result;			
	}
}