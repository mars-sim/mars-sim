package org.mars_sim.msp.simulation.structure;

import java.io.Serializable;
import org.mars_sim.msp.simulation.structure.Facility;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.person.medical.MedicalAid;
import org.mars_sim.msp.simulation.person.medical.Treatment;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.PersonCollection;

/**
 * This class represents a Infirmary that is based in a Settlement. It can
 * provide different type of treatments.
 */
public class Infirmary extends Facility
            implements Serializable, MedicalAid {
    /**
     * Name of Infirmary
     */
    public final static String NAME = "Infirmary";

    private int sickBeds;               // Number of sick beds
    private PersonCollection patients;  // Patients

    /** Constructor for random creation.
     *  @param manager the settlement facility manager
     */
    Infirmary(FacilityManager manager) {

        // Use Facility's constructor.
        super(manager, NAME);

        // Initialize random size from 1 to 5.
        sickBeds = 1 + RandomUtil.getRandomInt(4);
    }

    /**
     * A person requests to start the specified treatment using this aid. This
     * aid may elect to record that this treatment is being performed. If the
     * treatment can not be satisfied, then a false return value is provided.
     *
     * @param sufferer Person with problem.
     * @param treatment Treatment required.
     * @return Can the treatment be started.
     */
    public boolean startTreatment(Person suffer, Treatment treatment) {
        System.out.println(suffer.getName() + " enters infirmary for " +
                            treatment);

        return true;
    }

    /**
     * Stop a previously started treatment.
     *
     * @param sufferer Person with problem.
     * @param treatment Treatment required.
     */
    public void stopTreatment(Person suffer, Treatment treatment) {
        System.out.println(suffer.getName() + " leaves infirmary with " +
                           treatment);
    }
}