package org.mars_sim.msp.simulation.person.medical;

/**
 * This interface defines an entity that can provide Medical Aid to an
 * injured person. It can provide different types of treatments.
 */
public interface MedicalAid {

    /**
     * A person requests a specified treatment using this aid. This
     * aid may elect to record that this treatment is being performed. If the
     * treatment can not be satisfied, then a false return value is provided.
     * The Treatment will be started by the MeidcalAid once the internal
     * condition is correct.
     *
     * @param problem Treatment to solve this problem.
     * @return Can the treatment be satifies some time in the future.
     */
    public boolean requestTreatment(HealthProblem problem);

    /**
     * Stop a previously started treatment.
     *
     * @param problem Problem cured.
     */
    public void stopTreatment(HealthProblem problem);
}