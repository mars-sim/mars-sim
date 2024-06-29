/**
 * Mars Simulation Project
 * RequestMedicalTreatment.java
 * @date 2021-12-22
 * @author Scott Davis
 */
package com.mars_sim.core.person.health.task;

import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.person.health.MedicalAid;
import com.mars_sim.tools.Msg;

/**
 * A task for requesting and awaiting medical treatment at a medical station.
 */
public class RequestMedicalTreatment extends MedicalAidTask {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	/** Simple Task name */
	public static final String SIMPLE_NAME = RequestMedicalTreatment.class.getSimpleName();
	
    /** Task name */
    public static final String NAME = Msg.getString(
            "Task.description.requestMedicalTreatment"); //$NON-NLS-1$

    /** Task phases. */
    static final TaskPhase WAITING_FOR_TREATMENT = new TaskPhase(Msg.getString(
            "Task.phase.waitingForMedicalTreatment")); //$NON-NLS-1$
    static final TaskPhase TREATMENT = new TaskPhase(Msg.getString(
            "Task.phase.receivingMedicalTreatment")); //$NON-NLS-1$

    /** Maximum waiting duration in millisols. */
    private static final double MAX_WAITING_DURATION = 200D;

    private static final ExperienceImpact IMPACT = new ExperienceImpact(10D, NaturalAttributeType.EXPERIENCE_APTITUDE,
                                                false, 0.3);

    // Data members.
    private double waitingDuration;
    private boolean requested;

    static RequestMedicalTreatment createTask(Person patient) {
        // Get problems that need help
        var curable = getRequestableTreatment(patient);
        if (curable.isEmpty()) {
            return null;
        }
        		
        // Choose available medical aid for treatment.
        var medicalAid = MedicalHelper.determineMedicalAid(patient, curable);
        if (medicalAid == null) {
            return null;
        }

        return new RequestMedicalTreatment(patient, medicalAid);
    }

    /**
     * Create a task where a patient requests medical treatment at a medical aid.
     * 
     * @param patient the person to perform the task
     * @param aid Where will teh treatment be done
     * 
     */
    private RequestMedicalTreatment(Person patient, MedicalAid aid) {
        super(NAME, patient, aid, IMPACT, 0);
	    
        walkToMedicalAid(true);

        // Initialize phase.
        setPhase(WAITING_FOR_TREATMENT);
    }

    /**
     * Find any health problems that this patient can ask for help.
     * @param person
     * @return
     */
    static Set<HealthProblem> getRequestableTreatment(Person person) {
        return person.getPhysicalCondition().getProblems().stream()
                                .filter(p -> (p.getComplaint().getRecoveryTreatment() != null) 
                                        && !p.getComplaint().getRecoveryTreatment().getSelfAdminister())
                               .collect(Collectors.toSet());
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (WAITING_FOR_TREATMENT.equals(getPhase())) {
            return waitingForTreatmentPhase(time);
        }
        else if (TREATMENT.equals(getPhase())) {
            return treatmentPhase(time);
        }
        else {
            return time;
        }
    }

    /**
     * Performs the waiting for treatment phase of the task.
     * @param time the amount of time (millisol) to perform the phase.
     * @return the amount of time (millisol) left over after performing the phase.
     */
    private double waitingForTreatmentPhase(double time) {

        double remainingTime = 0D;
        var medicalAid = getMedicalAid();

        // Check if any health problems are currently being treated.
        if (underTreatment(medicalAid)) {
            setPhase(TREATMENT);
            remainingTime = time;
        }
        else {
            // Request treatment on first waiting call
            if (!requested) {

                requested = true;

                // Check if health problems are awaiting treatment.
                for(HealthProblem problem : person.getPhysicalCondition().getProblems()) {
                    if (!medicalAid.getProblemsAwaitingTreatment().contains(problem)
                            && medicalAid.canTreatProblem(problem)) {
                        // Request treatment for health problem.
                        medicalAid.requestTreatment(problem);
                    }
                }
            }

            // Check have not waited too long
            waitingDuration += time;
            if (waitingDuration >= MAX_WAITING_DURATION) {
                // End task if longer than maximum waiting duration.
                remainingTime = waitingDuration - MAX_WAITING_DURATION;
                endTask();
            }
        }

        return remainingTime;
    }

    private boolean underTreatment(MedicalAid aid) {
        for(HealthProblem problem : person.getPhysicalCondition().getProblems()) {
            if (aid.getProblemsBeingTreated().contains(problem)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Performs the treatment phase of the task.
     * @param time the amount of time (millisol) to perform the phase.
     * @return the amount of time (millisol) left over after performing the phase.
     */
    private double treatmentPhase(double time) {

        double remainingTime = 0D;
        var medicalAid = getMedicalAid();

        // Check if any health problems are currently being treated.
        if (!underTreatment(medicalAid)) {

            // Check if person still has health problems needing treatment.
            boolean treatableProblems = person.getPhysicalCondition().getProblems().stream()
                                    .anyMatch(hp -> medicalAid.getProblemsAwaitingTreatment().contains(hp));
            if (treatableProblems) {
                // If any remaining treatable problems, wait for treatment.
                setPhase(WAITING_FOR_TREATMENT);
                waitingDuration = 0D;
                remainingTime = time;
            }
            else {
                // Nothing being treated so done
                endTask();
            }
        }

        return remainingTime;
    }
}
