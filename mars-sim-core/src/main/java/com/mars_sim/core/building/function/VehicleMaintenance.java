/*
 * Mars Simulation Project
 * VehicleMaintenance.java
 * @date 2025-07-20
 * @author Scott Davis
 */
package com.mars_sim.core.building.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingException;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.FunctionSpec;
import com.mars_sim.core.data.UnitSet;
import com.mars_sim.core.location.LocationStateType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.Drone;
import com.mars_sim.core.vehicle.Flyer;
import com.mars_sim.core.vehicle.LightUtilityVehicle;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * The VehicleMaintenance interface is a building function for a building
 * capable of maintaining vehicles.
 */
public abstract class VehicleMaintenance extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(VehicleMaintenance.class.getName());

	protected List<RoverLocation> roverLocations;
	protected List<UtilityVehicleLocation> luvLocations;
	protected List<FlyerLocation> flyerLocations;
	
	private Collection<Rover> rovers;
	private Collection<LightUtilityVehicle> luvs;
	private Collection<Flyer> flyers;
	
	/**
	 * Constructor.
	 * 
	 * @param function the name of the child function.
	 * @param building the building this function is for.
	 */
	public VehicleMaintenance(FunctionType function, FunctionSpec spec, Building building) {
		// Use Function constructor.
		super(function,spec, building);

		rovers = new UnitSet<>();
		luvs = new UnitSet<>();
		flyers = new UnitSet<>();
		
		roverLocations = new ArrayList<>();
		luvLocations = new ArrayList<>();
		flyerLocations = new ArrayList<>();
	}

	/**
	 * Gets the number of rovers the building can accommodate.
	 * 
	 * @return rover capacity
	 */
	public int getRoverCapacity() {
		return roverLocations.size();
	}

	/**
	 * Gets the current number of rovers in the building.
	 * 
	 * @return number of rover
	 */
	public int getCurrentRoverNumber() {
		return rovers.size();
	}

	/**
	 * How many available rover locations unoccupied does the garage have?
	 * 
	 * @param Available rover parking locations.
	 */
	public int getAvailableRoverCapacity() {
		return roverLocations.size() - rovers.size();
	}

	/**
	 * Gets the number of utility vehicles the building can accommodate.
	 * 
	 * @return vehicle capacity
	 */
	public int getUtilityVehicleCapacity() {
		return luvLocations.size();
	}

	/**
	 * Gets the current number of utility vehicles in the building.
	 * 
	 * @return number of luvs
	 */
	public int getCurrentUtilityVehicleNumber() {
		return luvLocations.size();
	}

	/**
	 * How many available luv locations unoccupied does the garage have?
	 * 
	 * @param Available luv parking locations.
	 */
	public int getAvailableUtilityVehicleCapacity() {
		return luvLocations.size() - luvs.size();
	}
	
	/**
	 * Gets the number of flyers the building can accommodate.
	 * 
	 * @return flyer capacity
	 */
	public int getFlyerCapacity() {
		return flyerLocations.size();
	}

	/**
	 * Gets the current number of flyers in the building.
	 * 
	 * @return number of flyers
	 */
	public int getCurrentFlyerNumber() {
		return flyers.size();
	}

	/**
	 * How many available flyer locations unoccupied does the garage have?
	 * 
	 * @param Available flyer locations.
	 */
	public int getAvailableFlyerCapacity() {
		return flyerLocations.size() - flyers.size();
	}

	/**
	 * Adds vehicle to building if there's room for parking.
	 * 
	 * @param vehicle the vehicle to be added.
	 * @return true if vehicle can be added.
	 */
	public boolean addRover(Rover rover) {

		// Check if vehicle cannot be added to building.
		if (rovers.contains(rover)) {
			logger.log(rover, Level.INFO, 1000, 
				"Already garaged in " + building + ".");
			 return false;
		}
		
		if (rovers.size() >= roverLocations.size()) {
			logger.log(rover, Level.INFO, 1000,
				building + " already full.");
			return false;
		}
		
		// Add rover to building.
		if (rovers.add(rover)) {
	
			// Put vehicle in assigned parking location within building.
			RoverLocation location = getEmptyRoverParkingLocation();
			LocalPosition newLoc;
			
			if (location != null) {
				newLoc = LocalAreaUtil.convert2SettlementPos(location.getPosition(), getBuilding());
				location.parkRover(rover);
				
				// change the vehicle status
				rover.setPrimaryStatus(StatusType.GARAGED);
				// Update the vehicle's location state type
				rover.setLocationStateType(LocationStateType.INSIDE_SETTLEMENT);
				
				double newFacing = getBuilding().getFacing();
				rover.setParkedLocation(newLoc, newFacing);
		
				logger.fine(rover, "Added to " + building.getName() 
					+ " in " + building.getSettlement() + ".");

				return true;
			}
		}
		
		// can't add the vehicle to a garage
		return false;
	}

	
	/**
	 * Adds vehicle to building if there's room for parking.
	 * 
	 * @param vehicle the vehicle to be added.
	 * @return true if vehicle can be added.
	 */
	public boolean addUtilityVehicle(LightUtilityVehicle luv) {
	
		// Check if vehicle cannot be added to building.
		if (luvs.contains(luv)) {
			logger.log(luv, Level.INFO, 1000, 
				"Already garaged in " + building + ".");
			 return false;
		}
		
		if (luvs.size() >= luvLocations.size()) {
			logger.log(luv, Level.INFO, 1000,
				building + " already full.");
			return false;
		}
		
		// Add vehicle to building.
		if (luvs.add(luv)) {
	
			// Put vehicle in assigned parking location within building.
			UtilityVehicleLocation location = getEmptyUtilityVehicleParkingLocation();
			LocalPosition newLoc;
			
			if (location != null) {
				newLoc = LocalAreaUtil.convert2SettlementPos(location.getPosition(), getBuilding());
				location.parkUtilityVehicle(luv);
				
				// change the vehicle status
				luv.setPrimaryStatus(StatusType.GARAGED);
				// Update the vehicle's location state type
				luv.setLocationStateType(LocationStateType.INSIDE_SETTLEMENT);
				
				double newFacing = getBuilding().getFacing();
				luv.setParkedLocation(newLoc, newFacing);
		
				logger.fine(luv, "Added to " + building.getName() 
					+ " in " + building.getSettlement() + ".");
	
				return true;
			}
		}
		
		// can't add the vehicle to a garage
		return false;
	}

	/**
	 * Adds flyer to building if there's room for parking.
	 * 
	 * @param flyer the flyer to be added.
	 * @return true if flyer can be added.
	 */
	public boolean addFlyer(Flyer flyer) {

		// Check if flyer cannot be added to building.
		if (flyers.contains(flyer)) {
			logger.log(flyer, Level.INFO, 1000, 
				"Flyer already garaged in " + building + ".");
			 return false;
		}
		
		if (flyers.size() >= flyerLocations.size()) {
			logger.log(flyer, Level.INFO, 1000,
				building + " already full.");
			return false;
		}

		if (flyers.add(flyer)) {
			
			// Put flyer in assigned parking location within building.
			FlyerLocation location = getEmptyFlyerLocation();
			LocalPosition newLoc;
			
			if (location != null) {
				newLoc = LocalAreaUtil.convert2SettlementPos(location.getPosition(), getBuilding());
				location.parkFlyer(flyer);
				
				// change the flyer status
				flyer.setPrimaryStatus(StatusType.GARAGED);
				// Update the flyer's location state type
				flyer.setLocationStateType(LocationStateType.INSIDE_SETTLEMENT);
				
				double newFacing = getBuilding().getFacing();
				flyer.setParkedFlyerLocation(newLoc, newFacing);
		
				logger.fine(flyer, "Added to " + building.getName() 
					+ " in " + building.getSettlement() + ".");
				
				return true;
			}
		}
		
		// can't add the flyer to a garage
		return false;
	}	

	/**
	 * Remove a rover from garage building.
	 * 
	 * @param rover the rover to be removed.
	 * @return true if successfully removed
	 */
	public boolean removeRover(Rover rover, boolean transferCrew) {
		if (!containsRover(rover)) {
			return false;
		}

		// Note: Check if using Collection.remove() below is safe
		if (rovers.remove(rover)) {
			
			if (transferCrew)
				relocateCrew(rover);
			 
			parkInVicinity(rover);

			logger.fine(rover, "Removed from " + building.getName() 
				+ " in " + building.getSettlement() + ".");
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Remove a utility vehicle from garage building.
	 * 
	 * @param vehicle the vehicle to be removed.
	 * @return true if successfully removed
	 */
	public boolean removeUtilityVehicle(LightUtilityVehicle luv, boolean transferCrew) {
		if (!containsUtilityVehicle(luv)) {
			return false;
		}

		// Note: Check if using Collection.remove() below is safe
		if (luvs.remove(luv)) {
			
			if (transferCrew)
				relocateCrew(luv);
			 
			parkInVicinity(luv);

			logger.fine(luv, "Removed from " + building.getName() 
				+ " in " + building.getSettlement() + ".");
			
			return true;
		}
		
		return false;
	}

	
	/**
	 * Remove flyer from garage building.
	 * 
	 * @param flyer the flyer to be removed.
	 * @return true if successfully removed
	 */
	public boolean removeFlyer(Flyer flyer) {
		if (!containsFlyer(flyer)) {
			return false;
		}

		// Note: Check if using Collection.remove() below is safe
		if (flyers.remove(flyer)) {
			 
			parkInVicinity(flyer);

			logger.fine(flyer, "Removed from " + building.getName() 
				+ " in " + building.getSettlement() + ".");
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Relocates the crew.
	 * 
	 * @param vehicle
	 */
	public void relocateCrew(Vehicle vehicle) {
		
		if (vehicle instanceof Crewable c) {
			// Remove the human occupants from the settlement
			// But is this needed ? These should already be in the Vehicle
			// if there are in the crew
			for (Person p: new ArrayList<>(c.getCrew())) {
				// If person's origin is already in this vehicle
				// and it's called by removeFromGarage()
				Vehicle v = p.getVehicle();
				if (v != null) {
					BuildingManager.removePersonFromBuilding(p, building);
				}
			}
			// Remove the robot occupants from the settlement
			for (Robot r: new ArrayList<>(c.getRobotCrew())) {
				Vehicle v = r.getVehicle();
				if (v != null) {
					BuildingManager.removeRobotFromBuilding(r, building);
				}
			}
		}
	}
	
	
	/**
	 * Parks the vehicle in settlement vicinity upon being removed from garage.
	 * 
	 * @param vehicle
	 */
	public void parkInVicinity(Vehicle vehicle) {
		
		// FUTURE: should be done in a task to relocate the vehicle by either a person
		// or by AI that costs a minute amount of CUs.
		
		if (vehicle instanceof Rover r) {
			RoverLocation loc = getRoverParkedLocation(r);
			if (loc != null) {
				loc.clearParking();
			}
		}
		else if (vehicle instanceof Drone d) {
			FlyerLocation loc = getFlyerParkedLocation(d);
			if (loc != null) {
				loc.clearParking();
			}
		}
		else if (vehicle instanceof LightUtilityVehicle luv) {
			UtilityVehicleLocation loc = getUtilityVehicleParkedLocation(luv);
			if (loc != null) {
				loc.clearParking();
			}
		}
		

		vehicle.setPrimaryStatus(StatusType.PARKED);
		// Update the vehicle's location state type
		vehicle.setLocationStateType(LocationStateType.SETTLEMENT_VICINITY);
		// Find a new parking location
		vehicle.findNewParkingLoc();
	}
	
	/**
	 * Checks if a rover is in the building.
	 * 
	 * @param rover
	 * @return true if rover is in the building.
	 */
	public boolean containsRover(Rover rover) {
		return rovers.contains(rover);
	}

	/**
	 * Checks if a LUV is in the building.
	 * 
	 * @param luv
	 * @return true if LUV is in the building.
	 */
	public boolean containsUtilityVehicle(LightUtilityVehicle luv) {
		return luvs.contains(luv);
	}

	
	/**
	 * Checks if a flyer is in the building.
	 * 
	 * @param flyer
	 * @return true if flyer is in the building.
	 */
	public boolean containsFlyer(Flyer flyer) {
		return flyers.contains(flyer);
	}
	
	/**
	 * Gets a collection of rovers in the building.
	 * 
	 * @return Collection of rovers in the building.
	 */
	public Collection<Rover> getRovers() {
		return rovers;
	}

	/**
	 * Gets a collection of luvs in the building.
	 * 
	 * @return Collection of luvs in the building.
	 */
	public Collection<LightUtilityVehicle> getUtilityVehicles() {
		return luvs;
	}
	
	/**
	 * Gets a collection of flyers in the building.
	 * 
	 * @return Collection of flyers in the building.
	 */
	public Collection<Flyer> getFlyers() {
		return flyers;
	}
	
	/**
	 * Time passing for the building.
	 * 
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public boolean timePassing(ClockPulse pulse) {
		boolean valid = isValid(pulse);
		if (valid) {
			// Check to see if any vehicles are in the garage that don't need to be.
//			Iterator<Vehicle> i = vehicles.iterator();
//			while (i.hasNext()) {
//				Vehicle vehicle = i.next();
//				// Do not touch any reserved vehicle since they need garage 
//				// for maintenance or for preparing for mission
//				if (!vehicle.isReserved()
//						|| !vehicle.isReservedForMaintenance()) {
//					if (vehicle instanceof Crewable crewableVehicle) {
//						if (crewableVehicle.getCrewNum() == 0 && crewableVehicle.getRobotCrewNum() == 0) {
//							i.remove();
//							handleParking(vehicle);
//						}
//						// else do not remove
//					} else {
//						// For LUV, always remove
//						i.remove();
//						handleParking(vehicle);
//					}
//				}
//			}
		}
		return valid;
	}


	/**
	 * Add a new parking location in the building.
	 * 
	 * @param position the relative position of the parking spot.
	 */
	protected void addRoverParkingLocation(LocalPosition position) {
		roverLocations.add(new RoverLocation(position));
	}

	/**
	 * Add a new utility vehicle parking location in the building.
	 * 
	 * @param position the relative position of the parking spot.
	 */
	protected void addUilityVehicleParkingLocation(LocalPosition position) {
		luvLocations.add(new UtilityVehicleLocation(position));
	}
	
	/**
	 * Add a new flyer parking location in the building.
	 * 
	 * @param position the relative position of the parking spot.
	 */
	protected void addFlyerLocation(LocalPosition position) {
		flyerLocations.add(new FlyerLocation(position));
	}
	
	/**
	 * Gets the parking location of a given parked vehicle.
	 * 
	 * @param vehicle the parked vehicle.
	 * @return the parking location or null if none.
	 */
	public RoverLocation getRoverParkedLocation(Vehicle vehicle) {
		RoverLocation result = null;
		Iterator<RoverLocation> i = roverLocations.iterator();
		while (i.hasNext()) {
			RoverLocation parkingLocation = i.next();
			if (vehicle.equals(parkingLocation.getParkedRover())) {
				result = parkingLocation;
			}
		}

		return result;
	}

	/**
	 * Gets the parking location of a given parked utility vehicle.
	 * 
	 * @param vehicle the parked utility vehicle.
	 * @return the parking location or null if none.
	 */
	public UtilityVehicleLocation getUtilityVehicleParkedLocation(Vehicle vehicle) {
		UtilityVehicleLocation result = null;
		Iterator<UtilityVehicleLocation> i = luvLocations.iterator();
		while (i.hasNext()) {
			UtilityVehicleLocation parkingLocation = i.next();
			if (vehicle.equals(parkingLocation.getParkedUtilityVehicle())) {
				result = parkingLocation;
			}
		}

		return result;
	}

	
	/**
	 * Gets the drone parking location of a given parked flyer.
	 * 
	 * @param flyer the parked flyer.
	 * @return the drone location or null if none.
	 */
	public FlyerLocation getFlyerParkedLocation(Flyer flyer) {
		FlyerLocation result = null;
		Iterator<FlyerLocation> i = flyerLocations.iterator();
		while (i.hasNext()) {
			FlyerLocation location = i.next();
			if (flyer.equals(location.getParkedFlyer())) {
				result = location;
			}
		}

		return result;
	}
	
	/**
	 * Gets an empty rover parking location.
	 * 
	 * @return empty parking location or null if none available.
	 */
	public RoverLocation getEmptyRoverParkingLocation() {
		RoverLocation result = null;

		// Get list of empty parking locations.
		List<RoverLocation> emptyLocations = new ArrayList<>(roverLocations.size());
		Iterator<RoverLocation> i = roverLocations.iterator();
		while (i.hasNext()) {
			RoverLocation parkingLocation = i.next();
			if (!parkingLocation.hasParkedVehicle()) {
				emptyLocations.add(parkingLocation);
			}
		}

		// Randomize empty parking locations and select one.
		if (!emptyLocations.isEmpty()) {
			Collections.shuffle(emptyLocations);
			result = emptyLocations.get(0);
		}

		return result;
	}

	/**
	 * Gets an empty utility vehicle parking location.
	 * 
	 * @return empty parking location or null if none available.
	 */
	public UtilityVehicleLocation getEmptyUtilityVehicleParkingLocation() {
		UtilityVehicleLocation result = null;

		// Get list of empty parking locations.
		List<UtilityVehicleLocation> emptyLocations = new ArrayList<>(luvLocations.size());
		Iterator<UtilityVehicleLocation> i = luvLocations.iterator();
		while (i.hasNext()) {
			UtilityVehicleLocation parkingLocation = i.next();
			if (!parkingLocation.hasParkedUtilityVehicle()) {
				emptyLocations.add(parkingLocation);
			}
		}

		// Randomize empty parking locations and select one.
		if (!emptyLocations.isEmpty()) {
			Collections.shuffle(emptyLocations);
			result = emptyLocations.get(0);
		}

		return result;
	}
	
	/**
	 * Gets an empty flyer parking location.
	 * 
	 * @return empty parking location or null if none available.
	 */
	public FlyerLocation getEmptyFlyerLocation() {
		FlyerLocation result = null;

		// Get list of empty parking locations.
		List<FlyerLocation> emptyLocations = new ArrayList<>(flyerLocations.size());
		Iterator<FlyerLocation> i = flyerLocations.iterator();
		while (i.hasNext()) {
			FlyerLocation location = i.next();
			if (!location.hasParkedFlyer()) {
				emptyLocations.add(location);
			}
		}

		// Randomize empty parking locations and select one.
		if (!emptyLocations.isEmpty()) {
			Collections.shuffle(emptyLocations);
			result = emptyLocations.get(0);
		}

		return result;
	}
	
	@Override
	public double getMaintenanceTime() {
		return roverLocations.size() * 5D;
	}

	@Override
	public void destroy() {
		super.destroy();

		rovers.clear();
		rovers = null;

		roverLocations.clear();
		roverLocations = null;
		
		flyerLocations.clear();
		flyerLocations = null;
	}

	/**
	 * Inner class to represent a parking location in the building.
	 */
	public class RoverLocation implements Serializable {

		private static final long serialVersionUID = 1L;
		
		// Data members
		private LocalPosition pos;
		private Rover rover;

		protected RoverLocation(LocalPosition pos) {
			this.pos = pos;
			rover = null;
		}

		public LocalPosition getPosition() {
			return pos;
		}

		public Rover getParkedRover() {
			return rover;
		}

		public boolean hasParkedVehicle() {
			return (rover != null);
		}

		protected void parkRover(Rover r) {
			rover = r;
		}

		protected void clearParking() {
			rover = null;
		}
	}
	
	/**
	 * Inner class to represent a parking location for the utility vehicles in the building.
	 */
	public class UtilityVehicleLocation implements Serializable {

		private static final long serialVersionUID = 1L;
		
		// Data members
		private LocalPosition pos;
		private LightUtilityVehicle luv;

		protected UtilityVehicleLocation(LocalPosition pos) {
			this.pos = pos;
			luv = null;
		}

		public LocalPosition getPosition() {
			return pos;
		}

		public Vehicle getParkedUtilityVehicle() {
			return luv;
		}

		public boolean hasParkedUtilityVehicle() {
			return (luv != null);
		}

		protected void parkUtilityVehicle(LightUtilityVehicle vehicle) {
			luv = vehicle;
		}

		protected void clearParking() {
			luv = null;
		}
	}
	
	/**
	 * Inner class to represent a flyer parking location in the building.
	 */
	public class FlyerLocation implements Serializable {

		private static final long serialVersionUID = 1L;
		
		// Data members
		private LocalPosition pos;
		private Flyer flyer;

		protected FlyerLocation(LocalPosition pos) {
			this.pos = pos;
			flyer = null;
		}

		public LocalPosition getPosition() {
			return pos;
		}

		public Flyer getParkedFlyer() {
			return flyer;
		}

		public boolean hasParkedFlyer() {
			return (flyer != null);
		}

		protected void parkFlyer(Flyer flyer) {
			this.flyer = flyer;
		}

		protected void clearParking() {
			flyer = null;
		}
	}
}
