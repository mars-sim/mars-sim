/**
 * Mars Simulation Project
 * Structure.java
 * @version 2.71 2000-11-13
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation;

/** The Structure class is an abstract class that represents a 
 *  man-made structure such as a settlement, a transponder or 
 *  a supply cache.
 */
public abstract class Structure extends Unit {
    
    /** Constructs a Structure object
     *  @param name the name of the unit
     *  @param location the unit's location
     *  @param mars the virtual Mars
     *  @param manager the unit's unit manager
     */
    Structure(String name, Coordinates location, VirtualMars mars, UnitManager manager) {
        super(name, location, mars, manager);
    }
}
