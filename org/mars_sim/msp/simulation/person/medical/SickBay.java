/**
 * Mars Simulation Project
 * SickBay.java
 * @version 2.74 2002-03-10
 * @author Barry Evans
 */

package org.mars_sim.msp.simulation.person.medical;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;

/**
 * This class represents the abstract notiation of a SickBay. The Sick bay
 * object contains no knowledge of it's location as it will be encapsulated
 * within a location aware object.
 * It provides a number of Treatments to Persons, these are defined in the
 * Medical.xml file.
 *
 * @see org.mars_sim.msp.simulation.structure.Infirmary
 * @see org.mars_sim.msp.simulation.equipment.MobileSickBay
 */
public class SickBay implements MedicalAid, Serializable {

    private String name;                            // Type name of Sickbay
    private int sickBeds;                           // Number of sick beds
    private ArrayList patients = new ArrayList();   // Patients
    private Collection  supportedTreatments;        // Shared treatments

    /** Construct a Sick Bay.
     *  @param name Name of the Sick bay, this is used to locate support Treatments
     *  @param sickBeds Number of sickbeds.
     */
    public SickBay(String name, int sickBeds) {
        this.name = name;
        this.sickBeds = sickBeds;
    }

    /**
     * Get a problem that can be cured with a Medical skill. This method will
     * search the known problem and find the best one that can be cured.
     *
     * @return Select problem to cure.
     */
    public HealthProblem getCurableProblem() {
        Iterator iter = patients.iterator();
        while (iter.hasNext()) {
            HealthProblem problem = (HealthProblem)iter.next();

            // Find the first rpoblem that is not recovering and curable
            if (!problem.getRecovering())
            {
                problem.startRecovery();
                return problem;
            }
        }
        return null;
    }

    /**
     * Has this SickBay got problem awaiting help. Iterates through the list
     * of waiting patients and finds the first Problem not in a Recovery state.
     *
     * @return Problems are awaiting help.
     */
    public boolean hasWaitingPatients() {
        Iterator iter = patients.iterator();
        while (iter.hasNext()) {
            HealthProblem problem = (HealthProblem)iter.next();
            if (!problem.getRecovering()) {
                return true;
            }
        }
        return false;
    }

    /**
     * A person requests to start the specified treatment using this aid. This
     * aid may elect to record that this treatment is being performed. If the
     * treatment can not be satisfied, then a false return value is provided.
     *
     * @param sufferer Person with problem.
     * @return Can the treatment be satified.
     * @see MedicalAid
     */
    public boolean requestTreatment(HealthProblem problem) {

        Treatment required = problem.getIllness().getRecoveryTreatment();

        // Check the Treatment against supported and there is an available bed
        boolean canHeal = ((patients.size() < sickBeds) &&
                            ((supportedTreatments == null) ||
                            supportedTreatments.contains(required)));

        if (canHeal) {
            patients.add(problem);
            System.out.println(problem.getSufferer() + " enters " + name +
                                " for " + required);

            if (required.getSkill() == 0) {
                // Start now, no other help
                problem.startRecovery();
            }
        }

        return canHeal;
    }

    /**
     * Stop a previously started treatment.
     *
     * @param problem Person with problem.
     */
    public void stopTreatment(HealthProblem problem) {
        patients.remove(problem);
        System.out.println(problem.getSufferer() + " leaves " + name);
    }
}