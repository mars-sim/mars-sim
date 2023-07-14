/*
 * Mars Simulation Project
 * GroundVehicle.java
 * @date 2023-07-12
 * @author Scott Davis
 */
package org.mars_sim.msp.core.vehicle;


import java.util.List;

import org.mars.sim.mapdata.location.Direction;
import org.mars.sim.mapdata.location.LocalPosition;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.environment.TerrainElevation;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingCategory;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

/**
 * This abstract class represents a ground-type vehicle and 
 * must be extended to a concrete vehicle type.
 */
public abstract class GroundVehicle extends Vehicle {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(GroundVehicle.class.getName());
		
    public static final double LEAST_AMOUNT = .001D;
    
    public static final double VEHICLE_CLEARANCE_0 = 1.4;
    public static final double VEHICLE_CLEARANCE_1 = 2.8;
	
	// Data members
	/** Current elevation in km. */
	private double elevation;
	/** Ground vehicle's basic terrain handling capability. */
	private double terrainHandlingCapability;
	/** True if vehicle is stuck. */
	private boolean isStuck;

	/**
	 * Constructs a {@link GroundVehicle} object at a given settlement.
	 * 
	 * @param name                name of the ground vehicle
	 * @param spec         the configuration description of the vehicle.
	 * @param settlement          settlement the ground vehicle is parked at
	 * @param maintenanceWorkTime the work time required for maintenance (millisols)
	 */
	public GroundVehicle(String name, VehicleSpec spec, Settlement settlement, double maintenanceWorkTime) {
		// use Vehicle constructor
		super(name, spec, settlement, maintenanceWorkTime);

		terrainHandlingCapability = spec.getTerrainHandling();
	}

	/**
	 * Returns the elevation of the vehicle [in km].
	 * 
	 * @return elevation of the ground vehicle (in km)
	 */
	public double getElevation() {
		return elevation;
	}

	/**
	 * Sets the elevation of the vehicle [in km].
	 * 
	 * @param elevation new elevation for ground vehicle
	 */
	public void setElevation(double elevation) {
		this.elevation = elevation;
	}

	/**
	 * Returns the vehicle's terrain capability.
	 * 
	 * @return terrain handling capability of the ground vehicle
	 */
	public double getTerrainHandlingCapability() {
		return terrainHandlingCapability;
	}

	/**
	 * Sets the vehicle's terrain capability.
	 * 
	 * @param c sets the ground vehicle's terrain handling capability
	 */
	public void setTerrainHandlingCapability(double c) {
		terrainHandlingCapability = c;
	}

	/**
	 * Gets the average angle of terrain over a sample distance in direction
	 * vehicle is traveling.
	 * 
	 * @return ground vehicle's current terrain grade angle from horizontal
	 *         (radians)
	 */
	public double getTerrainGrade() {
		return getTerrainGrade(getDirection());
	}

	/**
	 * Gets the average angle of terrain over a sample distance in a given
	 * direction from the vehicle.
	 * 
	 * @return ground vehicle's current terrain grade angle from horizontal
	 *         (radians)
	 */
	public double getTerrainGrade(Direction direction) {
		// Determine the terrain grade in a given direction from the vehicle.
		return TerrainElevation.determineTerrainSteepness(getCoordinates(), direction);
	}
    
	/**
	 * Returns true if ground vehicle is stuck.
	 * 
	 * @return true if vehicle is currently stuck, false otherwise
	 */
	public boolean isStuck() {
		return isStuck;
	}

	/**
	 * Sets the ground vehicle's stuck value.
	 * 
	 * @param stuck true if vehicle is currently stuck, false otherwise
	 */
	public void setStuck(boolean stuck) {
		isStuck = stuck;
		if (isStuck) {
			setPrimaryStatus(StatusType.PARKED, StatusType.STUCK);
			setSpeed(0D);
			setParkedLocation(LocalPosition.DEFAULT_POSITION, getDirection().getDirection());
		}
	}

	/**
	 * Finds a new parking location and facing.
	 */
	@Override
	public void findNewParkingLoc() {

		Settlement settlement = getSettlement();
		if (settlement == null) {
			logger.severe(this, "Not found in any settlements.");
		}

		else {
			LocalPosition centerLoc = LocalPosition.DEFAULT_POSITION;

			// Start from near the settlement map center (0,0).

			int oX = 0;
			int oY = 0;

			int weight = 2;

			List<Building> evas = settlement.getBuildingManager().getBuildingsOfSameCategory(BuildingCategory.EVA_AIRLOCK);
			int numGarages = settlement.getBuildingManager().getBuildings(FunctionType.VEHICLE_MAINTENANCE)
					.size();
			int total = (int)(evas.size() + numGarages * weight - 1);
			if (total < 0)
				total = 0;
			int rand = RandomUtil.getRandomInt(total);

			if (rand != 0) {

				// Try parking near the EVA for shortest walk	
				if (rand < evas.size()) {
					Building eva = evas.get(rand);
					centerLoc = eva.getPosition();
				}

				else {
					// Try parking near a garage
					Building garage = BuildingManager.getAGarage(getSettlement());
					centerLoc = garage.getPosition();
				}
			}

			LocalPosition newLoc = LocalPosition.DEFAULT_POSITION;
			double newFacing = 0D;

			double step = 10D;
			boolean foundGoodLocation = false;

			boolean isSmallVehicle = getVehicleType() == VehicleType.DELIVERY_DRONE
					|| getVehicleType() == VehicleType.LUV;

			double d = VEHICLE_CLEARANCE_0;
			if (isSmallVehicle)
				d = VEHICLE_CLEARANCE_1;
			
			double w = getWidth() * d;
			double l = getLength() * d;

			// Try iteratively outward from 10m to 500m distance range.
			for (int x = oX; (x < 500) && !foundGoodLocation; x += step) {
				// Try ten random locations at each distance range.
				for (int y = oY; (y < step) && !foundGoodLocation; y++) {
					double distance = RandomUtil.getRandomDouble(step) + x;
					double radianDirection = RandomUtil.getRandomDouble(Math.PI * 2D);
					newLoc = centerLoc.getPosition(distance, radianDirection);
					newFacing = RandomUtil.getRandomDouble(360D);
			
					// Check if new vehicle location collides with anything.
					foundGoodLocation =	LocalAreaUtil.isObjectCollisionFree(this, w, l, 
							newLoc.getX(), newLoc.getY(), newFacing, getCoordinates());
					// Note: Enlarge the collision surface of a vehicle to avoid getting trapped within those enclosed space 
					// surrounded by buildings or hallways.
					// This is just a temporary solution to stop the vehicle from acquiring a parking between buildings.
					// TODO: need a permanent solution by figuring out how to detect those enclosed space
				}
			}

			setParkedLocation(newLoc, newFacing);
		}
	}
}
