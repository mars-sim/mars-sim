/**
 * Mars Simulation Project
 * MalfunctionManager.java
 * @version 2.74 2002-04-28
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.malfunction;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.medical.*;
import java.io.Serializable;
import java.util.*;

/** 
 * The MalfunctionManager class manages the current malfunctions in a unit.
 */
public class MalfunctionManager implements Serializable {

    private Malfunctionable entity; // The owning entity.
    private double timeSinceLastMaintenance; // Time passing (in millisols) since 
                                             // last maintenance on entity.
    private double maintenanceWorkTime; // The required work time for maintenance on entity.
    private double maintenanceTimeCompleted; // The completed 
    private Collection scope; // The scope strings of the unit.
    private Collection malfunctions; // The current malfunctions in the unit.
    private Mars mars; // The virtual Mars.

    /**
     * Constructs a MalfunctionManager object.
     */
    public MalfunctionManager(Malfunctionable entity, Mars mars) {

        // Initialize data members
	this.entity = entity;
	timeSinceLastMaintenance = 0D;
	this.mars = mars;
	scope = new ArrayList();
	malfunctions = new ArrayList();
	maintenanceWorkTime = 2000D;
    }

    /**
     * Add a unit scope string to the manager.
     * @param scopeString a unit scope string
     */
    public void addScopeString(String scopeString) {
        if ((scopeString != null) && !scope.contains(scopeString))
	    scope.add(scopeString);
    }

    /**
     * Checks if entity has a malfunction.
     * @return true if malfunction
     */
    public boolean hasMalfunction() {
        return (malfunctions.size() > 0);
    }
    
    /**
     * Gets an iterator for the unit's current malfunctions.
     * @return malfunction iterator
     */
    public Iterator getMalfunctions() {
        return malfunctions.iterator();
    }

    /**
     * Gets the most serious malfunction the entity has.
     * @return malfunction
     */
    public Malfunction getMostSeriousMalfunction() {
        Malfunction result = null;
	    
	// Check for any emergency malfunctions.
        Iterator i = malfunctions.iterator();
	while (i.hasNext()) {
	    Malfunction malfunction = (Malfunction) i.next();
	    if ((malfunction.getEmergencyWorkTime() - 
	            malfunction.getCompletedEmergencyWorkTime()) > 0D) result = malfunction;
	}

	// Otherwise get most serious malfunction.
	if (result == null) {
	    double highestSeverity = 0;
	    i = malfunctions.iterator();
	    while (i.hasNext()) {
	        Malfunction malfunction = (Malfunction) i.next();
		if (malfunction.getSeverity() > highestSeverity) {
		    highestSeverity = malfunction.getSeverity();
		    result = malfunction;
		}
	    }
	}

	return result;
    }

    /**
     * Adds a randomly selected malfunction to the unit (if possible).
     */
    private void addMalfunction() {
       MalfunctionFactory factory = mars.getMalfunctionFactory();
       Malfunction malfunction = factory.getMalfunction(scope);
       if (malfunction != null) {
           malfunctions.add(malfunction);
	   System.out.println(entity.getName() + " has new malfunction: " + malfunction.getName());
	   issueMedicalComplaints(malfunction);
       }
    }

    /**
     * Time passing while the unit is being actively used.
     * @param amount of time passing (in millisols)
     */
    public void activeTimePassing(double time) {

        timeSinceLastMaintenance += time;

	// Check for malfunction due to lack of maintenance.
        double chance = time * .000001D * timeSinceLastMaintenance;

	if (RandomUtil.lessThanRandPercent(chance)) {
	    System.out.println(entity.getName() + " has maintenance-triggered malfunction: " + timeSinceLastMaintenance);	
	    addMalfunction();
	}
    }

    /**
     * Called when the unit has an accident.
     */
    public void accident() {

        System.out.println(entity.getName() + " accident()");
	    
        // Multiple malfunctions may have occured.
	// 50% one malfunction, 25% two etc.
	boolean done = false;
	double chance = 100D;
	while (!done) {
            if (RandomUtil.lessThanRandPercent(chance)) {
	        addMalfunction();
		chance /= 2D;
	    }
	    else done = true;
	}
    }

    /**
     * Gets the time since last maintenance on entity.
     * @return time (in millisols)
     */
    public double getTimeSinceLastMaintenance() {
        return timeSinceLastMaintenance;
    }

    /**
     * Gets the required work time for maintenance for the entity.
     * @return time (in millisols)
     */
    public double getMaintenanceWorkTime() {
        return maintenanceWorkTime;
    }

    /**
     * Sets the required work time for maintenance for the entity.
     * @param maintenanceWorkTime (in millisols)
     */
    public void setMaintenanceWorkTime(double maintenanceWorkTime) {
        this.maintenanceWorkTime = maintenanceWorkTime;
    }

    /**
     * Gets the work time completed on maintenance.
     * @return time (in millisols)
     */
    public double getMaintenanceWorkTimeCompleted() {
        return maintenanceTimeCompleted;
    }

    /**
     * Add work time to maintenance.
     * @param time (in millisols)
     */
    public void addMaintenanceWorkTime(double time) {
        maintenanceTimeCompleted += time;
	if (maintenanceTimeCompleted >= maintenanceWorkTime) {
            maintenanceTimeCompleted = 0D;
	    timeSinceLastMaintenance = 0D;
	}
    }

    /**
     * Issues any necessary medical complaints.
     * @param malfunction the new malfunction
     */
    public void issueMedicalComplaints(Malfunction malfunction) {
       
	// Get people who can be affected by this malfunction.
        PersonCollection people = entity.getAffectedPeople();


	// Determine medical complaints for each malfunction.
	Iterator i1 = malfunction.getMedicalComplaints().keySet().iterator();
	while (i1.hasNext()) {
	    String complaintName = (String) i1.next();
	    double probability = ((Double) malfunction.getMedicalComplaints().get(complaintName)).doubleValue();
	    MedicalManager medic = mars.getMedicalManager();
            Complaint complaint = medic.getComplaintByName(complaintName);
            if (complaint != null) {
	        PersonIterator i2 = people.iterator();
	        while (i2.hasNext()) {
	            Person person = i2.next();
		    if (RandomUtil.lessThanRandPercent(probability)) 
                        person.getPhysicalCondition().addMedicalComplaint(complaint);
		}
	    }
	}
    }
}
