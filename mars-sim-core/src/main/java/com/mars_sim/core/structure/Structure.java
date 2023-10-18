/*
 * Mars Simulation Project
 * Structure.java
 * @date 2023-05-09
 * @author Scott Davis
 */

package com.mars_sim.core.structure;

import com.mars_sim.core.Unit;
import com.mars_sim.mapdata.location.Coordinates;

/**
 * The Structure class is an abstract class that represents a
 * man-made structure such as a settlement, a building, or a construction site.
 */
public abstract class Structure extends Unit {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param name the name of the unit
	 * @param location the unit's location
	 */
	public Structure(String name, Coordinates location) {
		super(name, location);
	}
}
