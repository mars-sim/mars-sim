/*
 * Mars Simulation Project
 * OuterSpace.java
 * @date 2023-06-05
 * @author Manny Kung
 */

package org.mars_sim.msp.core.environment;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.location.LocationStateType;

/**
 * OuterSpace is the object unit that represents the outer space
 */
public class OuterSpace extends PlanetaryEntity {

	/** default serial id. */
	private static final long serialVersionUID = 123L;

	private static final String NAME = "Outer Space";

	public OuterSpace() {
		super(NAME, Unit.OUTER_SPACE_UNIT_ID, Unit.OUTER_SPACE_UNIT_ID, null,
				UnitType.OUTER_SPACE);
		currentStateType = LocationStateType.OUTER_SPACE;

	}
}
