/*
 * Mars Simulation Project
 * ExamineBody.java
 * @date 2025-08-14
 * @author Manny Kung
 */
package com.mars_sim.core.person.health.task;

import java.util.Collections;
import java.util.logging.Level;

import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.MedicalCare;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.EventType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.person.health.DeathInfo;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.person.health.MedicalAid;
import com.mars_sim.core.person.health.MedicalEvent;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * A task for performing a physical exam over a patient or a postmortem exam on
 * a deceased person at a medical station.
 */
public class ExamineBody extends MedicalAidTask {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ExamineBody.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.examineBody"); //$NON-NLS-1$

	/** Task phases. */
	static final TaskPhase PREPARING = new TaskPhase(Msg.getString("Task.phase.examineBody.preparing")); //$NON-NLS-1$

	static final TaskPhase EXAMINING = new TaskPhase(Msg.getString("Task.phase.examineBody.examining")); //$NON-NLS-1$
	
	static final TaskPhase RECORDING = new TaskPhase(Msg.getString("Task.phase.examineBody.recording")); //$NON-NLS-1$

    private static final ExperienceImpact IMPACT = new ExperienceImpact(10D, NaturalAttributeType.EXPERIENCE_APTITUDE,
                                                false, 1.0, SkillType.MEDICINE);

    private static final double STANDARD_TRANSPORTATION_TIME = 20;
    
	// Data members.
    private double transportRemainingTime = STANDARD_TRANSPORTATION_TIME;
    
	private DeathInfo deathInfo;
	private Person patient;
	
	
	static ExamineBody createTask(Robot examiner, DeathInfo body) {
		MedicalAid aid = MedicalHelper.determineMedicalAid(examiner, Collections.emptySet());

		if (aid == null) {
			logger.warning(examiner, "Could not find medical aid to examine " + body.getPerson().getName());
		}
		return new ExamineBody(examiner, body, aid);
	}
	
	static ExamineBody createTask(Person examiner, DeathInfo body) {
		MedicalAid aid = MedicalHelper.determineMedicalAid(examiner, Collections.emptySet());

		if (aid == null) {
			logger.warning(examiner, "Could not find medical aid to examine " + body.getPerson().getName());
		}
		return new ExamineBody(examiner, body, aid);
	}

	/**
	 * Constructor.
	 * 
	 * @param examiner the worker to perform the task
	 * @param body Body to examine
	 */
	private ExamineBody(Worker examiner, DeathInfo body, MedicalAid aid) {
		super(NAME, examiner, aid, IMPACT, 0D);

		if (!examiner.isInSettlement()) {
			endTask();
			return;
		}
		
		// Probability affected by the person's stress and fatigue.
        if (examiner instanceof Person p && !p.getPhysicalCondition().isFitByLevel(1000, 50, 500)) {
        	endTask();
        	return;
        }
				
		patient = body.getPerson();

		// Set deathInfo
		deathInfo = body;

		// First walk to a medical activity spot 
		boolean success = walkToDoctorStation(false);  

		if (!success) {
			logger.info(worker, "Now trying to go to medical again.");
			// Check if the doctor is already at a medical activity spot	
			success = MedicalCare.dispatchToMedical(worker);
			
			if (!success) {
				// If no medical activity spot is available, end the task
				endTask();
				return ;
			}
		}
		
		// Initialize phase.
		setPhase(PREPARING);
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (PREPARING.equals(getPhase())) {
			return preparingPhase(time);
		} else if (EXAMINING.equals(getPhase())) {
			return examiningPhase(time);
		} else if (RECORDING.equals(getPhase())) {
			return recordingPhase(time);
		} else {
			return time;
		}
	}

	private double preparingPhase(double time) {
		double remainingTime = 0;
	

		String name = deathInfo.getDoctorRetrievingBody();
		
		if (name == null) {
			
			logger.info(worker, "Just retrieved the body of " 
					+ patient.getName() + ".");
			
			deathInfo.setDoctorRetrievingBody(worker.getName());
			
			// The first physician gets to set the estimate exam time
			
			// Get the worker's medical skill.
			double skill = worker.getSkillManager().getEffectiveSkillLevel(SkillType.MEDICINE);
			if (skill == 0)
				skill = .5;
			// Get the worker's emotion stability
			int stab = worker.getNaturalAttributeManager().getAttribute(NaturalAttributeType.EMOTIONAL_STABILITY);
			// Get the worker's stress resilience						
			int resilient = worker.getNaturalAttributeManager().getAttribute(NaturalAttributeType.STRESS_RESILIENCE);
			
			// Note: Need to refine in determining how long the exam would take. 
			// Depends on the cause of death ?
			double durationExam = 150 +  (200 - stab - resilient) / 5D / skill 
					+ 2 * RandomUtil.getRandomInt(5);
			
			// Set exam duration time
			deathInfo.setEstTimeExam(durationExam);
			
			logger.info(worker, "Set estimated exam time as " + Math.round(durationExam * 10.0)/10.0 + " millisols.");
			
		}
		
		else {
			
			transportRemainingTime = transportRemainingTime - time;
			
			if (transportRemainingTime < 0) {

				// Send the worker as a patient to a medical bed
				BuildingManager.addPatientToMedicalBed(patient, worker.getSettlement());
				// Initialize phase.
				setPhase(EXAMINING);
			}
		}

		return remainingTime;
	}
	
	/**
	 * Performs the examining phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the phase.
	 * @return the amount of time (millisol) left over after performing the phase.
	 */
	private double examiningPhase(double time) {
		double remainingTime = 0;

		var mal = getMalfunctionable();

		// If medical aid has malfunction, end task.
		if (mal.getMalfunctionManager().hasMalfunction()) {
			endTask();
			return 0;
		}
		
		// Retrieves the time spent on examining the body
		double timeExam = deathInfo.getTimeSpentExam();
			
		if (timeExam >= deathInfo.getEstTimeExam()
				&& deathInfo.getDoctorSigningCertificate() == null) {
			logger.log(worker, Level.INFO, 0, "Postmortem exam on " 
						+ patient.getName() + " completed.");
			
			// Note: there could be multiple physicians performing the exam
			//       This way, if one physician is no longer available others may
			//       finish what he has started.
			
			deathInfo.setDoctorSigningCertificate(worker.getName());
					
			deathInfo.setExamDone(true);			
			// Check for accident in medical aid.
			checkForAccident(mal, timeExam, 0.002);
			// Add experience.
			addExperience(timeExam);		
			// Ready to go to the next task phase
			setPhase(RECORDING);
		}

		else {
			// Get the worker's medical skill.
			double skill = worker.getSkillManager().getEffectiveSkillLevel(SkillType.MEDICINE);
			if (skill == 0)
				skill = .5;
			// Add exam time as modified by skill
			deathInfo.addTimeSpentExam(time * ( 1 + skill / 4D));
			
			logger.log(worker, Level.INFO, 10_000, "Performing a postmortem exam on " 
					+ patient.getName() + ".");
		}
		
		return remainingTime;
	}

	/**
	 * Performs the recording phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the phase.
	 * @return the amount of time (millisol) left over after performing the phase.
	 */
	private double recordingPhase(double time) {

		double remainingTime = 0D;
		
		var mal = getMalfunctionable();

		// If medical aid has malfunction, end task.
		if (mal.getMalfunctionManager().hasMalfunction()) {
			endTask();
			return remainingTime;
		}

		// Record the cause
		recordCause(deathInfo.getProblem());
		
		// Bury the body
		patient.buryBody();
		
		getSimulation().getMedicalManager().addDeathRegistry(worker.getSettlement(), deathInfo);
				
		// Check for accident in medical aid.
		checkForAccident(mal, time, 0.003);

		// Add experience.
		addExperience(time);

		endTask();
		
		return remainingTime;
	}
	
	/**
	 * Records the cause of death and creates the medical event.
	 * 
	 * @param problem
	 */
	private void recordCause(HealthProblem problem) {
		String cause = deathInfo.getCause();
		String problemStr = problem.toString();
		
		if (!cause.contains(problemStr)) {
			cause += problemStr;
			deathInfo.setCause(cause);
		}
			
		logger.log(worker, Level.WARNING, 1000, "Completed the postmortem exam on " 
					+ patient.getName() + ". Cause of death : " + cause);

		// Create medical event for performing an post-mortem exam
		MedicalEvent event = new MedicalEvent(worker, problem, EventType.MEDICAL_POSTMORTEM_EXAM); 
		// Register event
		registerNewEvent(event);
	}
}
