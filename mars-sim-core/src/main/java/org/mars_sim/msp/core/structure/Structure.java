/**
 * Mars Simulation Project
 * Structure.java
 * @version 2.76 2004-06-01
 * @author Scott Davis
 */
 
package org.mars_sim.msp.core.structure;

import org.mars_sim.msp.core.*;
import org.mars_sim.msp.core.malfunction.*;

/** 
 * The Structure class is an abstract class that represents a 
 * man-made structure such as a settlement, a transponder or 
 * a supply cache.
 */
public abstract class Structure extends Unit implements Malfunctionable {

    // Data members
    protected MalfunctionManager malfunctionManager; // The structure's malfunction manager.
	
    /** 
     * Constructor
     * @param name the name of the unit
     * @param location the unit's location
     */
    Structure(String name, Coordinates location) {
        super(name, location);

		// Initialize malfunction manager
		malfunctionManager = new MalfunctionManager(this);
		malfunctionManager.addScopeString("Structure");
    }

    /** 
     * Gets the unit's malfunction manager.
     * @return malfunction manager
     */
    public MalfunctionManager getMalfunctionManager() {
        return malfunctionManager;
    }
}