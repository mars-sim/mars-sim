/*
 * Mars Simulation Project
 * ExamineBody.java
 * @date 2022-06-30
 * @author Manny Kung
 */
package com.mars_sim.core.person.health.task;

import java.util.Collections;
import java.util.logging.Level;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.EventType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.health.DeathInfo;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.person.health.MedicalAid;
import com.mars_sim.core.person.health.MedicalEvent;
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
	private static final TaskPhase EXAMINING = new TaskPhase(Msg.getString("Task.phase.examineBody.examining")); //$NON-NLS-1$
	static final TaskPhase RECORDING = new TaskPhase(Msg.getString("Task.phase.examineBody.recording")); //$NON-NLS-1$

    private static final ExperienceImpact IMPACT = new ExperienceImpact(10D, NaturalAttributeType.EXPERIENCE_APTITUDE,
                                                false, 1.5,
												SkillType.MEDICINE);

	// Data members.
	private DeathInfo deathInfo;
	private Person patient;
	
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
	 * @param examiner the person to perform the task
	 * @param body Body to examin
	 */
	private ExamineBody(Person examiner, DeathInfo body, MedicalAid aid) {
		super(NAME, examiner, aid, IMPACT, 0D);

		if (!examiner.isInSettlement()) {
			endTask();
			return;
		}
		
		// Probability affected by the person's stress and fatigue.
        if (!examiner.getPhysicalCondition().isFitByLevel(1000, 50, 500)) {
        	endTask();
        	return;
        }

		// Determine patient and health problem to treat.
		deathInfo = body;
		if (!deathInfo.getBodyRetrieved()) {
			retrieveBody();
		}
					
		patient = deathInfo.getPerson();

		// Get the person's medical skill.
		double skill = examiner.getSkillManager().getEffectiveSkillLevel(SkillType.MEDICINE);
		if (skill == 0)
			skill = .5;
		// Get the person's emotion stability
		int stab = examiner.getNaturalAttributeManager().getAttribute(NaturalAttributeType.EMOTIONAL_STABILITY);
		// Get the person's stress resilience						
		int resilient = examiner.getNaturalAttributeManager().getAttribute(NaturalAttributeType.STRESS_RESILIENCE);
		
		// Note: Need to refine in determining how long the exam would take. 
		// Depends on the cause of death ?
		double durationExam = 150 +  (200 - stab - resilient) / 5D / skill 
				+ 2 * RandomUtil.getRandomInt(5);
		
		deathInfo.setEstTimeExam(durationExam);

		// Walk to medical aid.
		walkToMedicalAid(false);

		// Initialize phase.
		setPhase(EXAMINING);
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (EXAMINING.equals(getPhase())) {
			return examiningPhase(time);
		} else if (RECORDING.equals(getPhase())) {
			return recordingPhase(time);
		} else {
			return time;
		}
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
		double timeExam = deathInfo.getTimeExam();
		
		if (timeExam == 0)
			// Retrieve the body first before beginning the exam
			deathInfo.getBodyRetrieved();
		
		if (timeExam > deathInfo.getEstTimeExam()) {
			logger.log(worker, Level.WARNING, 20_000, "Postmortem exam done on " 
						+ patient.getName() + ".");
			
			deathInfo.setExamDone(true);
			
			// Check for accident in medical aid.
			checkForAccident(mal, timeExam, 0.002);
	
			// Add experience.
			addExperience(timeExam);
			
			// Ready to go to the next task phase
			setPhase(RECORDING);
		}

		else {
			// Get the person's medical skill.
			double skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.MEDICINE);
			if (skill == 0)
				skill = .5;
			// Add exam time as modified by skill
			deathInfo.addTimeExam(time * ( 1 + skill / 4D));
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

		double timeLeft = 0D;
		var mal = getMalfunctionable();

		// If medical aid has malfunction, end task.
		if (mal.getMalfunctionManager().hasMalfunction()) {
			endTask();
			return 0;
		}

		// Record the cause
		recordCause(deathInfo.getProblem());
		
		// Bury the body
		patient.buryBody();
		
		getSimulation().getMedicalManager().addDeathRegistry(person.getSettlement(), deathInfo);
				
		// Check for accident in medical aid.
		checkForAccident(mal, time, 0.003);

		// Add experience.
		addExperience(time);

		endTask();
		

		return timeLeft;
	}

	/**
	 * Retrieves the body.
	 * 
	 * @param problem
	 */
	private void retrieveBody() {
		deathInfo.setBodyRetrieved(true);
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
		MedicalEvent event = new MedicalEvent(person, problem, EventType.MEDICAL_POSTMORTEM_EXAM); 
		// Register event
		registerNewEvent(event);
	}
}
