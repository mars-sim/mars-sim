package org.mars_sim.msp.simulation.structure;

import java.io.Serializable;
import org.mars_sim.msp.simulation.structure.Facility;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.person.medical.MedicalAid;
import org.mars_sim.msp.simulation.person.medical.HealthProblem;
import org.mars_sim.msp.simulation.person.medical.SickBay;

/**
 * This class represents a Infirmary that is based in a Settlement. It uses a
 * Delegation pattern to provide the access to the SickBay object that maintains
 * dispenses the Treatment.
 */
public class Infirmary extends Facility
            implements Serializable, MedicalAid {
    /**
     * Name of Infirmary
     */
    public final static String NAME = "Infirmary";

    private SickBay     sickBay;        // Sickbay of the infirmary

    /** Constructor for random creation.
     *  @param manager the settlement facility manager
     */
    Infirmary(FacilityManager manager) {

        // Use Facility's constructor.
        super(manager, NAME);

        // Initialize random size from 1 to 5.
        sickBay = new SickBay(NAME, 1 + RandomUtil.getRandomInt(4));
    }

    /**
     * Get the SickBay of this infirmary.
     * @return Sickbay object.
     */
    public SickBay getSickBay() {
        return sickBay;
    }

    /**
     * A person requests to start the specified treatment using this aid. This
     * aid may elect to record that this treatment is being performed. If the
     * treatment can not be satisfied, then a false return value is provided.
     *
     * @param sufferer Person with problem.
     * @return Can the treatment be satified.
     */
    public boolean requestTreatment(HealthProblem problem) {
        return sickBay.requestTreatment(problem);
    }

    /**
     * Stop a previously started treatment.
     *
     * @param problem Person with problem.
     */
    public void stopTreatment(HealthProblem problem) {
        sickBay.stopTreatment(problem);

        // Must check if anyome else can join infirmary
    }
}