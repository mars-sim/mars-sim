/**
 * Mars Simulation Project
 * MeteoriteImpactImpl.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.environment;

import org.mars_sim.msp.core.structure.building.BuildingManager;

import com.google.inject.ImplementedBy;

@ImplementedBy(MeteoriteImpactImpl.class)
public interface MeteoriteImpact {

	void calculateMeteoriteProbability(BuildingManager buildingManager);

}
