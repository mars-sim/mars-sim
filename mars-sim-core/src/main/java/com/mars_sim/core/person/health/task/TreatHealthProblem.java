/*
 * Mars Simulation Project
 * TreatHealthProblem.java
 * @date 2025-08-14
 * @author Barry Evans
 */
package com.mars_sim.core.person.health.task;

import java.util.logging.Level;

import com.mars_sim.core.Unit;
import com.mars_sim.core.building.function.MedicalCare;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.EventType;
import com.mars_sim.core.person.Person;
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
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.tool.Msg;

/**
 * A task for performing a medical treatment at a medical station.
 */
public abstract class TreatHealthProblem extends MedicalAidTask {

    private static final long serialVersionUID = 1L;

	private static SimLogger logger = SimLogger.getLogger(TreatHealthProblem.class.getName());

    /** Task phases. */
    private static final TaskPhase TREATMENT = new TaskPhase(Msg.getString(
            "Task.phase.treatingHealthProblem")); //$NON-NLS-1$
    private static final TaskPhase DISPATCH = new TaskPhase(Msg.getString(
            "Task.phase.medicalDispatch")); //$NON-NLS-1$
    
    
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

        if (doctor.isInSettlement())
	        // Initialize phase.
        	setPhase(DISPATCH);
        else 
        	// In future, simulate offering telemedicine via mission control
        	setPhase(TREATMENT);
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (DISPATCH.equals(getPhase())) {
            return dispatchingPhase(time);
        }
        else if (TREATMENT.equals(getPhase())) {
            return treatmentPhase(time);
        }
        else {
            return time;
        }
    }


    /**
     * Dispatches to a medical facility in response to a medical need.
     * 
     * @param time the amount of time (millisol) to perform the phase.
     * @return the amount of time (millisol) left over after performing the phase.
     */
    private double dispatchingPhase(double time) {

    	double timeLeft = 0D;
    	
		// Check if the doctor is already at a medical activity spot	
		boolean success = MedicalCare.dispatchToMedical(worker);

		if (!success) {
			// First walk to a medical activity spot
			success = walkToDoctorStation(true);
			
			if (!success) {
				// If no medical activity spot is available, end the task
				endTask();
				
				return timeLeft / 2;
			}
			else {
				setPhase(TREATMENT);
			}
		}
		else {
			setPhase(TREATMENT);
		}
		
    	return timeLeft;
    }
    
    
    /**
     * Performs the treatment phase of the task.
     * 
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

        	logger.log(worker, Level.INFO, 0, "Treating " + healthProblem.getSufferer().getName()
        			+ " for " + healthProblem.getComplaint().getType().getName());

            // Create starting task event if needed.
            if (getCreateEvents()) {
            	Unit unit = null;
            	if (worker instanceof Person p) {
            		unit = p;
            	}
            	else if (worker instanceof Robot r) {
            		unit = r;
            	}
            	
                TaskEvent startingEvent = new TaskEvent(unit,
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
     * Stops the medical treatment
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
