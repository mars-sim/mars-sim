/*
 * Mars Simulation Project
 * TreatHealthProblem.java
 * @date 2024-06-08
 * @author Barry Evans
 */
package com.mars_sim.core.person.health.task;

import java.util.logging.Level;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.EventType;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact.PhysicalEffort;
import com.mars_sim.core.person.ai.task.util.TaskEvent;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.person.health.MedicalAid;
import com.mars_sim.core.person.health.Treatment;
import com.mars_sim.tools.Msg;

/**
 * A task for performing a medical treatment at a medical station.
 */
public abstract class TreatHealthProblem extends MedicalAidTask {

    private static SimLogger logger = SimLogger.getLogger(TreatHealthProblem.class.getName());

    /** Task phases. */
    private static final TaskPhase TREATMENT = new TaskPhase(Msg.getString(
            "Task.phase.treatingHealthProblem")); //$NON-NLS-1$

    private static final ExperienceImpact IMPACT = new ExperienceImpact(25D,
                            NaturalAttributeType.EXPERIENCE_APTITUDE, PhysicalEffort.NONE,
                            0.1D, SkillType.MEDICINE);

    private HealthProblem healthProblem;
    private double treatmentDuration;

    protected TreatHealthProblem(String name, Worker doctor, MedicalAid hospital, HealthProblem condition) {
        super(name, doctor, hospital, IMPACT, 0D);
        
        healthProblem = condition;

        // Get the person's medical skill.
        int skill = doctor.getSkillManager().getEffectiveSkillLevel(SkillType.MEDICINE);

        // Determine medical treatment.
        Treatment treatment = healthProblem.getComplaint().getRecoveryTreatment();
        if (treatment != null) {
            treatmentDuration = treatment.getAdjustedDuration(skill);
        }
        else {
            logger.warning(doctor, healthProblem + " does not have treatment.");
            endTask();
            return;
        }

        // Initialize phase.
        setPhase(TREATMENT);
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (TREATMENT.equals(getPhase())) {
            return treatmentPhase(time);
        }
        else {
            return time;
        }
    }

    /**
     * Performs the treatment phase of the task.
     * @param time the amount of time (millisol) to perform the phase.
     * @return the amount of time (millisol) left over after performing the phase.
     */
    private double treatmentPhase(double time) {

        var mal = getMalfunctionable();

        // If medical aid has malfunction, end task.
        if (mal.getMalfunctionManager().hasMalfunction()) {
            endTask();
            return time;
        }

        double timeLeft = 0D;

        // Start treatment if not already started.
        var aid = getMedicalAid();
        if (!aid.getProblemsBeingTreated().contains(healthProblem)) {
            aid.startTreatment(healthProblem, treatmentDuration);

        	logger.log(person, Level.INFO, 0, "Treating " + healthProblem.getSufferer().getName()
        			+ " for " + healthProblem.getComplaint().getType().getName());

            // Create starting task event if needed.
            if (getCreateEvents()) {
                TaskEvent startingEvent = new TaskEvent(person,
                		this, 
                		healthProblem.getSufferer(),
                		EventType.TASK_START,
                		getName()
                );
                registerNewEvent(startingEvent);
            }
        }

        // Check for accident in medical aid.
        checkForAccident(mal, time, 0.005);

        treatmentDuration -= time;
        if (treatmentDuration <= 0) {
            healthProblem.startRecovery();
            timeLeft = -treatmentDuration;
            endTask();
        }

        // Add experience.
        addExperience(time);

        return timeLeft;
    }

    /**
     * Stop mediical treatment
     */
    @Override
    protected void clearDown() {
        // Stop treatment.
        var aid = getMedicalAid();
        if ((aid != null) && aid.getProblemsBeingTreated().contains(healthProblem)) {
            aid.stopTreatment(healthProblem);
        }

        super.clearDown();
    }
}
