/*
 * Mars Simulation Project
 * GroundVehicle.java
 * @date 2023-07-12
 * @author Scott Davis
 */
package com.mars_sim.core.vehicle;


import com.mars_sim.core.environment.TerrainElevation;
import com.mars_sim.core.map.location.Direction;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.structure.Settlement;

/**
 * This abstract class represents a ground-type vehicle and 
 * must be extended to a concrete vehicle type.
 */
public abstract class GroundVehicle extends Vehicle {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// default logger.
	// May add back SimLogger logger = SimLogger.getLogger(GroundVehicle.class.getName());
		
    public static final double LEAST_AMOUNT = .001D;
	
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
}
