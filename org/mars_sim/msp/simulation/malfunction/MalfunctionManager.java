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

    private Unit unit; // The owning unit.
    private Collection scope; // The scope strings of the unit.
    private Collection malfunctions; // The current malfunctions in the unit.

    /**
     * Constructs a MalfunctionManager object.
     */
    public MalfunctionManager(Unit unit) {
        // Initialize data members
	this.unit = unit;
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
     * Gets an iterator for the unit's current malfunctions.
     * @return malfunction iterator
     */
    public Iterator getMalfunctions() {
        return malfunctions.iterator();
    }

    /**
     * Adds a randomly selected malfunction to the unit (if possible).
     */
    public void addMalfunction() {
       MalfunctionFactory factory = unit.getMars().getMalfunctionFactory();
       Malfunction malfunction = factory.getMalfunction(scope);
       if (malfunction != null) malfunctions.add(malfunction);
    }
}
