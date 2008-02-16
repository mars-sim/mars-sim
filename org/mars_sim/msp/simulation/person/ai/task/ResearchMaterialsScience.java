/**
 * Mars Simulation Project
 * ResearchMaterialsScience.java
 * @version 2.83 2008-01-19
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.Lab;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.SkillManager;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingException;
import org.mars_sim.msp.simulation.structure.building.function.Research;

/** 
 * The ResearchMaterialsScience class is a task for researching materials science.
 */
public class ResearchMaterialsScience extends ResearchScience implements
		Serializable {
    
    private static String CLASS_NAME = "org.mars_sim.msp.simulation.person.ai.task.ResearchMaterialsScience";
	
    private static Logger logger = Logger.getLogger(CLASS_NAME);

	/** 
	 * Constructor
	 * This is an effort driven task.
	 * @param person the person to perform the task
	 * @throws Exception if error constructing task.
	 */
	public ResearchMaterialsScience(Person person) throws Exception {
		super(Skill.MATERIALS_SCIENCE, person, false, null, 0D);
	}
	
	/** 
	 * Returns the weighted probability that a person might perform this task.
	 * @param person the person to perform the task
	 * @return the weighted probability that a person might perform this task
	 */
	public static double getProbability(Person person) {
		double result = 0D;

		try {
			Lab lab = getLocalLab(person, Skill.MATERIALS_SCIENCE, false, null);
			if (lab != null) {
				result = 25D; 
		
				// Check for crowding modifier.
				if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
					try {
						Building labBuilding = ((Research) lab).getBuilding();	
						if (labBuilding != null) {
							result *= Task.getCrowdingProbabilityModifier(person, labBuilding);		
							result *= Task.getRelationshipModifier(person, labBuilding);
						}
						else result = 0D;		
					}
					catch (BuildingException e) {
						logger.log(Level.SEVERE,"ResearchMaterialsScience.getProbability(): " + e.getMessage());
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	    
		// Effort-driven task modifier.
		result *= person.getPerformanceRating();
		
		// Job modifier.
		Job job = person.getMind().getJob();
		if (job != null) result *= job.getStartTaskProbabilityModifier(ResearchMaterialsScience.class);		

		return result;
	}
	
	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getMind().getSkillManager();
		return manager.getEffectiveSkillLevel(Skill.MATERIALS_SCIENCE);
	}  
	
	/**
	 * Gets a list of the skills associated with this task.
	 * May be empty list if no associated skills.
	 * @return list of skills as strings
	 */
	public List<String> getAssociatedSkills() {
		List<String> results = new ArrayList<String>(1);
		results.add(Skill.MATERIALS_SCIENCE);
		return results;
	}
}