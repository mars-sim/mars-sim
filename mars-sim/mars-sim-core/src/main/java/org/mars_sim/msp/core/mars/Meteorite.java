/**
 * Mars Simulation Project
 * Meteorite.java
 * @version 3.08 2015-06-04
 * @author Manny Kung
 */

package org.mars_sim.msp.core.mars;

import org.mars_sim.msp.core.structure.building.BuildingManager;

import com.google.inject.Inject;

public class Meteorite {

	private final MeteoriteImpact meteoriteImpact;

	@Inject
	public Meteorite(MeteoriteImpact meteoriteImpact) {
		this.meteoriteImpact = meteoriteImpact;
	}

	public void startMeteoriteImpact(BuildingManager buildingManager) {
		//System.out.println("startMeteoriteImpact() calling calculateMeteoriteProbability() next");
		//System.out.println("meteoriteImpact : "+ meteoriteImpact);
		meteoriteImpact.calculateMeteoriteProbability(buildingManager);
	}

}
