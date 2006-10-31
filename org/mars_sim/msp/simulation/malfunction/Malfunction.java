/**
 * Mars Simulation Project
 * Malfunction.java
 * @version 2.75 2003-01-20
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.malfunction;

import java.io.Serializable;
import java.util.*;

/** 
 * The Malfunction class represents a
 * malfunction in a vehicle, structure or equipment.
 */
public class Malfunction implements Serializable {

    // Data members
    private String name; 
    private int severity;
    private double probability;
    private Collection scope;
    private Map resourceEffects;
    private Map lifeSupportEffects;
    private Map medicalComplaints;

    // Work time tracking
    private double workTime;
    private double workTimeCompleted;
    private double emergencyWorkTime;
    private double emergencyWorkTimeCompleted;
    private double EVAWorkTime;
    private double EVAWorkTimeCompleted;
    
    /** 
     * Constructs a Malfunction object
     * @param name name of the malfunction 
     */
    public Malfunction(String name, int severity, double probability, double emergencyWorkTime, 
		       double workTime, double EVAWorkTime, Collection scope, Map resourceEffects, 
		       Map lifeSupportEffects, Map medicalComplaints) {

        // Initialize data members
        this.name = name;
        this.severity = severity;
        this.probability = probability;
        this.emergencyWorkTime = emergencyWorkTime;
        this.workTime = workTime;
        this.EVAWorkTime = EVAWorkTime;
        this.scope = scope;
        this.resourceEffects = resourceEffects;
        this.lifeSupportEffects = lifeSupportEffects;
        this.medicalComplaints = medicalComplaints;

        workTimeCompleted = 0D;
        emergencyWorkTimeCompleted = 0D;
        EVAWorkTimeCompleted = 0D;
    }

    /**
     * Returns the name of the malfunction. 
     * @return name of the malfunction 
     */
    public String getName() {
        return name;
    }

    /**
     * Returns true if malfunction is fixed. 
     * @return true if malfunction is fixed
     */
    public boolean isFixed() {
        boolean result = true;

        if (workTimeCompleted < workTime) result = false;
        if (emergencyWorkTimeCompleted < emergencyWorkTime) result = false;
        if (EVAWorkTimeCompleted < EVAWorkTime) result = false;
	
        return result;
    }

    /** 
     * Returns the severity level of the malfunction.
     * @return severity of malfunction (1 - 100)
     */
    public int getSeverity() {
        return severity;
    }

    /**
     * Returns the probability of this malfunction occuring.
     * @return probability
     */
    public double getProbability() {
        return probability;
    }
    
    /**
     * Returns the work time required to repair the malfunction.
     * @return work time (in millisols)
     */
    public double getWorkTime() {
        return workTime;
    }
   
    /**
     * Returns the completed work time.
     * @return completed work time (in millisols)
     */
    public double getCompletedWorkTime() {
        return workTimeCompleted; 
    }

    /** 
     * Adds work time to the malfunction. 
     * @param time work time (in millisols)
     * @return remaining work time not used (in millisols)
     */
    public double addWorkTime(double time) {
        workTimeCompleted += time;
        if (workTimeCompleted >= workTime) {
            double remaining = workTimeCompleted - workTime;
            workTimeCompleted = workTime;
            return remaining;
        }
        return 0D;
    }

    /**
     * Returns the emergency work time required to repair the malfunction.
     * @return emergency work time (in millisols)
     */
    public double getEmergencyWorkTime() {
        return emergencyWorkTime;
    }
   
    /**
     * Returns the completed emergency work time.
     * @return completed emergency work time (in millisols)
     */
    public double getCompletedEmergencyWorkTime() {
        return emergencyWorkTimeCompleted; 
    }

    /** 
     * Adds emergency work time to the malfunction. 
     * @param time emergency work time (in millisols)
     * @return remaining work time not used (in millisols)
     */
    public double addEmergencyWorkTime(double time) {
        emergencyWorkTimeCompleted += time;
        if (emergencyWorkTimeCompleted >= emergencyWorkTime) {
            double remaining = emergencyWorkTimeCompleted - emergencyWorkTime;
            emergencyWorkTimeCompleted = emergencyWorkTime;
            // System.out.println(name + "@" + Integer.toHexString(hashCode()) + " emergency fixed.");
            return remaining;
        }
        return 0D;
    }
    
    /**
     * Returns the EVA work time required to repair the malfunction.
     * @return EVA work time (in millisols)
     */
    public double getEVAWorkTime() {
        return EVAWorkTime;
    }
   
    /**
     * Returns the completed EVA work time.
     * @return completed EVA work time (in millisols)
     */
    public double getCompletedEVAWorkTime() {
        return EVAWorkTimeCompleted; 
    }

    /** 
     * Adds EVA work time to the malfunction. 
     * @param time EVA work time (in millisols)
     * @return remaining work time not used (in millisols)
     */
    public double addEVAWorkTime(double time) {
        EVAWorkTimeCompleted += time;
        if (EVAWorkTimeCompleted >= EVAWorkTime) {
            double remaining = EVAWorkTimeCompleted - EVAWorkTime;
            EVAWorkTimeCompleted = EVAWorkTime;
            return remaining;
        }
        return 0D;
    }

    /**
     * Checks if a unit's scope strings have any matches
     * with the malfunction's scope strings.
     * @return true if any matches
     */
    public boolean unitScopeMatch(Collection unitScope) {
        boolean result = false;

        if ((scope.size() > 0) && (unitScope.size() > 0)) {
            Iterator i1 = scope.iterator();
            while (i1.hasNext()) {
                String scopeString = (String) i1.next();
                Iterator i2 = unitScope.iterator();
                while (i2.hasNext()) {
                    String unitScopeString = (String) i2.next();
            	    if (scopeString.equals(unitScopeString)) result = true;
                }
            }
        }

        return result;
    }

    /**
     * Gets the resource effects of the malfunction.
     * @return resource effects as name-value pairs in Map
     */
    public Map getResourceEffects() {
        return resourceEffects;
    }

    /**
     * Gets the life support effects of the malfunction.
     * @return life support effects as name-value pairs in Map
     */
    public Map getLifeSupportEffects() {
        return lifeSupportEffects;
    }

    /**
     * Gets the medical complaints produced by this malfunction
     * and their probability of occuring.
     * @return medical complaints as name-value pairs in Map
     */
    public Map getMedicalComplaints() {
        return medicalComplaints;
    }

    /**
     * Gets a clone of this malfunction.
     * @return clone of this malfunction
     */
    public Malfunction getClone() {
        Malfunction clone = new Malfunction(name, severity, probability, emergencyWorkTime,
            workTime, EVAWorkTime, scope, resourceEffects, lifeSupportEffects, medicalComplaints);

        // if (emergencyWorkTime > 0D) System.out.println(name + "@" + Integer.toHexString(clone.hashCode()) + " emergency starts");
	
        return clone;
    }
}
