/**
 * Mars Simulation Project
 * Structure.java
 * @version 3.1.0 2017-11-04
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Unit;

/**
 * The Structure class is an abstract class that represents a
 * man-made structure such as a settlement, a building, a transponder or
 * a supply cache.
 */
//@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = As.PROPERTY, property = "@class")
//@JsonSubTypes({ @Type(value = Settlement.class, name = "settlement"),
//				@Type(value = Building.class, name = "building"),
//				@Type(value = ConstructionSite.class, name = "constructionSite")})
public abstract class Structure extends Unit {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param name the name of the unit
	 * @param location the unit's location
	 */
	public Structure(String name, Coordinates location) {
		super(name, location);
	}
}