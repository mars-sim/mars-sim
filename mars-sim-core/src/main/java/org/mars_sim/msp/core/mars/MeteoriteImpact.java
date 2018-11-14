/**
 * Mars Simulation Project
 * MeteoriteImpactImpl.java
 * @version 3.1.0 2017-10-03
 * @author Manny Kung
 */

package org.mars_sim.msp.core.mars;

import org.mars_sim.msp.core.structure.building.BuildingManager;

import com.google.inject.ImplementedBy;

@ImplementedBy(MeteoriteImpactImpl.class)
public interface MeteoriteImpact {

	void calculateMeteoriteProbability(BuildingManager buildingManager);

}
