/**
 * Mars Simulation Project
 * BuildingConnection.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.building.function;

import java.util.Iterator;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.config.BuildingSpec;
import com.mars_sim.core.building.config.FunctionSpec;
import com.mars_sim.core.structure.Settlement;

public class BuildingConnection extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	int baseLevel = 0;
	
	/** constructor. */
	public BuildingConnection(Building building, FunctionSpec spec) {
		// User Function constructor.
		super(FunctionType.CONNECTION, spec, building);
		
		baseLevel = building.getBaseLevel();
	}

	/**
	 * Gets the value of the function for a named building.
	 * 
	 * @param buildingName the building name.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function.
	 */
	public static double getFunctionValue(String buildingName, boolean newBuilding, Settlement settlement) {
	
		int numAdjBuildings = 0;
		
		double demand = 0;
		
		// Determine building base level.
		// Should only determine supply and demand of connectors with same base level.
		BuildingSpec spec = buildingConfig.getBuildingSpec(buildingName);
		int baseLevel = spec.getBaseLevel();

		Iterator<Building> j = settlement.getBuildingManager().getBuildingSet(FunctionType.CONNECTION)
				.iterator();
		while (j.hasNext()) {
			Building building = j.next();
			// Only add supply from connector buildings with same base level as this one.
			if (building.getBaseLevel() == baseLevel) {
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
	
				demand = demand + wearModifier ;
				numAdjBuildings += settlement.getBuildingManager().getAdjacentBuildings(building).size();
			}
		}

		double supply = numAdjBuildings * 2;

		demand = settlement.getPopulationFactor0() + Math.sqrt(demand);
		
		return demand / (supply + 1D);
	}
	
	/**
	 * Gets the value of this function.
	 * 
	 * @return value (VP) of building function.
	 */
	public double getFunctionValue() {

		int numBuildings = 0;

		int numAdjBuildings = 0;
		
		Iterator<Building> i = getBuilding().getBuildingManager().getBuildingSet(FunctionType.CONNECTION).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			// Only add demand from buildings with same base level as this one.
			if (building.getBaseLevel() == baseLevel) {
				numBuildings++;
			}
		}

		numAdjBuildings = getBuilding().getBuildingManager().getAdjacentBuildings(building).size();
		
		double supply = numAdjBuildings * 2;

		double demand = getBuilding().getSettlement().getPopulationFactor0() + Math.sqrt(numBuildings);

		return demand / (supply + 1D);
	}
	
}
