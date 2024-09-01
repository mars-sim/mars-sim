/*
 * Mars Simulation Project
 * Structure.java
 * @date 2023-05-09
 * @author Scott Davis
 */

package com.mars_sim.core.structure;

import com.mars_sim.core.Unit;
import com.mars_sim.core.map.location.Coordinates;

/**
 * The Structure class is an abstract class that represents a
 * man-made structure such as a settlement, a building, or a construction site.
 */
public abstract class Structure extends Unit {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	private transient String childContext;

	/**
	 * Constructor.
	 * 
	 * @param name the name of the unit
	 * @param location the unit's location
	 */
	protected Structure(String name, Coordinates location) {
		super(name, location);
	}

	/**
	 * The context of a building is always the parent Settlement
	 */
	@Override
	public String getContext() {
		return getSettlement().getName();
	}

	/**
	 * What is the context for any child entities.
	 * @return Combination of Settlement and Structure name.
	 */
	public String getChildContext() {
		if (childContext == null) {
			childContext = getContext() + ENTITY_SEPERATOR + getName();
		}
		return childContext;
	}
}
