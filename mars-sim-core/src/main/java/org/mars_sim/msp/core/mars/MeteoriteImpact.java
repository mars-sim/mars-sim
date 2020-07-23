/**
 * Mars Simulation Project
 * MeteoriteImpactImpl.java
 * @version 3.1.1 2020-07-22
 * @author Manny Kung
 */

package org.mars_sim.msp.core.mars;

import org.mars_sim.msp.core.structure.building.BuildingManager;

import com.google.inject.ImplementedBy;

@ImplementedBy(MeteoriteImpactImpl.class)
public interface MeteoriteImpact {

	void calculateMeteoriteProbability(BuildingManager buildingManager);

}
