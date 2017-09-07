/**
 * Mars Simulation Project
 * GroundVehicleMaintenance.java
 * @version 3.07 2014-06-24
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;

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

	private static final FunctionType FUNCTION = FunctionType.GROUND_VEHICLE_MAINTENANCE;

	/**
	 * Constructor.
	 * @param building the building the function is for.
	 */
	public GroundVehicleMaintenance(Building building) {
		// Call VehicleMaintenance constructor.
		super(FUNCTION, building);

		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();

		vehicleCapacity = config.getVehicleCapacity(building.getBuildingType());

		int parkingLocationNum = config.getParkingLocationNumber(building.getBuildingType());
		for (int x = 0; x < parkingLocationNum; x++) {
			Point2D.Double parkingLocationPoint = config.getParkingLocation(building.getBuildingType(), x);
			addParkingLocation(parkingLocationPoint.getX(), parkingLocationPoint.getY());
		}
		
		// Load activity spots
        loadActivitySpots(config.getGroundVehicleMaintenanceActivitySpots(building.getBuildingType()));
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
		super(FUNCTION, building);

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
		double demand = settlement.getAllAssociatedVehicles().size();

		double supply = 0D;
		boolean removedBuilding = false;
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (!newBuilding && building.getBuildingType().equals(buildingName) && !removedBuilding) {
				removedBuilding = true;
			}
			else {
				GroundVehicleMaintenance maintFunction = 
						(GroundVehicleMaintenance) building.getFunction(FUNCTION);
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += maintFunction.getVehicleCapacity() * wearModifier;
			}
		}

		double vehicleCapacityValue = demand / (supply + 1D);

		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
		double vehicleCapacity = config.getVehicleCapacity(buildingName);

		return vehicleCapacity * vehicleCapacityValue;
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