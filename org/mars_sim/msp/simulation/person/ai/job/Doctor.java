/**
 * Mars Simulation Project
 * Doctor.java
 * @version 2.78 2005-08-22
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.job;

import java.util.*;
import java.io.Serializable;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.mission.*;
import org.mars_sim.msp.simulation.person.ai.task.*;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;

/** 
 * The Doctor class represents a job for an medical treatment expert.
 */
public class Doctor extends Job implements Serializable {

	/**
	 * Constructor
	 */
	public Doctor() {
		// Use Job constructor
		super("Doctor");
		
		// Add doctor-related tasks.
		jobTasks.add(MedicalAssistance.class);
		jobTasks.add(ResearchMedicine.class);
		
		// Add doctor-related missions.
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
		
		int areologySkill = person.getMind().getSkillManager().getSkillLevel(Skill.MEDICAL);
		result = areologySkill;
		
		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute(NaturalAttributeManager.ACADEMIC_APTITUDE);
		result+= result * ((academicAptitude - 50D) / 100D);
		
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
		
		// Add total population / 2
		int population = settlement.getAllAssociatedPeople().size();
		result+= population / 2D;
		
		// Add (labspace * tech level) / 2 for all labs with areology specialities.
		List laboratoryBuildings = settlement.getBuildingManager().getBuildings(Research.NAME);
		Iterator i = laboratoryBuildings.iterator();
		while (i.hasNext()) {
			Building building = (Building) i.next();
			try {
				Research lab = (Research) building.getFunction(Research.NAME);
				if (lab.hasSpeciality(Skill.MEDICAL)) 
					result += ((double) (lab.getResearcherNum() * lab.getTechnologyLevel()) / 2D);
			}
			catch (BuildingException e) {
				System.err.println("Doctor.getSettlementNeed(): " + e.getMessage());
			}
		}		
		
		// Add (tech level / 2) for all medical infirmaries.
		List medicalBuildings = settlement.getBuildingManager().getBuildings(MedicalCare.NAME);
		Iterator j = medicalBuildings.iterator();
		while (j.hasNext()) {
			Building building = (Building) j.next();
			try {
				MedicalCare infirmary = (MedicalCare) building.getFunction(MedicalCare.NAME);
				result+= (double) infirmary.getTechLevel() / 2D;
			}
			catch (BuildingException e) {
				System.err.println("Doctor.getSettlementNeed(): " + e.getMessage());
			}
		}			
		
		return result;	
	}
}