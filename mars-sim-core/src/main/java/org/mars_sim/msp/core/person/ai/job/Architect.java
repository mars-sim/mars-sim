/**
 * Mars Simulation Project
 * Architect.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.BuildingSalvageMission;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * The Architect class represents an architect job focusing on construction of buildings, settlement
 * and other structures.
 */
class Architect
extends Job {

	/** Constructor. */
	public Architect() {
		// Use Job constructor.
		super(JobType.ARCHITECT, Job.buildRoleMap(5.0, 30.0, 10.0, 15.0, 10.0, 15.0, 15.0));

		// Add architect-related missions.
		jobMissionStarts.add(BuildingConstructionMission.class);
		jobMissionJoins.add(BuildingConstructionMission.class);
		
		jobMissionStarts.add(BuildingSalvageMission.class);
		jobMissionJoins.add(BuildingSalvageMission.class);

	}

	@Override
	public double getCapability(Person person) {

		double result = 0D;

		int constructionSkill = person.getSkillManager().getSkillLevel(SkillType.CONSTRUCTION);
		result = constructionSkill;

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
		int arts = attributes.getAttribute(NaturalAttributeType.ARTISTRY);
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		double averageAptitude = (academicAptitude + experienceAptitude + arts) / 3D;
		result+= result * ((averageAptitude - 50D) / 100D);

		if (person.getPhysicalCondition().hasSeriousMedicalProblems()) result = 0;

		return result;
	}

	@Override
	public double getSettlementNeed(Settlement settlement) {
		double result = .1;
		
		int population = settlement.getNumCitizens();
		
		// Add number of buildings currently at settlement.
		result += settlement.getBuildingManager().getNumBuildings() / 24D;
		
		result = (result + population / 24D) / 2.0;
		
		return result;
	}
}
