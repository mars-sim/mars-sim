package org.mars_sim.msp.simulation.person.medical;

import org.mars_sim.msp.simulation.person.Person;

/**
 * This interface defines an entity that can provide Medical Aid to an
 * injured person. It can provide different types of treatments.
 */
public interface MedicalAid {

    /**
     * A person requests to start the specified treatment using this aid. This
     * aid may elect to record that this treatment is being performed. If the
     * treatment can not be satisfied, then a false return value is provided.
     *
     * @param sufferer Person with problem.
     * @param treatment Treatment required.
     * @return Can the treatment be started.
     */
    public boolean startTreatment(Person suffer, Treatment treatment);

    /**
     * Stop a previously started treatment.
     *
     * @param sufferer Person with problem.
     * @param treatment Treatment required.
     */
    public void stopTreatment(Person suffer, Treatment treatment);
}