/*
 * Mars Simulation Project
 * Doctor.java
 * @date 2021-09-27
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.util.Iterator;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.util.Job;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

/**
 * The Doctor class represents a job for an medical treatment expert.
 */
public class Doctor extends Job {
		
	/** Constructor. */
	public Doctor() {
		// Use Job constructor
		super(JobType.DOCTOR, Job.buildRoleMap(20.0, 10.0, 5.0, 5.0, 5.0, 20.0, 15.0, 20.0));
	}

	/**
	 * Gets a person's capability to perform this job.
	 * 
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {

		double result = person.getSkillManager().getSkillLevel(SkillType.MEDICINE);

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		double averageAptitude = (academicAptitude + experienceAptitude) / 2D;
		result += result * ((averageAptitude - 100D) / 100D);

		if (person.getPhysicalCondition().hasSeriousMedicalProblems())
			result = 0D;

		return result;
	}

	/**
	 * Gets the base settlement demand for this job.
	 * 
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	public double getSettlementNeed(Settlement settlement) {

		double result = .1;

		int population = settlement.getNumCitizens();

		// Add (labspace * tech level) / 2 for all labs with medical specialties.
		result += getBuildingScienceDemand(settlement, ScienceType.MEDICINE, 5D);

		// Add (tech level / 2) for all medical infirmaries.
		Iterator<Building> j = settlement.getBuildingManager().getBuildingSet(FunctionType.MEDICAL_CARE).iterator();
		while (j.hasNext()) {
			result += (double) j.next().getMedical().getTechLevel() / 3D;
		}

		result = (result + population / 10D) / 5.0;
				
		return result;
	}
}
