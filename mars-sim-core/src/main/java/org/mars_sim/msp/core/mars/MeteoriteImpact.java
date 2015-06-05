/**
 * Mars Simulation Project
 * MeteoriteImpact.java
 * @version 3.08 2015-06-04
 * @author Manny Kung
 */

package org.mars_sim.msp.core.mars;

import org.mars_sim.msp.core.structure.building.BuildingManager;

import com.google.inject.ImplementedBy;

@ImplementedBy(MeteoriteImpactImpl.class)
public interface MeteoriteImpact  {

	void calculateMeteoriteProbability(BuildingManager buildingManager);

}

