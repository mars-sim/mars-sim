/*
 * Mars Simulation Project
 * Doctor.java
 * @date 2021-09-27
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.MedicalCare;
import org.mars_sim.msp.core.structure.building.function.Research;

/**
 * The Doctor class represents a job for an medical treatment expert.
 */
class Doctor extends Job {
		
	/** Constructor. */
	public Doctor() {
		// Use Job constructor
		super(JobType.DOCTOR, Job.buildRoleMap(20.0, 10.0, 5.0, 5.0, 5.0, 20.0, 15.0, 30.0));

		// Add doctor-related missions.
//		jobMissionJoins.add(BuildingConstructionMission.class);
//		jobMissionJoins.add(BuildingSalvageMission.class);
	}

	/**
	 * Gets a person's capability to perform this job.
	 * 
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {

		double result = 0D;

		int skill = person.getSkillManager().getSkillLevel(SkillType.MEDICINE);
		result = skill;

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		double averageAptitude = (academicAptitude + experienceAptitude) / 2D;
		result += result * ((averageAptitude - 100D) / 100D);

		if (person.getPhysicalCondition().hasSeriousMedicalProblems())
			result = 0D;

//		System.out.println(person + " doctor : " + Math.round(result*100.0)/100.0);

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

		int population = settlement.getNumCitizens();

		// Add (labspace * tech level) / 2 for all labs with medical specialties.
		List<Building> laboratoryBuildings = settlement.getBuildingManager().getBuildings(FunctionType.RESEARCH);
		Iterator<Building> i = laboratoryBuildings.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Research lab = building.getResearch();
			if (lab.hasSpecialty(ScienceType.MEDICINE)) {
				result += ((double) (lab.getResearcherNum() * lab.getTechnologyLevel()) / 3D);
			}
		}

		// Add (tech level / 2) for all medical infirmaries.
		List<Building> medicalBuildings = settlement.getBuildingManager().getBuildings(FunctionType.MEDICAL_CARE);
		Iterator<Building> j = medicalBuildings.iterator();
		while (j.hasNext()) {
			Building building = j.next();
			MedicalCare infirmary = building.getMedical();
			result += (double) infirmary.getTechLevel() / 3D;
		}

		result = (result + population / 8D) / 2.0;
		
//		System.out.println(settlement + " Doctor need: " + result);
		
		return result;
	}
}
