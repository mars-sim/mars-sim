/**
 * Mars Simulation Project
 * MedicalStation.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 * Based on Barry Evan's SickBay class
 */

package org.mars_sim.msp.core.person.medical;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class represents a medical station.
 * It provides a number of Treatments to Persons, these are defined in the
 * Medical.xml file.
 */
public class MedicalStation implements MedicalAid, Serializable {

    private int level;                              // Treatment level of the facility
    private int sickBeds;                           // Number of sick beds
    private List<HealthProblem> problemsBeingTreated; // List of health problems currently being treated.
    private List<HealthProblem> problemsAwaitingTreatment; // List of health problems awaiting treatment.
    private List<Treatment> supportedTreatments; // Treatments supported by the medical station.

    /** 
     * Constructor.
     *
     * @param level The treatment level of the medical station.
     * @param sickBeds Number of sickbeds. 
     */
    public MedicalStation(int level, int sickBeds) {
        this.level = level;
        this.sickBeds = sickBeds;
        problemsBeingTreated = new ArrayList<HealthProblem>();
        problemsAwaitingTreatment = new ArrayList<HealthProblem>();

        // Get all supported treatments.
        MedicalManager medManager = Simulation.instance().getMedicalManager();
        supportedTreatments = medManager.getSupportedTreatments(level);
    }

    /**
     * Gets the health problems awaiting treatment at the medical station.
     *
     * @return list of health problems
     */
    public List<HealthProblem> getProblemsAwaitingTreatment() {
        return new ArrayList<HealthProblem>(problemsAwaitingTreatment);
    }

    /**
     * Gets the health problems currently being treated at the medical station.
     *
     * @return list of health problems
     */
    public List<HealthProblem> getProblemsBeingTreated() {
        return new ArrayList<HealthProblem>(problemsBeingTreated);
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
    public Collection<Person> getPatients() {
        Collection<Person> result = new ConcurrentLinkedQueue<Person>();
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
    public List<Treatment> getSupportedTreatments() {
        return new ArrayList<Treatment>(supportedTreatments);
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
        	boolean degrading = problem.getDegrading();
        	
            // Check if treatment is supported in this medical station.
            Treatment requiredTreatment = problem.getIllness().getRecoveryTreatment();
            boolean supported = supportedTreatments.contains(requiredTreatment);
            
            // Check if problem is already being treated.
            boolean treating = problemsBeingTreated.contains(problem);
            
            // Check if problem is waiting to be treated.
            boolean waiting = problemsAwaitingTreatment.contains(problem);

            return supported && degrading && !treating && !waiting;
        }
    }
    
    /**
     * Add a health problem to the queue of problems awaiting treatment at this
     * medical station.
     *
     * @param problem The health problem to await treatment.
     * @throws Exception if health problem cannot be treated here.
     */
    public void requestTreatment(HealthProblem problem) {

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
        else throw new IllegalStateException("Health problem cannot be treated at this facility.");
    }

    /**
     * Starts the treatment of a health problem in the waiting queue.
     *
     * @param problem the health problem to start treating.
     * @param treatmentDuration the time required to perform the treatment.
     * @throws Exception if treatment cannot be started.
     */
    public void startTreatment(HealthProblem problem, double treatmentDuration) {
        
        if (problem == null) throw new IllegalArgumentException("problem is null");
        
        if (problemsAwaitingTreatment.contains(problem)) {
            problem.startTreatment(treatmentDuration);
            problemsBeingTreated.add(problem);
            problemsAwaitingTreatment.remove(problem);
        }
        else throw new IllegalStateException("Health problem not in medical station's waiting queue.");
    }

    /**
     * Stop a previously started treatment.
     *
     * @param problem Health problem stopping treatment on.
     * @throws Exception if health problem is not being treated.
     */
    public void stopTreatment(HealthProblem problem) {
        
        if (problemsBeingTreated.contains(problem)) {
            problem.stopTreatment();
            problemsBeingTreated.remove(problem);
            boolean cured = problem.getCured();
            boolean dead = problem.getSufferer().getPhysicalCondition().isDead();
            boolean recovering = problem.getRecovering();
            if (!cured && !dead && !recovering) problemsAwaitingTreatment.add(problem);
        }
        else throw new IllegalStateException("Health problem not currently being treated.");
    }
    
    /**
     * Gets the treatment level of the medical station.
     * @return treatment level
     */
    public int getTreatmentLevel() {
    	return level;
    }
}