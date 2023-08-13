/*
 * Mars Simulation Project
 * EarthReturn.java
 * @date 2023-04-15
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.building.function;

import java.util.Iterator;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.FunctionSpec;

/**
 * A building function for an Earth return vehicle.
 */
public class EarthReturn extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private int crewCapacity;
	private boolean hasLaunched;

	/**
	 * Constructor.
	 *
	 * @param building the building this function is for.
	 */
	public EarthReturn(Building building, FunctionSpec spec) {
		// Use Function constructor.
		super(FunctionType.EARTH_RETURN, spec, building);

		// Populate data members.
		crewCapacity = spec.getCapacity();

		// Initialize hasLaunched to false.
		hasLaunched = false;
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

		// Settlements need enough Earth return facilities to support population.
		double demand = settlement.getNumCitizens();

		// Supply based on wear condition of buildings.
		double supply = 0D;
		Iterator<Building> i = settlement.getBuildingManager().getBuildingSet(FunctionType.EARTH_RETURN).iterator();
		while (i.hasNext()) {
			Building earthReturnBuilding = i.next();
			EarthReturn earthReturn = earthReturnBuilding.getEarthReturn();
			double crewCapacity = earthReturn.getCrewCapacity();
			double wearFactor = ((earthReturnBuilding.getMalfunctionManager().getWearCondition() / 100D) * .75D) + .25D;
			supply += crewCapacity * wearFactor;
		}

		if (!newBuilding) {
			supply -= buildingConfig.getFunctionSpec(buildingName, FunctionType.EARTH_RETURN).getCapacity();
			if (supply < 0D)
				supply = 0D;
		}

		return demand / (supply + 1D);
	}

	/**
	 * Get the crew capacity for an Earth return mission.
	 *
	 * @return crew capacity.
	 */
	public int getCrewCapacity() {
		return crewCapacity;
	}

	/**
	 * Checks if the Earth return mission for this building has launched.
	 *
	 * @return true if mission has launched.
	 */
	public boolean hasLaunched() {
		return hasLaunched;
	}

	@Override
	public double getMaintenanceTime() {
		return crewCapacity * 50D;
	}

}
