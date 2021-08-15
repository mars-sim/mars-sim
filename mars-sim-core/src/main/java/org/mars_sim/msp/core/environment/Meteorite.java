/**
 * Mars Simulation Project
 * Meteorite.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.environment;

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
