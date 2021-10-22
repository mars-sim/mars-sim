/**
 * Mars Simulation Project
 * GroundVehicleMaintenance.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Iterator;

/**
 * The GroundVehicleMaintenance class is a building function for a building
 * capable of maintaining ground vehicles.
 */
public class GroundVehicleMaintenance
extends VehicleMaintenance
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param building the building the function is for.
	 */
	public GroundVehicleMaintenance(Building building) {
		// Call VehicleMaintenance constructor.
		super(FunctionType.GROUND_VEHICLE_MAINTENANCE, building);

		vehicleCapacity = buildingConfig.getFunctionCapacity(building.getBuildingType(),
															 FunctionType.GROUND_VEHICLE_MAINTENANCE);

		int parkingLocationNum = buildingConfig.getParkingLocationNumber(building.getBuildingType());
		for (int x = 0; x < parkingLocationNum; x++) {
			Point2D parkingLocationPoint = buildingConfig.getParkingLocation(building.getBuildingType(), x);
			addParkingLocation(parkingLocationPoint.getX(), parkingLocationPoint.getY());
		}
	}

	/**
	 * Constructor.
	 * @param building the building the function is for.
	 * @param vehicleCapacity the number of vehicles that can be parked.
	 * @param parkingLocations the parking locations.
	 */
	public GroundVehicleMaintenance(Building building, int vehicleCapacity, 
			Point2D[] parkingLocations) {
		// Call VehicleMaintenance constructor.
		super(FunctionType.GROUND_VEHICLE_MAINTENANCE, building);

		this.vehicleCapacity = vehicleCapacity;
		
		for (int x = 0; x < parkingLocations.length; x++) {
			addParkingLocation(parkingLocations[x].getX(), parkingLocations[x].getY());
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
			if (!newBuilding && building.getBuildingType().equals(buildingName) && !removedBuilding) {
				removedBuilding = true;
			}
			else {
				GroundVehicleMaintenance maintFunction = building.getGroundVehicleMaintenance();
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += maintFunction.getVehicleCapacity() * wearModifier;
			}
		}

		double vehicleCapacityValue = demand / (supply + 1D);

		double vehicleCapacity = buildingConfig.getFunctionCapacity(buildingName, FunctionType.GROUND_VEHICLE_MAINTENANCE);

		return vehicleCapacity * vehicleCapacityValue;
	}
}
