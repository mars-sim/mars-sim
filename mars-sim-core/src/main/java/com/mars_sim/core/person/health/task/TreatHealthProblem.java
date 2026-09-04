/*
 * Mars Simulation Project
 * TreatHealthProblem.java
 * @date 2025-08-14
 * @author Barry Evans
 */
package com.mars_sim.core.person.health.task;

import java.util.logging.Level;

import com.mars_sim.core.building.function.MedicalCare;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact.PhysicalEffort;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.person.health.MedicalAid;
import com.mars_sim.core.person.health.Treatment;
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
                            0.2D, SkillType.MEDICINE);

    private double treatmentDuration;
    
    private HealthProblem healthProblem;


    /**
     * Constructor 1.
     * 
     * @param name
     * @param doctor
     * @param hospital
     * @param condition
     */
    protected TreatHealthProblem(String name, Worker doctor, MedicalAid hospital, HealthProblem condition) {
        super(name, doctor, hospital, IMPACT, 0D);
        
        healthProblem = condition;

       	if (doctor instanceof Person person && person.isSuperUnfit()) {
    		logger.info(doctor, "Super Unfit.");
    		endTask();
    		return;
    	}
       	
        // Get the person's medical skill.
        int skill = doctor.getSkillManager().getEffectiveSkillLevel(SkillType.MEDICINE);

        // Determine medical treatment.
        Treatment treatment = healthProblem.getComplaint().getRecoveryTreatment();
        if (treatment != null) {
            treatmentDuration = treatment.getAdjustedDuration(skill);
        }
        else {
            logger.warning(doctor, healthProblem + " had no treatment plan.");
            endTask();
            return;
        }

        
        if (doctor.isInVehicleInGarage()) {
        	
	        // Initialize phase.
        	setPhase(DISPATCH);
        	
        	logger.info(doctor, 10_000, "Dispatching to  a patient in a garaged vehicle to treat " + healthProblem + ".");
        }
        
        else if (doctor.isInSettlement()) {
        	
	        // Initialize phase.
        	setPhase(DISPATCH);
        	
        	logger.info(doctor, 10_000, "Dispatching to a patient in a settlement to treat " + healthProblem + ".");
        	
//	       	// Send the person as a patient to a medical bed
//            else if (BuildingManager.addPatientToMedicalBed(healer, healer.getSettlement())) {
//    			logger.info(healer, 10_000, "Successfully being added to a medical bed during self-treatment.");
//    		}
//    		else {
//    			logger.info(healer, 10_000, "Unsuccessfully being added to a medical bed during self-treatment.");
//    		}
        }
        else if (doctor.isInVehicle() ) {
        	
	        // Initialize phase.
        	setPhase(TREATMENT);
        	
        	logger.info(doctor, 10_000, "Instructing a patient to treat " + healthProblem + ".");
        }
        else {
        	logger.info(doctor, 10_000, "Being outside and unable to treat " + healthProblem + ".");
        	endTask();
        }
        
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

    	if (worker instanceof Person person && person.isSuperUnfit()) {
    		logger.info(worker, "Super Unfit.");
    		endTask();
    		return time;
    	}
    	
    	double timeLeft = 0D;
    	
		// Check if the doctor is already at a medical activity spot	
		boolean success = walkToDoctorStation(true);

		if (!success) {
			logger.info(worker, 10_000, "Unsuccessfully tried to walk to Doctor's station to treat " + healthProblem + ".");
			
			// First walk to a medical activity spot
			success = MedicalCare.dispatchToMedical(worker);
			
			if (!success) {
				logger.info(worker, 10_000, "Unsuccessfully dispatched to Doctor's station to treat " + healthProblem + ".");
				
				// If no medical activity spot is available, end the task
				
				// Note: for now, do NOT call endTask, or else this task may not be able to get done
				
//				endTask();
				
//				return timeLeft / 2;
			}
			else {
				logger.info(worker, 10_000, "Successfully dispatched to Doctor's station to treat " + healthProblem + ".");
			}
		}
		else {
			logger.info(worker, 10_000, "Successfully arrived at Doctor's station to treat " + healthProblem + ".");
		}
		
		setPhase(TREATMENT);
		
    	return timeLeft;
    }
    
    
    /**
     * Performs the treatment phase of the task.
     * 
     * @param time the amount of time (millisol) to perform the phase.
     * @return the amount of time (millisol) left over after performing the phase.
     */
    private double treatmentPhase(double time) {

    	if (worker instanceof Person person && person.isSuperUnfit()) {
    		logger.info(worker, "Super Unfit.");
    		endTask();
    		return time;
    	}
    	
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
            String des = "";
            if (worker.getName().equals(healthProblem.getSufferer().getName())) {
            	des = "Self-treating for " + healthProblem.getComplaint().getType().getName();
            	logger.log(worker, Level.INFO, 0, des + ".");
            }
            else {
            	des = "Treating " + healthProblem.getSufferer().getName()
            			+ " for " + healthProblem.getComplaint().getType().getName();
            	logger.log(worker, Level.INFO, 0, des + ".");
            	
            }
            setDescription(des);
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
