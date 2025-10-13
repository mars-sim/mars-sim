/*
 * Mars Simulation Project
 * ExamineBodyMeta.java
 * @date 2025-08-14
 * @author Manny Kung
 */
package com.mars_sim.core.person.health.task;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.person.health.DeathInfo;
import com.mars_sim.core.person.health.MedicalManager;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * Meta task for the ExamineBody task.
 */
public class ExamineBodyMeta  extends MetaTask implements SettlementMetaTask {
	/**
     * Represents a Job needed for body examination
     */
    private static class ExamineBodyJob extends SettlementTask {

		private static final long serialVersionUID = 1L;

        private DeathInfo patient;

        public ExamineBodyJob(SettlementMetaTask owner, DeathInfo patient, RatingScore score) {
			super(owner, "Examine Body", patient.getPerson(), score);
            this.patient = patient;
        }

        @Override
        public Task createTask(Person person) {
            return ExamineBody.createTask(person, patient);
        }
        
        @Override
        public Task createTask(Robot robot) {
            return ExamineBody.createTask(robot, patient);
        }
    }

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.examineBody"); //$NON-NLS-1$

	// High score so that it gets done soon
	private static final double DEFAULT_SCORE = 500D;

	// Extra score for every day body not examined
	private static final double MOD_SCORE = 2;

	private static MedicalManager medicalManager;

    public ExamineBodyMeta() {
		super(NAME, WorkerType.ROBOT, TaskScope.ANY_HOUR);

		setTrait(TaskTrait.MEDICAL, TaskTrait.TREATMENT);
		setPreferredJob(JobType.MEDICS);
		addPreferredRobot(RobotType.MEDICBOT);
		
		addAllCrewRoles();
	}

	/**
     * Gets the score for a Settlement task for a person. to examine a body based on Job & Skill.
     * 
	 * @param t Task being scored
	 * @param person Person requesting work.
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
    @Override
	public RatingScore assessPersonSuitability(SettlementTask t, Person person) {
        RatingScore factor = RatingScore.ZERO_RATING;
        if (person.isInSettlement() &&
				person.getPhysicalCondition().isFitByLevel(1000, 70, 1000)) {

			// Effort-driven task modifier.
			factor = super.assessPersonSuitability(t, person);
			if (factor.getScore() == 0D) {
				return factor;
			}

			double skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.MEDICINE);
			if (skill == 0)
				skill = 0.01D;
			factor.addModifier(SKILL_MODIFIER, skill);
		}
		return factor;
	}

	/**
     * Gets the score for a Settlement task for a robot. to examine a body based on Job & Skill.
     * 
	 * @param t Task being scored
	 * @param robot Robot requesting work.
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
    @Override
	public RatingScore assessRobotSuitability(SettlementTask t, Robot robot) {
        RatingScore factor = RatingScore.ZERO_RATING;
        if (robot.isInSettlement()) {

			// Effort-driven task modifier.
			factor = super.assessRobotSuitability(t, robot);
			if (factor.getScore() == 0D) {
				return factor;
			}

			double skill = robot.getSkillManager().getEffectiveSkillLevel(SkillType.MEDICINE);
			if (skill == 0)
				skill = 0.01D;
			factor.addModifier(SKILL_MODIFIER, skill);
		}
		return factor;
	}

	/**
	 * Scans the settlement for any post mortems that are needed.
	 * 
	 * @param settlement the settlement to scan.
	 */
	@Override
	public List<SettlementTask> getSettlementTasks(Settlement settlement) {
		List<SettlementTask>  tasks = new ArrayList<>();
		List<DeathInfo> deaths = medicalManager.getPostmortemExam(settlement);

		if (!deaths.isEmpty()) { // && hasNeedyMedicalAidsAtSettlement(settlement)) {
			for(DeathInfo info : deaths) {
				if (!info.getExamDone() 
						&& isVehicleContainerUnitSettlement(info.getPerson())) {
					RatingScore score = new RatingScore(DEFAULT_SCORE);
					score.addBase("due", 
							getMarsTime().getTimeDiff(info.getTimeOfDeath()) * MOD_SCORE);
					tasks.add(new ExamineBodyJob(this, info, score));
				}
			}
		}

		return tasks;
	}

	/**
	 * Is this person in a vehicle parked in the settlement vicinity or in a garage ?
	 * 
	 * @param person
	 * @return
	 */
	private boolean isVehicleContainerUnitSettlement(Person person) {
		boolean result = false;
		
		boolean isInSettlement = person.isInSettlement();
		
		if (isInSettlement) {
			return true;
		}
		
		Vehicle vehicle = person.getVehicle();
		if (vehicle != null && 
				(vehicle.isInGarage() || vehicle.isInSettlement() || vehicle.isRightOutsideSettlement())) {
			// In future, need to model how this person is to be transported 
			return true;
		}
		
		return result;
	}
	
	/**
	 * Checks if there are medical aids at a settlement that have people waiting for
	 * treatment.
	 *
	 * @param settlement the settlement.
	 * @return true if needy medical aids.
	 */
	private boolean hasNeedyMedicalAidsAtSettlement(Settlement settlement) {

		// Check all medical care buildings.
		for (Building b : settlement.getBuildingManager().getBuildingSet(FunctionType.MEDICAL_CARE)) {
			// Check if there are any sick beds at building.
			if (b.getMedical().hasPatients()) {
				return true;
			}
		}

		return false;
	}

	public static void initialiseInstances(MedicalManager mm) {
		medicalManager = mm;
	}
}
