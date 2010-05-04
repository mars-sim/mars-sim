/**
 * Mars Simulation Project
 * Doctor.java
 * @version 2.86 2009-05-13
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.BuildingSalvageMission;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.person.ai.task.MedicalAssistance;
import org.mars_sim.msp.core.person.ai.task.PrescribeMedication;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.function.MedicalCare;
import org.mars_sim.msp.core.structure.building.function.Research;

/** 
 * The Doctor class represents a job for an medical treatment expert.
 */
public class Doctor extends Job implements Serializable {
    
    	private static String CLASS_NAME = "org.mars_sim.msp.simulation.person.ai.job.Doctor";
	
	private static Logger logger = Logger.getLogger(CLASS_NAME);

	/**
	 * Constructor
	 */
	public Doctor() {
		// Use Job constructor
		super("Doctor");
		
		// Add doctor-related tasks.
		jobTasks.add(MedicalAssistance.class);
        jobTasks.add(PrescribeMedication.class);
		
		// Add doctor-related missions.
        jobMissionStarts.add(TravelToSettlement.class);
		jobMissionJoins.add(TravelToSettlement.class);	
		jobMissionStarts.add(RescueSalvageVehicle.class);
		jobMissionJoins.add(RescueSalvageVehicle.class);
        jobMissionJoins.add(BuildingConstructionMission.class);
        jobMissionJoins.add(BuildingSalvageMission.class);
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
		
		// Add (labspace * tech level) / 2 for all labs with medical specialities.
		List<Building> laboratoryBuildings = settlement.getBuildingManager().getBuildings(Research.NAME);
		Iterator<Building> i = laboratoryBuildings.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			try {
				Research lab = (Research) building.getFunction(Research.NAME);
				if (lab.hasSpeciality(Skill.MEDICAL)) 
					result += ((double) (lab.getResearcherNum() * lab.getTechnologyLevel()) / 2D);
			}
			catch (BuildingException e) {
			    logger.log(Level.SEVERE,"Doctor.getSettlementNeed()", e);
			}
		}		
		
		// Add (tech level / 2) for all medical infirmaries.
		List<Building> medicalBuildings = settlement.getBuildingManager().getBuildings(MedicalCare.NAME);
		Iterator<Building> j = medicalBuildings.iterator();
		while (j.hasNext()) {
			Building building = j.next();
			try {
				MedicalCare infirmary = (MedicalCare) building.getFunction(MedicalCare.NAME);
				result+= (double) infirmary.getTechLevel() / 2D;
			}
			catch (BuildingException e) {
			    logger.log(Level.SEVERE,"Doctor.getSettlementNeed()", e);
			}
		}			
		
		return result;	
	}
}