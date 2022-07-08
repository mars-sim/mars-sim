/**
 * Mars Simulation Project
 * BuildingConnection.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.util.Iterator;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingSpec;
import org.mars_sim.msp.core.structure.building.FunctionSpec;

public class BuildingConnection extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;


	/** constructor. */
	public BuildingConnection(Building building, FunctionSpec spec) {
		// User Function constructor.
		super(FunctionType.BUILDING_CONNECTION, spec, building);
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

		// Determine building base level.
		// Should only determine supply and demand of connectors with same base level.
		BuildingSpec spec = buildingConfig.getBuildingSpec(buildingName);
		int baseLevel = spec.getBaseLevel();

		// Determine demand.
		double demand = 0D;
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(FunctionType.LIFE_SUPPORT).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			// Demand based on life support buildings that are not building connections.
			if (!building.hasFunction(FunctionType.BUILDING_CONNECTION)) {

				// Only add demand from buildings with same base level as this one.
				if (building.getBaseLevel() == baseLevel) {
					demand += 1D;

					// If building is not EVA and does not have a walkable airlock path, add more
					// demand.
					if ((settlement.getAirlockNum() > 0) && !building.hasFunction(FunctionType.EVA)) {

						if (!settlement.hasWalkableAvailableAirlock(building)) {

							demand += 100D;
						}
					}
				}
			}
		}

		// Determine supply.
		double supply = 0D;
		Iterator<Building> j = settlement.getBuildingManager().getBuildings(FunctionType.BUILDING_CONNECTION)
				.iterator();
		while (j.hasNext()) {
			Building building = j.next();

			// Only add supply from connector buildings with same base level as this one.
			if (building.getBaseLevel() == baseLevel) {
				supply += (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
			}
		}

		if (!newBuilding) {
			supply -= 1D;
			if (supply < 0D) {
				supply = 0D;
			}
		}

		return demand / (supply + 1D);
	}
}
