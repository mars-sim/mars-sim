/**
 * Mars Simulation Project
 * MalfunctionManager.java
 * @version 2.74 2002-04-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.io.Serializable;
import java.util.*;

/** 
 * The MalfunctionManager class manages the current malfunctions in a unit.
 */
public class MalfunctionManager implements Serializable {

    private Collection scope; // The scope strings of the unit.
    private Collection malfunctions; // The current malfunctions in the unit.

    /**
     * Constructs a MalfunctionManager object.
     */
    public MalfunctionManager() {
        // Initialize data members
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


}
