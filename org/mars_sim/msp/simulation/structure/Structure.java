/**
 * Mars Simulation Project
 * Structure.java
 * @version 2.74 2002-04-13
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.malfunction.*;

/** The Structure class is an abstract class that represents a 
 *  man-made structure such as a settlement, a transponder or 
 *  a supply cache.
 */
public abstract class Structure extends Unit implements Malfunctionable {

    // Data members
    protected MalfunctionManager malfunctionManager; // The structure's malfunction manager.
	
    /** Constructs a Structure object
     *  @param name the name of the unit
     *  @param location the unit's location
     *  @param mars the virtual Mars
     */
    Structure(String name, Coordinates location, Mars mars) {
        super(name, location, mars);

	// Initialize malfunction manager
	malfunctionManager = new MalfunctionManager(this, mars);
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
