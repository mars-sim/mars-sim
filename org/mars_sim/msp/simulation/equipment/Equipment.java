/**
 * Mars Simulation Project
 * Equipment.java
 * @version 2.74 2002-01-13
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.equipment;

import org.mars_sim.msp.simulation.*;

/** The Equipment class is an abstract class that represents  
 *  a useful piece of equipment, such as a EVA suite or a
 *  medpack.
 */
public abstract class Equipment extends Unit {
    
    /** Constructs a Structure object
     *  @param name the name of the unit
     *  @param location the unit's location
     *  @param mars the virtual Mars
     */
    Equipment(String name, Coordinates location, VirtualMars mars) {
        super(name, location, mars);
    }
}
