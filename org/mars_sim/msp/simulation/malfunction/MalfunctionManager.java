/**
 * Mars Simulation Project
 * MalfunctionManager.java
 * @version 2.74 2002-04-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.malfunction;

import org.mars_sim.msp.simulation.*;
import java.io.Serializable;
import java.util.*;

/** 
 * The MalfunctionManager class manages the current malfunctions in a unit.
 */
public class MalfunctionManager implements Serializable {

    private Malfunctionable entity; // The owning entity.
    private double timeSinceLastMaintenance; // Time passing (in millisols) since 
                                             // last maintenance on entity.
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
     * Adds a randomly selected malfunction to the unit (if possible).
     */
    private void addMalfunction() {
       MalfunctionFactory factory = mars.getMalfunctionFactory();
       Malfunction malfunction = factory.getMalfunction(scope);
       if (malfunction != null) {
           malfunctions.add(malfunction);
	   System.out.println(entity.getName() + " has new malfunction: " + malfunction.getName());
       }
    }

    /**
     * Time passing while the unit is being actively used.
     * @param amount of time passing (in millisols)
     */
    public void activeTimePassing(double time) {

        timeSinceLastMaintenance += time;

        // double chance = .01D + (.0001D * timeSinceLastMaintenance);
        double chance1 = .01D + (.00001D * timeSinceLastMaintenance);
	// chance *= time;
	double chance2 = chance1 * time;

	if (RandomUtil.lessThanRandPercent(chance2)) addMalfunction();
    }

    /**
     * Called when the unit has an accident.
     */
    public void accident() {

        System.out.println(entity.getName() + " accident()");
	    
        // Multiple malfunctions may have occured.
	// 50% one malfunction, 25% two etc.
	boolean done = false;
	double chance = 50D;
	while (!done) {
            if (RandomUtil.lessThanRandPercent(chance)) {
	        addMalfunction();
		chance /= 2D;
	    }
	    else done = true;
	}
    }
}
