/**
 * Mars Simulation Project
 * Astronomer.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.util.Iterator;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.AreologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.BiologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.BuildingSalvageMission;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.AstronomicalObservation;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Research;

/**
 * The Astronomer class represents a job for an astronomer.
 */
class Astronomer extends Job  {

	/** Constructor. */
	public Astronomer() {
		// Use Job constructor
		super(JobType.ASTRONOMER, Job.buildRoleMap(5.0, 5.0, 5.0, 20.0, 25.0, 10.0, 30.0));


		// Add astronomer-related missions.
		jobMissionStarts.add(Exploration.class);
		jobMissionJoins.add(Exploration.class);
		
//		jobMissionStarts.add(AreologyFieldStudy.class);
		jobMissionJoins.add(AreologyFieldStudy.class);
		
//		jobMissionStarts.add(BiologyFieldStudy.class);
		jobMissionJoins.add(BiologyFieldStudy.class);
		
//		jobMissionStarts.add(Mining.class);
		jobMissionJoins.add(Mining.class);
				
		jobMissionJoins.add(BuildingConstructionMission.class);
		
		jobMissionJoins.add(BuildingSalvageMission.class);
		
	}

	@Override
	public double getCapability(Person person) {
		double result = 0D;

		int astronomySkill = person.getSkillManager().getSkillLevel(SkillType.ASTRONOMY);
		result = astronomySkill;

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
		result += result * ((academicAptitude - 50D) / 100D);

		if (person.getPhysicalCondition().hasSeriousMedicalProblems())
			result = 0D;

//		System.out.println(person + " astro : " + Math.round(result*100.0)/100.0);

		return result;
	}

	@Override
	public double getSettlementNeed(Settlement settlement) {
		double result = 0.1;
		
		int population = settlement.getNumCitizens();

		BuildingManager manager = settlement.getBuildingManager();

		// Add (labspace * tech level / 2) for all labs with astronomy specialties.
		Iterator<Building> i = manager.getBuildings(FunctionType.RESEARCH).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Research lab = building.getResearch();
			if (lab.hasSpecialty(ScienceType.ASTRONOMY))
				result += lab.getLaboratorySize() * lab.getTechnologyLevel() / 16.0;
		}

		// Add astronomical observatories (observer capacity * tech level * 2).
		Iterator<Building> j = manager.getBuildings(FunctionType.ASTRONOMICAL_OBSERVATION).iterator();
		while (j.hasNext()) {
			Building building = j.next();
			AstronomicalObservation observatory = building.getAstronomicalObservation();
			result += observatory.getObservatoryCapacity() * observatory.getTechnologyLevel() / 2.0;
		}

		result = (result + population / 24D) / 2.0;
		
//		System.out.println(settlement + " Astronomer need: " + result);
		
		return result;
	}
}
