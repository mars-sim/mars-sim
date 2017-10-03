/**
 * Mars Simulation Project
 * DustStormType.java
 * @version 3.1.0 2017-10-03
 * @author Manny Kung
 */

package org.mars_sim.msp.core.mars;

import org.mars_sim.msp.core.Msg;

public enum DustStormType {

	// The demarcation between local and regional storms has been placed at a scale
	// of 2000 km, or about 40 degrees of longitude (for the long axis of the storm),
	// while planet-encircling dust storm has a major axis that completely encircles
	// either one or both hemispheres of Mars.
	// source: http://www.alpo-astronomy.org/jbeish/Sand_Ships_of_Mars.htm

	// Local storms then include all dust activity at smaller scales, extending down to
	// the dust devils observed	by Viking and Pathfinder


	DUST_DEVIL							(Msg.getString("DustStormType.dustDevil")), //$NON-NLS-1$
	LOCAL								(Msg.getString("DustStormType.localStorm")), //$NON-NLS-1$
	REGIONAL							(Msg.getString("DustStormType.regionalStorm")), //$NON-NLS-1$
	PLANET_ENCIRCLING					(Msg.getString("DustStormType.planetEncirclingStorm")), //$NON-NLS-1$
	;


	private String name;

	/** hidden constructor. */
	private DustStormType(String name) {
		this.name = name;
	}

	public final String getName() {
		return this.name;
	}

	@Override
	public final String toString() {
		return getName();
	}
}
