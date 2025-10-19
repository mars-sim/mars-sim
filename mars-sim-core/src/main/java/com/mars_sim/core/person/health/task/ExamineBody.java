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
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.tool.MathUtils;
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
    private boolean attemptTransfer;
    private double transportRemainingTime = STANDARD_TRANSPORTATION_TIME;
    
    private DeathInfo deathInfo;
	private Person deceasedPerson;
	
	
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
				
		deceasedPerson = body.getPerson();

		// Set deathInfo
		deathInfo = body;

		// Future: need to deal with the need of having more than one doctor to examine the patient.
		// Ideally it should allow more than one doctors. But in terms of finding a doctor activity spot,
		// and it would be messy.
		// Other doctors may have already arrived first and the second one may or may not be necessary
		// and additional activity spots may or may not be available.
		// At the end of the day, only need one doctor to sign off the death certificate.
		
		// First walk to a medical activity spot 
		boolean success = walkToDoctorStation(false);  

		if (!success) {
			logger.info(worker, "Tried to walk to Doctor's station unsuccessfully to examine " + deceasedPerson.getName() + ".");
			// Note: Avoid calling this to instantly send the doctor there.
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

	/**
	 * Prepares for the exam.
	 * 
	 * @param time
	 * @return
	 */
	private double preparingPhase(double time) {
		double remainingTime = 0;

		if (!attemptTransfer) {
			
			attemptTransfer = true;
		}
		
		else {
			
			transportRemainingTime = transportRemainingTime - time;
			
			if (transportRemainingTime < 0) {

				// Send the worker as a patient to a medical bed
				boolean toSend = BuildingManager.addPatientToMedicalBed(deceasedPerson, worker.getSettlement());

				String name = deathInfo.getDoctorRetrievingBody();
				
				if (toSend && name == null) {
				
					deathInfo.setDoctorRetrievingBody(worker.getName());
					
					logger.info(worker, "Just retrieved and transferred the body of " + deceasedPerson.getName() + " to the medical facility.");
					
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
					
					logger.info(worker, "Set estimated mortem exam time on " + deceasedPerson.getName() 
						+ " as " + Math.round(durationExam * 10.0)/10.0 + " millisols.");
					
					// Initialize phase.
					setPhase(EXAMINING);
				}
				
				else {
					logger.info(worker, "Unable to retrieve and transfer the body of " + deceasedPerson.getName() + " to the medical facility.");
					
					// Set it back to null
					deathInfo.setDoctorRetrievingBody(null);
					
					endTask();
				}
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
	
		var mal = getMalfunctionable();

		// If medical aid has malfunction, end task.
		if (mal.getMalfunctionManager().hasMalfunction()) {
			endTask();
			return 0;
		}
		
		// Retrieves the time spent on examining the body
		double timeExam = deathInfo.getTimeSpentExam();
	
		// Get the worker's medical skill.
		double skill = getEffectiveSkillLevel();
		if (skill == 0)
			skill = .5;
		
		double workTime =  time * ( 1 + skill / 4D);
					
		if (timeExam >= deathInfo.getEstTimeExam()
				&& deathInfo.getDoctorSigningCertificate() == null) {
			logger.log(worker, Level.INFO, 0, "Postmortem exam on " 
						+ deceasedPerson.getName() + " completed.");
			
			// Note: there could be multiple physicians performing the exam
			//       This way, if one physician is no longer available others may
			//       finish what he has started.
			
			deathInfo.signOffDeathCertificate(worker.getName());
					
			deathInfo.timestampExamDone(true);			
			// Check for accident in medical aid.
			checkForAccident(mal, timeExam, 0.002);
			// Add experience.
			addExperience(timeExam);		
			// Ready to go to the next task phase
			setPhase(RECORDING);
		}

		else {
			
			// Add exam time as modified by skill
			deathInfo.addTimeSpentExam(workTime);
			
			logger.log(worker, Level.INFO, 30_000, "Performing a postmortem exam on " 
					+ deceasedPerson.getName() + ".");
		}
		
		// if work time is greater than time, then less time is spent on this frame
		return MathUtils.between((workTime - time), 0, time) * .5;
		// Note: 1. workTime can be longer or shorter than time
		//       2. the return time may range from zero to as much as half the tick  
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
		deceasedPerson.buryBody();
		
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
					+ deceasedPerson.getName() + ". Cause of death : " + cause);

		// Create medical event for performing an post-mortem exam
		problem.registerHistoricalEvent(EventType.MEDICAL_POSTMORTEM_EXAM); 
	}
}
