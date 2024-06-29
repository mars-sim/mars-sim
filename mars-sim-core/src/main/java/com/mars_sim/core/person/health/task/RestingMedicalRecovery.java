/**
 * Mars Simulation Project
 * RestingMedicalRecovery.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.person.health.task;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.person.health.HealthProblemState;
import com.mars_sim.core.person.health.MedicalAid;
import com.mars_sim.tools.Msg;

/**
 * A task for resting at a medical station bed to recover from a health problem
 * which requires bed rest.
 */
public class RestingMedicalRecovery extends MedicalAidTask {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static SimLogger logger = SimLogger.getLogger(RestingMedicalRecovery.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.restingMedicalRecovery"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase RESTING = new TaskPhase(Msg.getString(
            "Task.phase.restingInBed")); //$NON-NLS-1$
	
    /** Maximum resting duration (millisols) */
    private static final double RESTING_DURATION = 300D;

    
    private static final ExperienceImpact IMPACT = new ExperienceImpact(10D, NaturalAttributeType.EXPERIENCE_APTITUDE,
                                                false, -2);

    // Data members
    private double restingTime;

    /**
     * Create a resting recovery task for a person
     * @param tired Person needing recovery rest
     */
    static RestingMedicalRecovery createTask(Person tired) {
		MedicalAid aid = MedicalHelper.determineMedicalAid(tired, Collections.emptySet());

		if (aid == null) {
			logger.warning(tired, "Could not find medical aid for recovery");
		}
		return new RestingMedicalRecovery(tired, aid);
	}

    /**
     * Constructor.
     * @param person the person to perform the task
     * @param aid 
     */
    private RestingMedicalRecovery(Person person, MedicalAid aid) {
        super(NAME, person, aid, IMPACT, 0D);

        // Initialize data members.
        restingTime = 0D;

        walkToMedicalAid(true);        

        // Initialize phase.
        setPhase(RESTING);
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (RESTING.equals(getPhase())) {
            return restingPhase(time);
        }
        else {
            return time;
        }
    }

    /**
     * Performs the resting phase of the task.
     * @param time the amount of time (millisol) to perform the phase.
     * @return the amount of time (millisol) left over after performing the phase.
     */
    private double restingPhase(double time) {

        double remainingTime = 0D;
        var aid = getMedicalAid();
        // Add person to medical aid resting recovery people if not already on it.
        if (!aid.getRestingRecoveryPeople().contains(person)) {
            aid.startRestingRecovery(person);
        }

        // Check if exceeding maximum bed rest duration.
        boolean timeOver = false;
        if ((restingTime + time) >= RESTING_DURATION) {
            remainingTime = (restingTime + time) - RESTING_DURATION;
            time = RESTING_DURATION - restingTime;
            restingTime = RESTING_DURATION;
            timeOver = true;
        }
        else {
            restingTime += time;
        }

        // Add bed rest to all health problems that require it.
        Set<HealthProblem> resting = getRestingProblems(person);
        for(HealthProblem problem : resting) {
            problem.addBedRestRecoveryTime(time);
    		logger.log(worker, Level.FINE, 20_000, "Was taking a medical leave and resting");	
        }

        // If person has no more health problems requiring bed rest, end task.
        if (resting.isEmpty()) {
			logger.log(worker, Level.FINE, 0, "Ended the medical leave.");
            endTask();
        }

        // Reduce person's fatigue due to bed rest.
        person.getPhysicalCondition().reduceFatigue(3D * time);

        // If out of bed rest time, end task.
        if (timeOver) {
            endTask();
        }

        return remainingTime;
    }

    /**
     * Stop the resting period for this person if still active
     */
    @Override
    protected void clearDown() {
        // Remove person from medical aid.
        var aid = getMedicalAid();
        if ((aid != null) && aid.getRestingRecoveryPeople().contains(person)) {
            aid.stopRestingRecovery(person);
        }

        super.clearDown();
    }

    /**
     * Get any active health problems that need bed rest
     * @param person
     * @return
     */
    static Set<HealthProblem> getRestingProblems(Person person) {
        return person.getPhysicalCondition().getProblems().stream()
                            .filter(p -> (p.getState() == HealthProblemState.RECOVERING)
                                            && p.requiresBedRest())
                            .collect(Collectors.toSet());
    }
}
