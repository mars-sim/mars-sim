/**
 * Mars Simulation Project
 * Architect.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.robot.ai.job;

import java.io.Serializable;

import org.mars_sim.msp.core.person.ai.task.ConsolidateContainers;
import org.mars_sim.msp.core.person.ai.task.ManufactureConstructionMaterials;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * The Architect class represents an architect job focusing on construction of buildings, settlement
 * and other structures.
 */
public class Constructionbot
extends RobotJob
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	//private static Logger logger = Logger.getLogger(Architect.class.getName());

	/** Constructor. */
	public Constructionbot() {
		// Use Job constructor.
		super(Constructionbot.class);

		// Add architect-related tasks.
		jobTasks.add(ManufactureConstructionMaterials.class);
		//jobTasks.add(ConstructBuilding.class); //determine to what extend the walking bug is affecting the outdoor portion of this task
		//jobTasks.add(SalvageBuilding.class); //determine to what extend the walking bug is affecting the outdoor portion of this task
        jobTasks.add(ConsolidateContainers.class); //determine to what extend the walking bug is affecting the outdoor portion of this task


		// Add architect-related missions.
		//jobMissionStarts.add(BuildingConstructionMission.class);
		//jobMissionJoins.add(BuildingConstructionMission.class);
		//jobMissionStarts.add(BuildingSalvageMission.class);
		//jobMissionJoins.add(BuildingSalvageMission.class);

	}

//	@Override
//	public double getCapability(Person person) {
//
//		double result = 0D;
//
//		int constructionSkill = person.getMind().getSkillManager().getSkillLevel(SkillType.CONSTRUCTION);
//		result = constructionSkill;
//
//		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
//		int academicAptitude = attributes.getAttribute(NaturalAttribute.ACADEMIC_APTITUDE);
//		int experienceAptitude = attributes.getAttribute(NaturalAttribute.EXPERIENCE_APTITUDE);
//		double averageAptitude = (academicAptitude + experienceAptitude) / 2D;
//		result+= result * ((averageAptitude - 50D) / 100D);
//
//		if (person.getPhysicalCondition().hasSeriousMedicalProblems()) result = 0D;
//
//		return result;
//	}

	@Override
	public double getSettlementNeed(Settlement settlement) {
		double result = 0D;
		// Add number of buildings currently at settlement.
		result += settlement.getBuildingManager().getNumBuildings() / 10D;
		return result;
	}

	@Override
	public double getCapability(Robot robot) {
		// TODO Auto-generated method stub
		return 0;
	}
}