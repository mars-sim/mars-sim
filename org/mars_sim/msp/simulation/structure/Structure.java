/**
 * Mars Simulation Project
 * Structure.java
 * @version 2.74 2002-01-13
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure;

import org.mars_sim.msp.simulation.*;

/** The Structure class is an abstract class that represents a 
 *  man-made structure such as a settlement, a transponder or 
 *  a supply cache.
 */
public abstract class Structure extends Unit {
    
    /** Constructs a Structure object
     *  @param name the name of the unit
     *  @param location the unit's location
     *  @param mars the virtual Mars
     */
    Structure(String name, Coordinates location, VirtualMars mars) {
        super(name, location, mars);
    }
}
