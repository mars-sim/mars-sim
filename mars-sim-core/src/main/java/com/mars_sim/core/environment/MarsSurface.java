/*
 * Mars Simulation Project
 * MarsSurface.java
 * @date 2023-06-05
 * @author Manny Kung
 */

package com.mars_sim.core.environment;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.unit.UnitHolder;

/**
 * MarsSurface is the object unit that represents the surface of Mars
 */
public class MarsSurface extends PlanetaryEntity
	implements UnitHolder {

	/** default serial id. */
	private static final long serialVersionUID = 123L;

	private static final String NAME = "Mars Surface";

	public MarsSurface() {
		super(NAME, Unit.MARS_SURFACE_UNIT_ID, UnitType.MARS);
	}
}
