/**
 * Mars Simulation Project
 * MedicalStation.java
 * @version 2.75 2003-06-19
 * @author Scott Davis
 * Based on Barry Evan's SickBay class
 */

package org.mars_sim.msp.simulation.person.medical;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.person.*;

/**
 * This class represents the abstract notiation of a medical station.
 * It provides a number of Treatments to Persons, these are defined in the
 * Medical.xml file.
 *
 * @see org.mars_sim.msp.simulation.structure.building.function.MedicalCare
 * @see org.mars_sim.msp.simulation.equipment.MobileSickBay
 */
public abstract class MedicalStation implements MedicalAid, Serializable {

    private int level;                              // Treatment level of the facility
    private int sickBeds;                           // Number of sick beds
    private List problemsBeingTreated;              // List of health problems currently being treated.
    private List problemsAwaitingTreatment;         // List of health problems awaiting treatment.
    private List supportedTreatments;               // Treatments supported by the medical station.

    /** 
     * Constructor.
     *
     * @param level The treatment level of the medical station.
     * @param sickBeds Number of sickbeds. 
     * @param manager The medical manager for Mars.
     */
    public MedicalStation(int level, int sickBeds, MedicalManager manager) {
        this.level = level;
        this.sickBeds = sickBeds;
        problemsBeingTreated = new ArrayList();
        problemsAwaitingTreatment = new ArrayList();

        // Get all supported treatments.
        supportedTreatments = manager.getSupportedTreatments(level);
    }

    /**
     * Gets the health problems awaiting treatment at the medical station.
     *
     * @return list of health problems
     */
    public List getProblemsAwaitingTreatment() {
        return new ArrayList(problemsAwaitingTreatment);
    }

    /**
     * Gets the health problems currently being treated at the medical station.
     *
     * @return list of health problems
     */
    public List getProblemsBeingTreated() {
        return new ArrayList(problemsBeingTreated);
    }

    /**
     * Gets the number of sick beds.
     *
     * @return Sick bed count.
     */
    public int getSickBedNum() {
        return sickBeds;
    }

    /**
     * Gets the current number of people being treated here.
     *
     * @return Patient count.
     */
    public int getPatientNum() {
        return getPatients().size();
    }
    
    /**
     * Gets the patients at this medical station.
     * @return Collection of People.
     */
    public PersonCollection getPatients() {
        PersonCollection result = new PersonCollection();
        Iterator i = problemsBeingTreated.iterator();
        while (i.hasNext()) {
            Person patient = ((HealthProblem) i.next()).getSufferer();
            if (!result.contains(patient)) result.add(patient);
        }
        
        return result;
    }

    /**
     * Get a list of supported Treatments at this SickBay.
     */
    public List getSupportedTreatments() {
        return new ArrayList(supportedTreatments);
    }

    /**
     * Checks if a health problem can be treated at this medical station.
     *
     * @param problem The health problem to check treatment.
     * @return true if problem can be treated.
     */
    public boolean canTreatProblem(HealthProblem problem) {
        if (problem == null) return false;
        else {
            // Check if treatment is supported in this medical station.
            Treatment requiredTreatment = problem.getIllness().getRecoveryTreatment();
            boolean supported = supportedTreatments.contains(requiredTreatment);
            
            // Check if problem is already being treated.
            boolean treating = problemsBeingTreated.contains(problem);
            
            // Check if problem is waiting to be treated.
            boolean waiting = problemsAwaitingTreatment.contains(problem);
            
            if (supported && !treating && !waiting) return true;
            else return false;
        }
    }
    
    /**
     * Add a health problem to the queue of problems awaiting treatment at this
     * medical station.
     *
     * @param problem The health problem to await treatment.
     * @throws Exception if health problem cannot be treated here.
     */
    public void requestTreatment(HealthProblem problem) throws Exception {

        if (problem == null) throw new IllegalArgumentException("problem is null");

        // Check if treatment is supported here.
        if (canTreatProblem(problem)) {

            // If the patient can administer the treatment themselves then
            // start the treatment.
            Treatment treatment = problem.getIllness().getRecoveryTreatment();
            if (treatment.getSelfAdminister() && (getPatientNum() < sickBeds)) {
                problem.startTreatment(treatment.getDuration());
                problemsBeingTreated.add(problem);
            }
            else {
                // Otherwise add the problem to the waiting queue.
                problemsAwaitingTreatment.add(problem);
            }
        }
        else throw new Exception("Health problem cannot be treated at this facility.");
    }

    /**
     * Starts the treatment of a health problem in the waiting queue.
     *
     * @param problem the health problem to start treating.
     * @param treatmentDuration the time required to perform the treatment.
     * @throws Exception if treatment cannot be started.
     */
    public void startTreatment(HealthProblem problem, double treatmentDuration) throws Exception {
        
        if (problem == null) throw new IllegalArgumentException("problem is null");
        
        if (problemsAwaitingTreatment.contains(problem)) {
            problem.startTreatment(treatmentDuration);
            problemsBeingTreated.add(problem);
            problemsAwaitingTreatment.remove(problem);
        }
        else throw new Exception("Health problem not in medical station's waiting queue.");
    }

    /**
     * Stop a previously started treatment.
     *
     * @param problem Health problem stopping treatment on.
     * @throws Exception if health problem is not being treated.
     */
    public void stopTreatment(HealthProblem problem) throws Exception {
        
        if (problemsBeingTreated.contains(problem)) {
            problem.stopTreatment();
            problemsBeingTreated.remove(problem);
            boolean cured = problem.getCured();
            boolean dead = problem.getSufferer().getPhysicalCondition().isDead();
            if (!cured && !dead) problemsAwaitingTreatment.add(problem);
        }
        else throw new Exception("Health problem not currently being treated.");
    }
}
