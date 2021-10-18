/**
 * Mars Simulation Project
 * Flyer.java
 * @version 3.2.0 2021-06-20
 * @author Manny
 */

package org.mars_sim.msp.core.vehicle;

import java.io.Serializable;
import java.util.logging.Level;

import org.mars_sim.msp.core.Direction;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The Flyer class represents an airborne.
 */
public abstract class Flyer extends Vehicle implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(Flyer.class.getName());
	
	public static final String LANDER_HAB = "Lander Hab";
	public static final String OUTPOST_HUB = "Outpost Hub";
			
	/** Comparison to indicate a small but non-zero amount of fuel (methane) in kg that can still work on the fuel cell to propel the engine. */
    public static final double LEAST_AMOUNT = .001D;
    
    /** Ideal hovering elevation. */
	public final static double ELEVATION_ABOVE_GROUND = .1; // in km
	
	// Data members
	/** Current total elevation above the sea level in km. */
	private double elevation;

	/** Current hovering elevation in km. */
	private double hoveringElevation;

//	/** Current Angle of Attack in degree. */
//	private double AoA;

	// NASA Space Shuttle Fuel Cell Power Plant 7.6 kg/kW
	// The targeted space systems feature power outputs of 1 to 10 kW systems 
	// (eventually scalable up to 100 kW), compact sizing (250 to 350 watts per kg),
	// and high reliability for long lives (10,000 hours).
	
	/**
	 * Constructs a {@link Flyer} object at a given settlement.
	 * 
	 * @param name                name of the airborne vehicle
	 * @param description         the configuration description of the vehicle.
	 * @param settlement          settlement the airborne vehicle is parked at
	 * @param maintenanceWorkTime the work time required for maintenance (millisols)
	 */
	public Flyer(String name, String description, Settlement settlement, double maintenanceWorkTime) {
		// use Vehicle constructor
		super(name, description, settlement, maintenanceWorkTime);
	}

	/**
	 * Returns the hovering elevation of the vehicle in km.
	 * 
	 * @return elevation of the airborne vehicle (in km)
	 */
	public double getHoveringElevation() {
		return hoveringElevation;
	}

	/**
	 * Sets the hovering elevation of the vehicle (in km.)
	 * 
	 * @param elevation new elevation for airborne vehicle
	 */
	public void setHoveringlevation(double elevation) {
		this.hoveringElevation = elevation;
	}

	/**
	 * Returns the total elevation of the vehicle above the sea level in km.
	 * 
	 * @return elevation of the airborne vehicle (in km)
	 */
	public double getElevation() {
		return elevation;
	}

	/**
	 * Sets the elevation of the vehicle (in km.)
	 * 
	 * @param elevation new elevation for airborne vehicle
	 */
	public void setElevation(double elevation) {
		this.elevation = elevation;
	}

	/**
	 * Gets the average angle of attack over next 7.4km distance in direction
	 * vehicle is traveling.
	 * 
	 * @return airborne vehicle's current angle of attack in radians from horizontal plane
	 */
	public double getAngleOfAttack() {
		return getTerrainGrade();
	}

	/**
	 * Gets the average angle of terrain over next 7.4km distance in direction
	 * vehicle is traveling.
	 * 
	 * @return vehicle's current terrain grade angle from horizontal
	 *         (radians)
	 */
	public double getTerrainGrade() {
		return getTerrainGrade(getDirection());
	}

	/**
	 * Gets the average angle of terrain over next 7.4km distance in a given
	 * direction from the vehicle.
	 * 
	 * @return ground vehicle's current terrain grade angle from horizontal
	 *         (radians)
	 */
	public double getTerrainGrade(Direction direction) {
		// Determine the terrain grade in a given direction from the vehicle.
		if (terrainElevation == null)
			terrainElevation = surfaceFeatures.getTerrainElevation();
		return terrainElevation.determineTerrainSteepness(getCoordinates(), direction);
	}
	
	/**
	 * Find a new parking location and facing
	 */
	@Override
	public void findNewParkingLoc() {

		Settlement settlement = getSettlement();
		if (settlement == null) {
			logger.severe(this, "Not found to be parked in a settlement.");
		}

		else {
			
			double centerXLoc = 0D;
			double centerYLoc = 0D;

			// Place the vehicle starting from the settlement center (0,0).

			int oX = 15;
			int oY = 0;

			int weight = 2;

			long numHab = settlement.getBuildingManager().getNumBuildingsOfSameType(LANDER_HAB);
			long numHub = settlement.getBuildingManager().getNumBuildingsOfSameType(OUTPOST_HUB);
			int numGarages = settlement.getBuildingManager().getBuildings(FunctionType.GROUND_VEHICLE_MAINTENANCE)
					.size();
			int total = (int)(numHab + numHub + numGarages * weight - 1);
			if (total < 0)
				total = 0;
			int rand = RandomUtil.getRandomInt(total);

			if (rand != 0) {

				// Try parking near the lander hab or outpost hub
				
				if (rand < numHab + numHub) {
					int r0 = RandomUtil.getRandomInt((int)numHab - 1);
					Building hab = settlement.getBuildingManager().getBuildingsOfSameType(LANDER_HAB).get(r0);
					int r1 = 0;
					Building hub = null;
					if (numHub > 0) {
						r1 = RandomUtil.getRandomInt((int)numHub - 1);
						hub = settlement.getBuildingManager().getBuildingsOfSameType(OUTPOST_HUB).get(r1);
					}

					if (hab != null) {
						centerXLoc = (int) hab.getXLocation();
						centerYLoc = (int) hab.getYLocation();
					} else if (hub != null) {
						centerXLoc = (int) hub.getXLocation();
						centerYLoc = (int) hub.getYLocation();
					}
				}

				else {
					// Try parking near a garage
					
					Building garage = BuildingManager.getAGarage(getSettlement());
					centerXLoc = (int) garage.getXLocation();
					centerYLoc = (int) garage.getYLocation();
				}
			}

			double newXLoc = 0D;
			double newYLoc = 0D;

			double newFacing = 0D;

			double step = 10D;
			boolean foundGoodLocation = false;

			// Try iteratively outward from 10m to 500m distance range.
			for (int x = oX; (x < 500) && !foundGoodLocation; x += step) {
				// Try ten random locations at each distance range.
				for (int y = oY; (y < step) && !foundGoodLocation; y++) {
					double distance = RandomUtil.getRandomDouble(step) + x;
					double radianDirection = RandomUtil.getRandomDouble(Math.PI * 2D);
					newXLoc = centerXLoc - (distance * Math.sin(radianDirection));
					newYLoc = centerYLoc + (distance * Math.cos(radianDirection));
					newFacing = RandomUtil.getRandomDouble(360D);

					// Check if new vehicle location collides with anything.
					foundGoodLocation = //LocalAreaUtil.isGoodLocation(this, newXLoc, newYLoc, newFacing, getCoordinates());
							LocalAreaUtil.isObjectCollisionFree(this, this.getWidth() * 1.3, this.getLength() * 1.3, newXLoc,
							newYLoc, newFacing, getCoordinates());
					// Note: Enlarge the collision surface of a vehicle to avoid getting trapped within those enclosed space 
					// surrounded by buildings or hallways.
					// This is just a temporary solution to stop the vehicle from acquiring a parking between buildings.
					// TODO: need a permanent solution by figuring out how to detect those enclosed space
				}
			}

			setParkedLocation(newXLoc, newYLoc, newFacing);

		}
	}

	/**
	 * Checks if the vehicle has enough amount of fuel as prescribed
	 * 
	 * @param fuelConsumed
	 * @return true if it has enough fuel
	 */
	protected boolean hasEnoughFuel(double fuelConsumed) {
		Vehicle v = getVehicle();
        int fuelType = v.getFuelType();
        
    	try {
    		double remainingFuel = v.getAmountResourceStored(fuelType);
//	
    		if (remainingFuel < LEAST_AMOUNT) {
    			v.addStatus(StatusType.OUT_OF_FUEL);
    			return false;
    		}
    			
    		if (fuelConsumed > remainingFuel) {
            	fuelConsumed = remainingFuel;
            	return false;
    		}
    		else
    			return true;
	    }
	    catch (Exception e) {
	    	logger.log(this, Level.SEVERE, 0, 
	    			"Could not retrieve methane. Cannot fly.", e);
	    	return false;
	    }
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
	}
}
