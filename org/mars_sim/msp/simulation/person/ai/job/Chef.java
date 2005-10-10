/**
 * Mars Simulation Project
 * Chef.java
 * @version 2.78 2005-08-22
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.job;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.mission.*;
import org.mars_sim.msp.simulation.person.ai.task.*;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;

/** 
 * The Chef class represents a job for a chef.
 */
public class Chef extends Job implements Serializable {

	public Chef() {
		// Use Job constructor
		super("Chef");
		
		// Add chef-related tasks.
		jobTasks.add(CookMeal.class);
		
		// Add chef-related missions.
		jobMissionStarts.add(TravelToSettlement.class);
		jobMissionJoins.add(TravelToSettlement.class);	
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
		List kitchenBuildings = settlement.getBuildingManager().getBuildings(Cooking.NAME);
		Iterator i = kitchenBuildings.iterator();
		while (i.hasNext()) {
			Building building = (Building) i.next();
			try {
				Cooking kitchen = (Cooking) building.getFunction(Cooking.NAME); 
				result += (double) kitchen.getCookCapacity();
			}
			catch (BuildingException e) {
				System.err.println("Chef.getSettlementNeed(): " + e.getMessage());
			}
		}
		
		// Add total population / 2.
		int population = settlement.getCurrentPopulationNum();
		result+= ((double) population / 2D);
		
		return result;			
	}
}