/**
 * Mars Simulation Project
 * ResearchBotany.java
 * @version 2.76 2004-05-21
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import org.mars_sim.msp.simulation.Lab;
import org.mars_sim.msp.simulation.Mars;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.Research;

/** 
 * The ResearchBotany class is a task for researching botany science.
 */
public class ResearchBotany extends ResearchScience implements Serializable {

	/** 
	 * Constructor
	 * This is an effort driven task.
	 * @param person the person to perform the task
	 * @param mars the virtual Mars
	 */
	public ResearchBotany(Person person, Mars mars) {
		super(Skill.BOTANY, person, mars, false, null, 0D);
	}
	
	/** 
	 * Returns the weighted probability that a person might perform this task.
	 * @param person the person to perform the task
	 * @param mars the virtual Mars
	 * @return the weighted probability that a person might perform this task
	 */
	public static double getProbability(Person person, Mars mars) {
		double result = 0D;

		Lab lab = getLocalLab(person, Skill.BOTANY, false, null);
		if (lab != null) {
			result = 25D; 
		
			// Check for crowding modifier.
			if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
				try {
					Building labBuilding = ((Research) lab).getBuilding();	
					result *= Task.getCrowdingProbabilityModifier(person, labBuilding);				
				}
				catch (BuildingException e) {
					System.err.println("ResearchBotany.getProbability(): " + e.getMessage());
				}
			}
		}
	    
		// Effort-driven task modifier.
		result *= person.getPerformanceRating();

		return result;
	}
}