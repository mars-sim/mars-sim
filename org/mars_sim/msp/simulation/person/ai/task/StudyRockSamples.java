/**
 * Mars Simulation Project
 * StudyRockSamples.java
 * @version 2.78 2005-08-14
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.Lab;
import org.mars_sim.msp.simulation.Resource;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.SkillManager;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;

/** 
 * The StudyRockSamples class is a task for scientific research on 
 * collected rock samples. 
 */
public class StudyRockSamples extends ResearchScience implements Serializable {

    // Rate of rock sample research (kg / millisol)
    private static final double RESEARCH_RATE = .01D;

    /** 
     * Constructor 
     * This is an effort driven task.
     * @param person the person to perform the task
     * @throws Exception if error constructing task.
     */
    public StudyRockSamples(Person person) throws Exception {
		super(Skill.AREOLOGY, person, true, Resource.ROCK_SAMPLES, RESEARCH_RATE);
    }

    /** 
     * Returns the weighted probability that a person might perform this task.
     * @param person the person to perform the task
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
		double result = 0D;

		Lab lab = getLocalLab(person, Skill.AREOLOGY, true, Resource.ROCK_SAMPLES);
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
					System.err.println("StudyRockSamples.getProbability(): " + e.getMessage());
				}
			}
		}
	    
		// Effort-driven task modifier.
		result *= person.getPerformanceRating();
		
		// Job modifier.
		Job job = person.getMind().getJob();
		if (job != null) result *= job.getStartTaskProbabilityModifier(StudyRockSamples.class);		

		return result;
    }
    
	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getMind().getSkillManager();
		return manager.getEffectiveSkillLevel(Skill.AREOLOGY);
	}  
	
	/**
	 * Gets a list of the skills associated with this task.
	 * May be empty list if no associated skills.
	 * @return list of skills as strings
	 */
	public List getAssociatedSkills() {
		List results = new ArrayList();
		results.add(Skill.AREOLOGY);
		return results;
	}
}