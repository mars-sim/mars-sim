/*
 * Mars Simulation Project
 * DustStormType.java
natural attributes.
 * @author Manny Kung
 */

package com.mars_sim.core.environment;

import com.mars_sim.core.tool.Msg;

public enum DustStormType {

	// The demarcation between local and regional storms has been placed at a scale
	// of 2000 km, or about 40 degrees of longitude (for the long axis of the storm),
	// while planet-encircling dust storm has a major axis that completely encircles
	// either one or both hemispheres of Mars.
	// source: http://www.alpo-astronomy.org/jbeish/Sand_Ships_of_Mars.htm

	// Local storms then include all dust activity at smaller scales, extending down to
	// the dust devils observed	by Viking and Pathfinder

	// Order in terms of size
	LOCAL, DUST_DEVIL, REGIONAL, PLANET_ENCIRCLING;

	private String name;

	/** hidden constructor. */
	private DustStormType() {
        this.name = Msg.getStringOptional("DustStormType", name());
	}

	public final String getName() {
		return this.name;
	}
}
