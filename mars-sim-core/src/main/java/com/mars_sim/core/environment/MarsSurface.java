/*
 * Mars Simulation Project
 * MarsSurface.java
 * @date 2023-06-05
 * @author Manny Kung
 */

package com.mars_sim.core.environment;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.map.location.Coordinates;
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

	/**
	 * Gets the time offset for a given point on the surface.
	 * 
	 * @param point
	 * @return
	 */
	public static int getTimeOffset(Coordinates point) {
		// Get the rotation about the planet and convert that to a fraction of the Sol.
		double fraction = point.getTheta()/(Math.PI * 2D); 
		if (fraction >= 1D) {
			// Gone round the planet
			fraction = 0D;
		}
		return (int) (1000 * fraction);
	}
}
