/**
 * Mars Simulation Project
 * SickBay.java
 * @version 2.74 2002-03-10
 * @author Barry Evans
 */

package org.mars_sim.msp.simulation.person.medical;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.malfunction.*;

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
    private int level;                              // Level of facility
    private int sickBeds;                           // Number of sick beds
    private int treatedPatients;                    // Number of patients treated
    private ArrayList patients = new ArrayList();   // Patients treated & queuing
    private Collection supportedTreatments;         // Shared treatments
    private Malfunctionable owner;                  // Owner entity of sickbay

    /** Construct a Sick Bay.
     *  @param name Name of the Sick bay, this is used to locate support Treatments
     *  @param sickBeds Number of sickbeds.
     *  @param mars Overall simulation control.
     *  @param owner The owner entity of the sickbay.
     */
    public SickBay(String name, int sickBeds, int level, Mars mars, Malfunctionable owner) {
        this.name = name;
        this.sickBeds = sickBeds;
        this.level = level;
        this.treatedPatients = 0;
	this.owner = owner;

        supportedTreatments =
                    mars.getMedicalManager().getSupportedTreatments(level);
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

            // Find the first problem that is not awaiting Treatment here.
            if (problem.getAwaitingTreatment()) {
                return problem;
            }
        }
        return null;
    }

    /**
     * Get the patients both treated and queuing.
     * @return List of HealthProblems.
     */
    public List getPatients() {
        return patients;
    }

    /**
     * Reutnrs the nunber of Sick beds
     * @return Sick bed count.
     */
    public int getSickBeds() {
        return sickBeds;
    }

    /**
     * This method returns the number of pateints being treated.
     * @return Patient count.
     */
    public int getTreatedPatientCount() {
        return treatedPatients;
    }

    /**
     * Get a list of supported Treatments at this SickBay.
     */
    public Collection getTreatments() {
        return supportedTreatments;
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
            if (!problem.getRecovering() &&
                !problem.getIllness().getRecoveryTreatment().getSelfAdminister()) {
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
     * @return Can the treatment be satified at some future time.
     * @see MedicalAid
     * @see org.mars_sim.msp.simulation.person.ai.MedicalAssistance
     */
    public boolean requestTreatment(HealthProblem problem) {

        Treatment required = problem.getIllness().getRecoveryTreatment();
        boolean canHeal = supportedTreatments.contains(required);

        // Check the Treatment against supported
        if (canHeal) {
            patients.add(problem);

            // If the patient can administer the Treatment them selves then.
            if (required.getSelfAdminister() && (treatedPatients < sickBeds)) {
                // Start now, no other help
                problem.startTreatment(required.getDuration());
                treatedPatients++;
            }
        }

        return canHeal;
    }

    /**
     * This problem has now started its treatment
     */
    public void startTreatment(HealthProblem started) {
        if (patients.contains(started)) {
            treatedPatients++;
        }
        else {
            System.out.println("WARNING : Unexpected patient is startTreatment " +
                                started);
        }
    }

    /**
     * Stop a previously started treatment.
     *
     * @param stopped Person with problem.
     */
    public void stopTreatment(HealthProblem stopped) {
        if (patients.remove(stopped)) {
            treatedPatients--;

            // See if first one awaiting treatment can heal themselves
            if (patients.size() >= sickBeds) {
                boolean found = false;
                Iterator iter = patients.iterator();
                while (iter.hasNext() && !found) {
                    HealthProblem problem = (HealthProblem)iter.next();

                    // Find the first rpoblem that is not recovering and curable
                    if (problem.getAwaitingTreatment()) {
                        Treatment required = problem.getIllness().getRecoveryTreatment();
                        if (required.getSelfAdminister()) {
                            problem.startTreatment(required.getDuration());
                            treatedPatients++;
                        }
                        found = true;
                    }
                }
            }
        }
        else {
            System.out.println("WARNING : Unexpected patient is stopTreatment " +
                                stopped);
        }
    }

    /**
     * Gets the owner entity of the sickbay.
     * @return owner entity
     */
    public Malfunctionable getOwner() {
        return owner; 
    }
}
