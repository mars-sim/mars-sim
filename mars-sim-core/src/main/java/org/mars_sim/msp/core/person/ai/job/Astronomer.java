/**
 * Mars Simulation Project
 * Astronomer.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.util.Iterator;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.AstronomicalObservation;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

/**
 * The Astronomer class represents a job for an astronomer.
 */
class Astronomer extends Job  {

	/** Constructor. */
	public Astronomer() {
		// Use Job constructor
		super(JobType.ASTRONOMER, Job.buildRoleMap(5.0, 20.0, 5.0, 5.0, 20.0, 25.0, 10.0, 30.0));
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
		Iterator<Building> j = manager.getBuildings(FunctionType.ASTRONOMICAL_OBSERVATION).iterator();
		while (j.hasNext()) {
			Building building = j.next();
			AstronomicalObservation observatory = building.getAstronomicalObservation();
			result += observatory.getObservatoryCapacity() * observatory.getTechnologyLevel() / 2.0;
		}

		result = (result + population / 24D) / 2.0;
				
		return result;
	}
}
