/*
 * Mars Simulation Project
 * Moon.java
 * @date 2023-06-05
 * @author Manny Kung
 */

package com.mars_sim.core.moon;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.environment.PlanetaryEntity;

/**
 * Moon is the object unit that represents Earth's Moon.
 */
public class Moon extends PlanetaryEntity {

	/** default serial id. */
	private static final long serialVersionUID = 123L;

	private static final String NAME = "Moon";


	public Moon(Unit outerSpace) {
		super(NAME, Unit.MOON_UNIT_ID, outerSpace.getIdentifier(), outerSpace, UnitType.MOON);
	}
}
