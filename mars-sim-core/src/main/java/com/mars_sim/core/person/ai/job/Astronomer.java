/*
 * Mars Simulation Project
 * Astronomer.java
 * @date 2025-07-29
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.job;

import java.util.Iterator;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.AstronomicalObservation;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobSpec;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.structure.Settlement;

/**
 * The Astronomer class represents a job for an astronomer.
 */
public class Astronomer extends JobSpec  {

	/** Constructor. */
	public Astronomer() {
		// Use Job constructor
		super(JobType.ASTRONOMER, JobSpec.buildRoleMap(5.0, 20.0, 5.0, 5.0, 20.0, 25.0, 10.0, 30.0));
	}

	@Override
	public double getCapability(Person person) {
		double result = person.getSkillManager().getSkillLevel(SkillType.ASTRONOMY);

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
		result += result * ((academicAptitude - 50D) / 100D);

		if (person.getPhysicalCondition().hasSeriousMedicalProblems())
			result = 0D;

		return result;
	}

	@Override
	public double getSettlementNeed(Settlement settlement) {
		double result = 0.1;
		
		int population = settlement.getNumCitizens();

		BuildingManager manager = settlement.getBuildingManager();

		// Add (labspace * tech level / 2) for all labs with astronomy specialties.
		result += getBuildingScienceDemand(settlement, ScienceType.ASTRONOMY, 16D);

		// Add astronomical observatories (observer capacity * tech level * 2).
		Iterator<Building> j = manager.getObservatories().iterator();
		while (j.hasNext()) {
			Building building = j.next();
			AstronomicalObservation observatory = building.getAstronomicalObservation();
			result += observatory.getObservatoryCapacity() * observatory.getTechnologyLevel() / 2.0;
		}

		result = (result + population / 30D) / 4.0;
				
		return result;
	}
}
