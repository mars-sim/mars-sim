/**
 * Mars Simulation Project
 * Structure.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */
 
package org.mars_sim.msp.core.structure;

import org.mars_sim.msp.core.*;

/** 
 * The Structure class is an abstract class that represents a 
 * man-made structure such as a settlement, a transponder or 
 * a supply cache.
 */
public abstract class Structure extends Unit {
	
    /** 
     * Constructor
     * @param name the name of the unit
     * @param location the unit's location
     */
    Structure(String name, Coordinates location) {
        super(name, location);
    }
}