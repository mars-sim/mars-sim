/**
 * Mars Simulation Project
 * EarthReturn.java
 * @version 3.1.0 2019-09-20
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.Iterator;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * A building function for launching an Earth return mission.
 */
public class EarthReturn extends Function implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final FunctionType NAME = FunctionType.EARTH_RETURN;

	// Data members
	private int crewCapacity;
	private boolean hasLaunched;

	/**
	 * Constructor.
	 * 
	 * @param building the building this function is for.
	 */
	public EarthReturn(Building building) {
		// Use Function constructor.
		super(NAME, building);

		// Populate data members.
		crewCapacity = buildingConfig.getEarthReturnCrewCapacity(building.getBuildingType());

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
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(NAME).iterator();
		while (i.hasNext()) {
			Building earthReturnBuilding = i.next();
			EarthReturn earthReturn = (EarthReturn) earthReturnBuilding.getFunction(NAME);
			double crewCapacity = earthReturn.getCrewCapacity();
			double wearFactor = ((earthReturnBuilding.getMalfunctionManager().getWearCondition() / 100D) * .75D) + .25D;
			supply += crewCapacity * wearFactor;
		}

		if (!newBuilding) {
			supply -= buildingConfig.getEarthReturnCrewCapacity(buildingName);
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
	public double getFullPowerRequired() {
		return 0D;
	}

	@Override
	public double getPoweredDownPowerRequired() {
		return 0D;
	}

	@Override
	public void timePassing(double time) {
		// Do nothing
	}

	@Override
	public double getMaintenanceTime() {
		return crewCapacity * 50D;
	}

	@Override
	public double getFullHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPoweredDownHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}
}