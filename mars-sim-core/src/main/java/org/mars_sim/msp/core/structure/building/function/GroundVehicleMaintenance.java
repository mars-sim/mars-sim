/**
 * Mars Simulation Project
 * GroundVehicleMaintenance.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.util.Iterator;

import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.FunctionSpec;

/**
 * The GroundVehicleMaintenance class is a building function for a building
 * capable of maintaining ground vehicles.
 */
public class GroundVehicleMaintenance
extends VehicleMaintenance {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param building the building the function is for.
	 * @param spec Defines the Function details
	 */
	public GroundVehicleMaintenance(Building building, FunctionSpec spec) {
		// Call VehicleMaintenance constructor.
		super(FunctionType.GROUND_VEHICLE_MAINTENANCE, spec, building);

		for (LocalPosition parkingLocationPoint : buildingConfig.getParkingLocations(building.getBuildingType())) {
			addParkingLocation(parkingLocationPoint);
		}
	}

	/**
	 * Constructor.
	 * @param building the building the function is for.
	 * @param parkingLocations the parking locations.
	 */
	public GroundVehicleMaintenance(Building building, LocalPosition[] parkingLocations) {
		// Call VehicleMaintenance constructor.
		super(FunctionType.GROUND_VEHICLE_MAINTENANCE, null, building);
		
		for (LocalPosition parkingLocation : parkingLocations) {
			addParkingLocation(parkingLocation);
		}
	}

	/**
	 * Gets the value of the function for a named building.
	 * @param buildingName the building name.
	 * @param newBuilding true if adding a new building.
	 * @param settlement the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String buildingName, boolean newBuilding,
			Settlement settlement) {

		// Demand is one ground vehicle capacity for every ground vehicles.
		double demand = settlement.getOwnedVehicleNum();

		double supply = 0D;
		boolean removedBuilding = false;
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(FunctionType.GROUND_VEHICLE_MAINTENANCE).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
				removedBuilding = true;
			}
			else {
				GroundVehicleMaintenance maintFunction = building.getGroundVehicleMaintenance();
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += maintFunction.getVehicleCapacity() * wearModifier;
			}
		}

		double vehicleCapacityValue = demand / (supply + 1D);

		double vehicleCapacity = buildingConfig.getParkingLocations(buildingName).size();

		return vehicleCapacity * vehicleCapacityValue;
	}
}
