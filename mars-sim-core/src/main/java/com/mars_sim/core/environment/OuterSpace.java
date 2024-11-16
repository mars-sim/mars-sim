/*
 * Mars Simulation Project
 * OuterSpace.java
 * @date 2023-06-05
 * @author Manny Kung
 */

package com.mars_sim.core.environment;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitType;

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
	}
}
