/**
 * Mars Simulation Project
 * Equipment.java
 * @version 2.74 2002-04-17
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.equipment;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.malfunction.*;

/** The Equipment class is an abstract class that represents  
 *  a useful piece of equipment, such as a EVA suite or a
 *  medpack.
 */
public abstract class Equipment extends Unit implements Malfunctionable {
   
    // Data members
    protected MalfunctionManager malfunctionManager; // The equipment's malfunction manager
	
    /** Constructs an Equipment object
     *  @param name the name of the unit
     *  @param location the unit's location
     *  @param mars the virtual Mars
     */
    Equipment(String name, Coordinates location, Mars mars) {
        super(name, location, mars);

	// Initialize malfunction manager.
	malfunctionManager = new MalfunctionManager(this, mars);
	malfunctionManager.addScopeString("Equipment");
    }

    /**
     * Gets the unit's malfunction manager.
     * @return malfunction manager
     */
    public MalfunctionManager getMalfunctionManager() {
        return malfunctionManager;
    }
}
