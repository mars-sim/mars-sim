/**
 * Mars Simulation Project
 * Meteorologist.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.AreologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.BiologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Research;

/**
 * The Meteorologist class represents a job for a meteorologist.
 */
class Meteorologist extends Job {
		
	/** Constructor. */
	public Meteorologist() {
		// Use Job constructor
		super(JobType.METEOROLOGIST, Job.buildRoleMap(5.0, 10.0, 10.0, 10.0, 15.0, 20.0, 30.0));
			

		// Add meteorologist-related missions.
		jobMissionStarts.add(AreologyFieldStudy.class);		
		jobMissionJoins.add(AreologyFieldStudy.class);
		
		jobMissionJoins.add(BiologyFieldStudy.class);
		
		jobMissionStarts.add(Exploration.class);
		jobMissionJoins.add(Exploration.class);
		
//		jobMissionJoins.add(BuildingConstructionMission.class);
//		jobMissionJoins.add(BuildingSalvageMission.class);
	}

	@Override
	public double getCapability(Person person) {
		double result = 0D;

		int meteorologySkill = person.getSkillManager().getSkillLevel(SkillType.METEOROLOGY);
		result = meteorologySkill;

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
		
		// Add (labspace * tech level / 2) for all labs with meteorology specialties.
		List<Building> laboratoryBuildings = settlement.getBuildingManager().getBuildings(FunctionType.RESEARCH);
		Iterator<Building> i = laboratoryBuildings.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Research lab = building.getResearch();
			if (lab.hasSpecialty(ScienceType.METEOROLOGY)) {
				result += (lab.getLaboratorySize() * lab.getTechnologyLevel() / 12.0);
			}
		}

		result = (result + population / 12D) / 2.0;
		
//		System.out.println(settlement + " Meteorologist need: " + result);
		
		return result;
	}
}
