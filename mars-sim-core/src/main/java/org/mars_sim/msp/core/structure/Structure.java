/**
 * Mars Simulation Project
 * Structure.java
 * @version 2.90 2010-01-20
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