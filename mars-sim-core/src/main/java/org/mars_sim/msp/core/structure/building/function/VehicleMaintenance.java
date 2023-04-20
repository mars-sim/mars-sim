/*
 * Mars Simulation Project
 * VehicleMaintenance.java
 * @date 2022-09-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.data.UnitSet;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.FunctionSpec;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Flyer;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * The VehicleMaintenance interface is a building function for a building
 * capable of maintaining vehicles.
 */
public abstract class VehicleMaintenance extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(VehicleMaintenance.class.getName());

	protected List<VehicleLocation> parkingLocations;
	protected List<FlyerLocation> flyerLocations;
	
	private Collection<Vehicle> vehicles;
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

		vehicles = new UnitSet<>();
		flyers = new UnitSet<>();
		
		parkingLocations = new ArrayList<>();
		flyerLocations = new ArrayList<>();
	}

	/**
	 * Gets the number of vehicles the building can accommodate.
	 * 
	 * @return vehicle capacity
	 */
	public int getVehicleCapacity() {
		return parkingLocations.size();
	}

	/**
	 * Gets the current number of vehicles in the building.
	 * 
	 * @return number of vehicles
	 */
	public int getCurrentVehicleNumber() {
		return vehicles.size();
	}

	/**
	 * How many available locations unoccupied does the garage have?
	 * 
	 * @param Available parking locations.
	 */
	public int getAvailableCapacity() {
		return parkingLocations.size() - vehicles.size();
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
	 * Adds flyer to building if there's room for parking.
	 * 
	 * @param flyer the flyer to be added.
	 * @return true if flyer can be added.
	 */
	public boolean addFlyer(Flyer flyer) {

		// Check if flyer cannot be added to building.
		if (flyers.contains(flyer)) {
			logger.log(flyer, Level.INFO, 1000, 
				"Already garaged in " + building + ".");
			 return false;
		}
		
		if (flyers.size() >= flyerLocations.size()) {
			logger.log(flyer, Level.INFO, 1000,
				building + " already full.");
			return false;
		}

		if (flyers.add(flyer)) {
			
			// Put vehicle in assigned parking location within building.
			FlyerLocation location = getEmptyFlyerLocation();
			LocalPosition newLoc;
			
			if (location != null) {
				newLoc = LocalAreaUtil.getLocalRelativePosition(location.getPosition(), getBuilding());
				location.parkFlyer(flyer);
				
				// change the vehicle status
				flyer.setPrimaryStatus(StatusType.GARAGED);
				// Update the vehicle's location state type
				flyer.updateLocationStateType(LocationStateType.INSIDE_SETTLEMENT);
				
				double newFacing = getBuilding().getFacing();
				flyer.setFlyerLocation(newLoc, newFacing);
		
				logger.fine(flyer, "Added to " + building.getNickName() + " in " + building.getSettlement());
				
				return true;
			}
		}
		
		// can't add the flyer to a garage
		return false;
	}
		
	/**
	 * Adds vehicle to building if there's room for parking.
	 * 
	 * @param vehicle the vehicle to be added.
	 * @return true if vehicle can be added.
	 */
	public boolean addVehicle(Vehicle vehicle) {

		// Check if vehicle cannot be added to building.
		if (vehicles.contains(vehicle)) {
			logger.log(vehicle, Level.INFO, 1000, 
				"Already garaged in " + building + ".");
			 return false;
		}
		
		if (vehicles.size() >= parkingLocations.size()) {
			logger.log(vehicle, Level.INFO, 1000,
				building + " already full.");
			return false;
		}
		
		// Add vehicle to building.
		if (vehicles.add(vehicle)) {
	
			// Put vehicle in assigned parking location within building.
			VehicleLocation location = getEmptyParkingLocation();
			LocalPosition newLoc;
			
			if (location != null) {
				newLoc = LocalAreaUtil.getLocalRelativePosition(location.getPosition(), getBuilding());
				location.parkVehicle(vehicle);
				
				// change the vehicle status
				vehicle.setPrimaryStatus(StatusType.GARAGED);
				// Update the vehicle's location state type
				vehicle.updateLocationStateType(LocationStateType.INSIDE_SETTLEMENT);
				
				double newFacing = getBuilding().getFacing();
				vehicle.setParkedLocation(newLoc, newFacing);
		
				logger.fine(vehicle, "Added to " + building.getNickName() + " in " + building.getSettlement());
				
				return true;
			}
		}
		
		// can't add the vehicle to a garage
		return false;
	}

	/**
	 * Remove vehicle from garage building.
	 * 
	 * @param vehicle the vehicle to be removed.
	 * @return true if successfully removed
	 */
	public boolean removeVehicle(Vehicle vehicle, boolean transferCrew) {
		if (!containsVehicle(vehicle)) {
			return false;
		}

		// Note: Check if using Collection.remove() below is safe
		if (vehicles.remove(vehicle)) {
			
			if (transferCrew)
				handleCrew(vehicle);
			 
			handleParking(vehicle);

			logger.fine(vehicle, "Removed from " + building.getNickName() + " in " + building.getSettlement());
			
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
			 
			handleParking(flyer);

			logger.fine(flyer, "Removed from " + building.getNickName() + " in " + building.getSettlement());
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Handles the crew.
	 * 
	 * @param vehicle
	 */
	public void handleCrew(Vehicle vehicle) {
		
		if (vehicle instanceof Crewable) {
			// Remove the human occupants from the settlement
			// But is this needed ? These should already be in the Vehicle
			// if there are in the crew
			Crewable c = ((Crewable)vehicle);
			for (Person p: new ArrayList<>(c.getCrew())) {
				// If person's origin is already in this vehicle
				// and it's called by removeFromGarage()
				Vehicle v = p.getVehicle();
				if (v != null) {
					if (p.getVehicle().equals(vehicle)) {
						p.setContainerUnit(vehicle);
						p.setLocationStateType(LocationStateType.INSIDE_VEHICLE);
					}
					else {
						p.transfer(vehicle);
						BuildingManager.removePersonFromBuilding(p, building);
					}
				}
			}
			// Remove the robot occupants from the settlement
			for (Robot r: new ArrayList<>(c.getRobotCrew())) {
				Vehicle v = r.getVehicle();
				if (v != null) {
					if (r.getVehicle().equals(vehicle)) {
						r.setContainerUnit(vehicle);
						r.setLocationStateType(LocationStateType.INSIDE_VEHICLE);
					}
					else {
						r.transfer(vehicle);
						BuildingManager.removeRobotFromBuilding(r, building);
					}
				}
			}
		}
	}
	
	
	/**
	 * Handles the parking situation of the vehicle upon being removed from garage.
	 * 
	 * @param vehicle
	 */
	public void handleParking(Vehicle vehicle) {
		
		if (vehicle.getVehicleType() == VehicleType.DELIVERY_DRONE) {
			FlyerLocation loc = getFlyerParkedLocation((Flyer)vehicle);
			if (loc != null) {
				loc.clearParking();
			}
		}
		else {
			VehicleLocation parkedLoc = getVehicleParkedLocation(vehicle);
			if (parkedLoc != null) {
				parkedLoc.clearParking();
			}
		}

		vehicle.setPrimaryStatus(StatusType.PARKED);
		// Update the vehicle's location state type
		vehicle.updateLocationStateType(LocationStateType.WITHIN_SETTLEMENT_VICINITY);

		vehicle.findNewParkingLoc();
	}
	
	/**
	 * Checks if a vehicle is in the building.
	 * 
	 * @return true if vehicle is in the building.
	 */
	public boolean containsVehicle(Vehicle vehicle) {
		return vehicles.contains(vehicle);
	}

	/**
	 * Checks if a flyer is in the building.
	 * 
	 * @return true if flyer is in the building.
	 */
	public boolean containsFlyer(Flyer flyer) {
		return flyers.contains(flyer);
	}
	
	/**
	 * Gets a collection of vehicles in the building.
	 * 
	 * @return Collection of vehicles in the building.
	 */
	public Collection<Vehicle> getVehicles() {
		return vehicles;
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
			Iterator<Vehicle> i = vehicles.iterator();
			while (i.hasNext()) {
				Vehicle vehicle = i.next();
				// Do not touch any reserved vehicle since they need garage 
				// for maintenance or for preparing for mission
				if (!vehicle.isReserved()) {
					if (vehicle instanceof Crewable) {
						Crewable crewableVehicle = (Crewable) vehicle;
						if (crewableVehicle.getCrewNum() == 0 && crewableVehicle.getRobotCrewNum() == 0) {
							i.remove();
							handleParking(vehicle);
						}
					} else {
						i.remove();
						handleParking(vehicle);
					}
				}
			}
		}
		return valid;
	}


	/**
	 * Add a new parking location in the building.
	 * 
	 * @param position the relative position of the parking spot.
	 */
	protected void addParkingLocation(LocalPosition position) {
		parkingLocations.add(new VehicleLocation(position));
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
	public VehicleLocation getVehicleParkedLocation(Vehicle vehicle) {
		VehicleLocation result = null;
		Iterator<VehicleLocation> i = parkingLocations.iterator();
		while (i.hasNext()) {
			VehicleLocation parkingLocation = i.next();
			if (vehicle.equals(parkingLocation.getParkedVehicle())) {
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
	 * Gets an empty parking location.
	 * 
	 * @return empty parking location or null if none available.
	 */
	public VehicleLocation getEmptyParkingLocation() {
		VehicleLocation result = null;

		// Get list of empty parking locations.
		List<VehicleLocation> emptyLocations = new ArrayList<>(parkingLocations.size());
		Iterator<VehicleLocation> i = parkingLocations.iterator();
		while (i.hasNext()) {
			VehicleLocation parkingLocation = i.next();
			if (!parkingLocation.hasParkedVehicle()) {
				emptyLocations.add(parkingLocation);
			}
		}

		// Randomize empty parking locations and select one.
		if (emptyLocations.size() > 0) {
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
		if (emptyLocations.size() > 0) {
			Collections.shuffle(emptyLocations);
			result = emptyLocations.get(0);
		}

		return result;
	}
	
	@Override
	public double getMaintenanceTime() {
		return parkingLocations.size() * 50D;
	}

	@Override
	public void destroy() {
		super.destroy();

		vehicles.clear();
		vehicles = null;

		parkingLocations.clear();
		parkingLocations = null;
		
		flyerLocations.clear();
		flyerLocations = null;
	}

	/**
	 * Inner class to represent a parking location in the building.
	 */
	public class VehicleLocation implements Serializable {

		private static final long serialVersionUID = 1L;
		
		// Data members
		private LocalPosition pos;
		private Vehicle parkedVehicle;

		protected VehicleLocation(LocalPosition pos) {
			this.pos = pos;
			parkedVehicle = null;
		}

		public LocalPosition getPosition() {
			return pos;
		}

		public Vehicle getParkedVehicle() {
			return parkedVehicle;
		}

		public boolean hasParkedVehicle() {
			return (parkedVehicle != null);
		}

		protected void parkVehicle(Vehicle vehicle) {
			parkedVehicle = vehicle;
		}

		protected void clearParking() {
			parkedVehicle = null;
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
