/**
 * Mars Simulation Project
 * GroundVehicle.java
 * @version 3.1.0 2017-08-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.vehicle;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.mars_sim.msp.core.Direction;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.mars.TerrainElevation;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.SystemType;

/** The GroundVehicle class represents a ground-type vehicle.  It is
 *  abstract and should be extended to a particular type of ground
 *  vehicle.
 */
public abstract class GroundVehicle
extends Vehicle
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** TODO Ground Vehicle Status String should be internationalized. */
	public final static String STUCK = "Stuck - using winch";

	// Data members
	/** Current elevation in km. */
	private double elevation;
	/** Ground vehicle's basic terrain handling capability. */
	private double terrainHandlingCapability;
	/** True if vehicle is stuck. */
	private boolean isStuck;

	/** 
	 * Constructs a {@link GroundVehicle} object at a given settlement.
	 * @param name name of the ground vehicle
	 * @param description the configuration description of the vehicle.
	 * @param settlement settlement the ground vehicle is parked at
	 * @param maintenanceWorkTime the work time required for maintenance (millisols)
	 */
	public GroundVehicle(String name, String description, Settlement settlement, 
			double maintenanceWorkTime) {
		// use Vehicle constructor
		super(name, description, settlement, maintenanceWorkTime);

		// Add scope to malfunction manager.
		malfunctionManager.addScopeString(SystemType.VEHICLE.getName());//"GroundVehicle");

		setTerrainHandlingCapability(0D); // Default terrain capability
		SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
		elevation = surface.getTerrainElevation().getElevation(getCoordinates());
	}

	/**
	 * Returns vehicle's current status
	 * @return the vehicle's current status
	 */
	public String getStatus() {
		String status = null;

		if (isStuck) status = STUCK;
		else status = super.getStatus();

		return status;
	}

	/**
	 * Returns the elevation of the vehicle in km. 
	 * @return elevation of the ground vehicle (in km)
	 */
	public double getElevation() {
		return elevation;
	}

	/**
	 * Sets the elevation of the vehicle (in km.) 
	 * @param elevation new elevation for ground vehicle
	 */
	public void setElevation(double elevation) {
		this.elevation = elevation;
	}

	/**
	 * Returns the vehicle's terrain capability 
	 * @return terrain handling capability of the ground vehicle
	 */
	public double getTerrainHandlingCapability() {
		return terrainHandlingCapability;
	}

	/**
	 * Sets the vehicle's terrain capability 
	 * @param c sets the ground vehicle's terrain handling capability
	 */
	public void setTerrainHandlingCapability(double c) {
		terrainHandlingCapability = c;
	}

	/** 
	 * Gets the average angle of terrain over next 7.4km distance in direction vehicle is traveling.
	 * @return ground vehicle's current terrain grade angle from horizontal (radians)
	 */
	public double getTerrainGrade() {
		return getTerrainGrade(getDirection());
	}

	/** 
	 * Gets the average angle of terrain over next 7.4km distance in a given direction from the vehicle.
	 * @return ground vehicle's current terrain grade angle from horizontal (radians)
	 */
	public double getTerrainGrade(Direction direction) {
		// Determine the terrain grade in a given direction from the vehicle.
		SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
		TerrainElevation terrain = surface.getTerrainElevation();
		return terrain.determineTerrainDifficulty(getCoordinates(), direction);
	}

	/**
	 * Returns true if ground vehicle is stuck 
	 * @return true if vehicle is currently stuck, false otherwise
	 */
	public boolean isStuck() {
		return isStuck;
	}

	/**
	 * Sets the ground vehicle's stuck value 
	 * @param stuck true if vehicle is currently stuck, false otherwise
	 */
	public void setStuck(boolean stuck) {
		isStuck = stuck;
		if (isStuck) {
			setSpeed(0D);
			setParkedLocation(0D, 0D, getDirection().getDirection());
		}
	}

	/**
	 * Gets the driver of the ground vehicle.
	 * @return the vehicle driver.
	 */
	public VehicleOperator getDriver() {
		return getOperator();
	}

	/**
	 * Sets the driver of the ground vehicle.
	 * @param operator the driver
	 */
	public void setDriver(VehicleOperator operator) {
		setOperator(operator);
	}

	@Override
	public void determinedSettlementParkedLocationAndFacing() {

		Settlement settlement = getSettlement();
		if (settlement == null) {
			throw new IllegalStateException("Vehicle not parked at a settlement");
		}

		double centerXLoc = 0D;
		double centerYLoc = 0D;

		// If settlement has garages, place vehicle near a random garage.
		// Otherwise place vehicle near settlement center.
		List<Building> garageList = settlement.getBuildingManager().getBuildings(
				FunctionType.GROUND_VEHICLE_MAINTENANCE);
		if (garageList.size() >= 1) {
			Collections.shuffle(garageList);
			Building garage = garageList.get(0);
			centerXLoc = garage.getXLocation();
			centerYLoc = garage.getYLocation();
		}

		double newXLoc = 0D;
		double newYLoc = 0D;
		double newFacing = 0D;
		boolean foundGoodLocation = false;

		// Try iteratively outward from 10m to 500m distance range.
		for (int x = 15; (x < 500) && !foundGoodLocation; x+= 10) {
			// Try ten random locations at each distance range.
			for (int y = 0; (y < 10) && !foundGoodLocation; y++) {
				double distance = RandomUtil.getRandomDouble(10D) + x;
				double radianDirection = RandomUtil.getRandomDouble(Math.PI * 2D);
				newXLoc = centerXLoc - (distance * Math.sin(radianDirection));
				newYLoc = centerYLoc + (distance * Math.cos(radianDirection));
				newFacing = RandomUtil.getRandomDouble(360D);

				// Check if new vehicle location collides with anything.
				foundGoodLocation = LocalAreaUtil.checkBoundedObjectNewLocationCollision(this, 
						newXLoc, newYLoc, newFacing, getCoordinates());
			}
		}

		setParkedLocation(newXLoc, newYLoc, newFacing);
	}
}