/**
 * Mars Simulation Project
 * MedicalAid.java
 * @version 3.1.0 2017-03-09
 * @author Barry Evans
 */

package org.mars_sim.msp.core.person.health;

import java.util.List;

import org.mars_sim.msp.core.person.Person;

/**
 * This interface defines an entity that can provide Medical Aid to an
 * injured person. It can provide different types of treatments.
 */
public interface MedicalAid {

    /**
     * Gets the health problems awaiting treatment at the medical station.
     * @return list of health problems
     */
    public List<HealthProblem> getProblemsAwaitingTreatment();

    /**
     * Gets the health problems currently being treated at the medical station.
     * @return list of health problems
     */
    public List<HealthProblem> getProblemsBeingTreated();

    /**
     * Gets the people who are resting to recover from a health problem at the
     * medical station.
     * @return list of people.
     */
    public List<Person> getRestingRecoveryPeople();

    /**
     * Start a person resting to recover from a health problem.
     * @param person the person
     */
    public void startRestingRecovery(Person person);

    /**
     * Stop a person from resting to recover from a health problem.
     * @param person the person.
     */
    public void stopRestingRecovery(Person person);

    /**
     * Get a list of supported Treatments at this medical aid.
     * @return List of treatments.
     */
    public List<Treatment> getSupportedTreatments();

    /**
     * Checks if a health problem can be treated at this medical aid.
     * @param problem The health problem to check treatment.
     * @return true if problem can be treated.
     */
    public boolean canTreatProblem(HealthProblem problem);

    /**
     * Add a health problem to the queue of problems awaiting treatment at this
     * medical aid.
     * @param problem The health problem to await treatment.
     */
    public void requestTreatment(HealthProblem problem);

    /**
     * Remove a health problem from the queue of problems awaiting treatment
     * at this medical aid.
     * @param problem the heath problem awaiting treatment.
     */
    public void cancelRequestTreatment(HealthProblem problem);

    /**
     * Starts the treatment of a health problem in the waiting queue.
     * @param problem the health problem to start treating.
     * @param treatmentDuration the time required to perform the treatment.
     */
    public void startTreatment(HealthProblem problem, double treatmentDuration);

    /**
     * Stop a previously started treatment.
     * @param problem Health problem stopping treatment on.
     */
    public void stopTreatment(HealthProblem problem);
}