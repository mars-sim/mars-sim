/**
 * Mars Simulation Project
 * StudyRockSamples.java
 * @version 2.76 2004-06-08
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import org.mars_sim.msp.simulation.Lab;
import org.mars_sim.msp.simulation.Resource;
import org.mars_sim.msp.simulation.person.*;
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
     */
    public StudyRockSamples(Person person) {
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
					result *= Task.getCrowdingProbabilityModifier(person, labBuilding);				
				}
				catch (BuildingException e) {
					System.err.println("StudyRockSamples.getProbability(): " + e.getMessage());
				}
			}
		}
	    
		// Effort-driven task modifier.
		result *= person.getPerformanceRating();
		
		// Job modifier.
		result *= person.getMind().getJob().getStartTaskProbabilityModifier(StudyRockSamples.class);		

		return result;
    }
}