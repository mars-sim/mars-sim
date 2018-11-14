/**
 * Mars Simulation Project
 * Meteorite.java
 * @version 3.1.0 2017-10-03
 * @author Manny Kung
 */

package org.mars_sim.msp.core.mars;

import java.io.Serializable;

import org.mars_sim.msp.core.structure.building.BuildingManager;

import com.google.inject.Inject;

public class Meteorite implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private final MeteoriteImpact meteoriteImpact;

	@Inject
	public Meteorite(MeteoriteImpact meteoriteImpact) {
		this.meteoriteImpact = meteoriteImpact;
	}

	public void startMeteoriteImpact(BuildingManager buildingManager) {
		meteoriteImpact.calculateMeteoriteProbability(buildingManager);
	}

}
