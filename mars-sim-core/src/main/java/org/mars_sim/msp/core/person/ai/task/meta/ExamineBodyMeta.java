/*
 * Mars Simulation Project
 * ExamineBodyMeta.java
 * @date 2021-12-22
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.ExamineBody;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.SettlementMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.SettlementTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.person.health.DeathInfo;
import org.mars_sim.msp.core.person.health.MedicalManager;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.MedicalCare;

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

        public ExamineBodyJob(SettlementMetaTask owner, DeathInfo patient, double score) {
			super(owner, "Examine body of " + patient.getPerson().getName(), score);
            this.patient = patient;
        }

        @Override
        public Task createTask(Person person) {
            return new ExamineBody(person, patient);
        }
    }

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.examineBody"); //$NON-NLS-1$

	// High score so that it gets done soon
	private static final double DEFAULT_SCORE = 500D;

	// Extra score for every day body not examined
	private static final int SOL_SCORE = 50;

	private static MedicalManager medicalManager;

    public ExamineBodyMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);

		setTrait(TaskTrait.MEDICAL);
		setPreferredJob(JobType.MEDICS);
	}

	/**
     * Get the score for a Settlement task for a person. to examine a body based on Job & Skill
	 * @param t Task being scored
	 * @parma p Person requesting work.
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
    @Override
	public double getPersonSettlementModifier(SettlementTask t, Person p) {
        double factor = 0D;
        if (p.isInSettlement() &&
				p.getPhysicalCondition().isFitByLevel(1000, 70, 1000)) {

			// Effort-driven task modifier.
			factor = getPersonModifier(p);

			double skill = p.getSkillManager().getEffectiveSkillLevel(SkillType.MEDICINE);
			if (skill == 0)
				skill = 0.01D;
			factor *= skill;
		}
		return factor;
	}


	/**
	 * Scan the Settlement for any post mortems that are needed
	 * @param settlement Settlemnt to scan.
	 */
	@Override
	public List<SettlementTask> getSettlementTasks(Settlement settlement) {
		List<SettlementTask>  tasks = new ArrayList<>();
		List<DeathInfo> deaths = medicalManager.getPostmortemExams(settlement);

		if (!deaths.isEmpty() && hasNeedyMedicalAidsAtSettlement(settlement)) {
			for(DeathInfo pm : deaths) {
				if (!pm.getExamDone()) {
					double score = DEFAULT_SCORE +
							((marsClock.getMissionSol() - pm.getMissionSol()) * SOL_SCORE);
					tasks.add(new ExamineBodyJob(this, pm, score));
				}
			}
		}

		return tasks;
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
		for(Building b : settlement.getBuildingManager().getBuildings(FunctionType.MEDICAL_CARE)) {
			// Check if there are any sick beds at building.
			MedicalCare medicalCare = b.getMedical();
			if (medicalCare.hasEmptyBeds()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Robots 
	 */
	@Override
	public double getRobotSettlementModifier(SettlementTask t, Robot r) {
		return 0;
	}

	public static void initialiseInstances(MedicalManager mm) {
		medicalManager = mm;
	}
}
