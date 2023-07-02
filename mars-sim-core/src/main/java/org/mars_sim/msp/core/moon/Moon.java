/*
 * Mars Simulation Project
 * Moon.java
 * @date 2023-06-05
 * @author Manny Kung
 */

package org.mars_sim.msp.core.moon;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.environment.PlanetaryEntity;
import org.mars_sim.msp.core.location.LocationStateType;

/**
 * Moon is the object unit that represents Earth's Moon
 */
public class Moon extends PlanetaryEntity {

	/** default serial id. */
	private static final long serialVersionUID = 123L;

	private static final String NAME = "Moon";


	public Moon(Unit outerSpace) {
		super(NAME, Unit.MOON_UNIT_ID, outerSpace.getIdentifier(), outerSpace, UnitType.MOON);
	
		// Set currentStateType
		currentStateType = LocationStateType.MOON;
	}
}
