/**
 * Mars Simulation Project
 * Botanist.java
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
 * The Botanist class represents a job for a botanist.
 */
public class Botanist extends Job implements Serializable {

	/**
	 * Constructor
	 */
	public Botanist() {
		// Use Job constructor
		super("Botanist");
		
		// Add botany-related tasks.
		jobTasks.add(ResearchBotany.class);
		jobTasks.add(TendGreenhouse.class);
		
		// Add botanist-related missions.
		// jobMissionStarts.add(TravelToSettlement.class);
		jobMissionJoins.add(TravelToSettlement.class);	
		jobMissionStarts.add(RescueSalvageVehicle.class);
		jobMissionJoins.add(RescueSalvageVehicle.class);
	}

	/**
	 * Gets a person's capability to perform this job.
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {
		
		double result = 0D;
		
		int botanySkill = person.getMind().getSkillManager().getSkillLevel(Skill.BOTANY);
		result = botanySkill;
		
		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute(NaturalAttributeManager.ACADEMIC_APTITUDE);
		int experienceAptitude = attributes.getAttribute(NaturalAttributeManager.EXPERIENCE_APTITUDE);
		double averageAptitude = (academicAptitude + experienceAptitude) / 2D;
		result+= result * ((averageAptitude - 50D) / 100D);
		
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
		
		// Add (labspace * tech level) / 2 for all labs with botany specialities.
		List laboratoryBuildings = settlement.getBuildingManager().getBuildings(Research.NAME);
		Iterator i = laboratoryBuildings.iterator();
		while (i.hasNext()) {
			Building building = (Building) i.next();
			try {
				Research lab = (Research) building.getFunction(Research.NAME);
				if (lab.hasSpeciality(Skill.BOTANY)) 
					result += (double) (lab.getResearcherNum() * lab.getTechnologyLevel()) / 2D;
			}
			catch (BuildingException e) {
				System.err.println("Botanist.getSettlementNeed(): " + e.getMessage());
			}
		}
		
		// Add (growing area in greenhouses) / 10
		List greenhouseBuildings = settlement.getBuildingManager().getBuildings(Farming.NAME);
		Iterator j = greenhouseBuildings.iterator();
		while (j.hasNext()) {
			Building building = (Building) j.next();
			try {
				Farming farm = (Farming) building.getFunction(Farming.NAME);
				result += (farm.getGrowingArea() / 10D);
			}
			catch (BuildingException e) {
				System.err.println("Botanist.getSetltementNeed(): " + e.getMessage());
			}
		}
		
		return result;	
	}	
}