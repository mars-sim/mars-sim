package org.mars_sim.msp.core.person.medical;

import java.util.List;

/**
 * This interface defines an entity that can provide Medical Aid to an
 * injured person. It can provide different types of treatments.
 */
public interface MedicalAid {
    
    /**
     * Gets the health problems awaiting treatment at the medical station.
     *
     * @return list of health problems
     */
    public List getProblemsAwaitingTreatment();
    
    /**
     * Gets the health problems currently being treated at the medical station.
     *
     * @return list of health problems
     */
    public List getProblemsBeingTreated();
    
    /**
     * Get a list of supported Treatments at this medical aid.
     *
     * @return List of treatments.
     */
    public List getSupportedTreatments();
    
    /**
     * Checks if a health problem can be treated at this medical aid.
     *
     * @param problem The health problem to check treatment.
     * @return true if problem can be treated.
     */
    public boolean canTreatProblem(HealthProblem problem);
    
    /**
     * Add a health problem to the queue of problems awaiting treatment at this
     * medical aid.
     *
     * @param problem The health problem to await treatment.
     * @throws Exception if health problem cannot be treated here.
     */
    public void requestTreatment(HealthProblem problem) ;

    /**
     * Starts the treatment of a health problem in the waiting queue.
     *
     * @param problem the health problem to start treating.
     * @param treatmentDuration the time required to perform the treatment.
     * @throws Exception if treatment cannot be started.
     */
    public void startTreatment(HealthProblem problem, double treatmentDuration);
    
    /**
     * Stop a previously started treatment.
     *
     * @param problem Health problem stopping treatment on.
     * @throws Exception if health problem is not being treated.
     */
    public void stopTreatment(HealthProblem problem);
}