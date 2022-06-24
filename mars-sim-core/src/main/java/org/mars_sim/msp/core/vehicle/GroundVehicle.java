/**
 * Mars Simulation Project
 * GroundVehicle.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.vehicle;


import org.mars_sim.msp.core.Direction;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The GroundVehicle class represents a ground-type vehicle. It is abstract and
 * should be extended to a particular type of ground vehicle.
 */
public abstract class GroundVehicle extends Vehicle {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(GroundVehicle.class.getName());
	
	public static final String LANDER_HAB = "Lander Hab";
	public static final String OUTPOST_HUB = "Outpost Hub";
			
	// public final static String STUCK = "Stuck - using winch";

	/** Comparison to indicate a small but non-zero amount of fuel (methane) in kg that can still work on the fuel cell to propel the engine. */
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

//	private static TerrainElevation terrain;

	/**
	 * Constructs a {@link GroundVehicle} object at a given settlement.
	 * 
	 * @param name                name of the ground vehicle
	 * @param description         the configuration description of the vehicle.
	 * @param settlement          settlement the ground vehicle is parked at
	 * @param maintenanceWorkTime the work time required for maintenance (millisols)
	 */
	public GroundVehicle(String name, String description, Settlement settlement, double maintenanceWorkTime) {
		// use Vehicle constructor
		super(name, description, settlement, maintenanceWorkTime);

		// Add scope to malfunction manager.
//		malfunctionManager.addScopeString(SystemType.VEHICLE.getName());// "GroundVehicle");

		setTerrainHandlingCapability(0D); // Default terrain capability
	}

	/**
	 * Returns the elevation of the vehicle in km.
	 * 
	 * @return elevation of the ground vehicle (in km)
	 */
	public double getElevation() {
		return elevation;
	}

	/**
	 * Sets the elevation of the vehicle (in km.)
	 * 
	 * @param elevation new elevation for ground vehicle
	 */
	public void setElevation(double elevation) {
		this.elevation = elevation;
	}

	/**
	 * Returns the vehicle's terrain capability
	 * 
	 * @return terrain handling capability of the ground vehicle
	 */
	public double getTerrainHandlingCapability() {
		return terrainHandlingCapability;
	}

	/**
	 * Sets the vehicle's terrain capability
	 * 
	 * @param c sets the ground vehicle's terrain handling capability
	 */
	public void setTerrainHandlingCapability(double c) {
		terrainHandlingCapability = c;
	}

	/**
	 * Gets the average angle of terrain over next 7.4km distance in direction
	 * vehicle is traveling.
	 * 
	 * @return ground vehicle's current terrain grade angle from horizontal
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
	 * Returns true if ground vehicle is stuck
	 * 
	 * @return true if vehicle is currently stuck, false otherwise
	 */
	public boolean isStuck() {
		return isStuck;
	}

	/**
	 * Sets the ground vehicle's stuck value
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
	 * Find a new parking location and facing
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
					
					if (hab != null) {
						centerLoc = hab.getPosition();
					} else  { //if (hub != null) {
						
						int r1 = 0;
						Building hub = null;
						if (numHub > 0) {
							r1 = RandomUtil.getRandomInt((int)numHub - 1);
							hub = settlement.getBuildingManager().getBuildingsOfSameType(OUTPOST_HUB).get(r1);
									
							centerLoc = hub.getPosition();
						}
					}
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

			boolean isSmallVehicle = getVehicleTypeString().equalsIgnoreCase(VehicleType.DELIVERY_DRONE.getName())
					|| getVehicleTypeString().equalsIgnoreCase(VehicleType.LUV.getName());

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

	/**
	 * Checks if the vehicle has enough amount of fuel as prescribed
	 * 
	 * @param fuelConsumed
	 * @return
	 */
	/* 
	protected boolean hasEnoughFuel(double fuelConsumed) {
		Vehicle v = getVehicle();
        int fuelType = v.getFuelType();
        
    	try {
    		double remainingFuel = v.getAmountResourceStored(fuelType);
	
    		if (remainingFuel < LEAST_AMOUNT) {
    			v.setPrimaryStatus(StatusType.PARKED, StatusType.OUT_OF_FUEL);
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
	    			"Could not retrieve methane. Cannot drive.", e);
	    	return false;
	    }
	}
	*/
}
