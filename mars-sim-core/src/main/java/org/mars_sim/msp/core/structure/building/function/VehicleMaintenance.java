/**
 * Mars Simulation Project
 * VehicleMaintenance.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The VehicleMaintenance interface is a building function for a building
 * capable of maintaining vehicles.
 */
public abstract class VehicleMaintenance extends Function implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(VehicleMaintenance.class.getName());
	
	protected int vehicleCapacity;

	protected List<ParkingLocation> parkingLocations;
	private Collection<Vehicle> vehicles;

	/**
	 * Constructor.
	 * 
	 * @param function the name of the child function.
	 * @param building the building this function is for.
	 */
	public VehicleMaintenance(FunctionType function, Building building) {
		// Use Function constructor.
		super(function, building);

		vehicles = new ConcurrentLinkedQueue<Vehicle>();
		parkingLocations = new ArrayList<ParkingLocation>();
	}

	/**
	 * Gets the number of vehicles the building can accommodate.
	 * 
	 * @return vehicle capacity
	 */
	public int getVehicleCapacity() {
		return vehicleCapacity;
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
	 * Add vehicle to building if there's room.
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
		
		if (vehicles.size() >= vehicleCapacity) {
			logger.log(vehicle, Level.INFO, 1000,
				building + " already full.");
			return false;
		}

		// Add vehicle to building.
		if (vehicles.add(vehicle)) {
	
			if (vehicle instanceof Crewable) {
				// Transfer the human occupants to the settlement
				Crewable c = (Crewable)vehicle;
				for (Person p: new ArrayList<>(c.getCrew())) {
					p.transfer(building.getSettlement());
					BuildingManager.addPersonOrRobotToBuilding(p, building);
				}
				// Transfer the robot occupants to the settlement
				for (Robot r: new ArrayList<>(c.getRobotCrew())) {
					r.transfer(building.getSettlement());
					BuildingManager.addPersonOrRobotToBuilding(r, building);
				}
			}		
		
			vehicle.setPrimaryStatus(StatusType.GARAGED);
		
			// Update the vehicle's location state type
			vehicle.updateLocationStateType(LocationStateType.INSIDE_SETTLEMENT);
		
			// Put vehicle in assigned parking location within building.
			ParkingLocation location = getEmptyParkingLocation();
			LocalPosition newLoc;
			if (location != null) {
				newLoc = LocalAreaUtil.getLocalRelativePosition(location.getPosition(), getBuilding());
				location.parkVehicle(vehicle);
			} else {
				// Park vehicle in center point of building.
				newLoc = getBuilding().getPosition();
			}
	
			double newFacing = getBuilding().getFacing();
			vehicle.setParkedLocation(newLoc, newFacing);
	
			logger.fine(vehicle, "Added to " + building.getNickName() + " in " + building.getSettlement());
			
			return true;
		}
		
		return false;
	}

	/**
	 * Remove vehicle from garage building.
	 * 
	 * @param vehicle the vehicle to be removed.
	 * @return true if successfully removed
	 */
	public boolean removeVehicle(Vehicle vehicle) {
		if (!containsVehicle(vehicle)) {
			return false;
		}
		else {
			if (vehicles.remove(vehicle)) {

				if (vehicle instanceof Crewable) {
					// Remove the human occupants from the settlement
					// But is this needed ? These should already be in the Vehicle
					// if there are in the crew
					Crewable c = ((Crewable)vehicle);
					for (Person p: new ArrayList<>(c.getCrew())) {
						p.transfer(vehicle);
						BuildingManager.removePersonFromBuilding(p, building);
					}
					// Remove the robot occupants from the settlement
					for (Robot r: new ArrayList<>(c.getRobotCrew())) {
						r.transfer(vehicle);
						BuildingManager.removeRobotFromBuilding(r, building);
					}
				}
				
				ParkingLocation parkedLoc = getVehicleParkedLocation(vehicle);
				if (parkedLoc != null) {
					parkedLoc.clearParking();
				}
	
				vehicle.setPrimaryStatus(StatusType.PARKED);
				
				// Update the vehicle's location state type
				vehicle.updateLocationStateType(LocationStateType.WITHIN_SETTLEMENT_VICINITY);

				vehicle.findNewParkingLoc();
	
				logger.fine(vehicle, "Removed from " + building.getNickName() + " in " + building.getSettlement());
				
				return true;
			}
		}
		
		return false;
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
	 * Gets a collection of vehicles in the building.
	 * 
	 * @return Collection of vehicles in the building.
	 */
	public Collection<Vehicle> getVehicles() {
		return vehicles;
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
			for (Vehicle vehicle : vehicles) {
				if (!vehicle.isReserved()) {
					if (vehicle instanceof Crewable) {
						Crewable crewableVehicle = (Crewable) vehicle;
						if (crewableVehicle.getCrewNum() == 0 && crewableVehicle.getRobotCrewNum() == 0) {
							removeVehicle(vehicle);
						}
					} else {
						removeVehicle(vehicle);
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
		parkingLocations.add(new ParkingLocation(position));
	}

	/**
	 * Gets the parking location of a given parked vehicle.
	 * 
	 * @param vehicle the parked vehicle.
	 * @return the parking location or null if none.
	 */
	public ParkingLocation getVehicleParkedLocation(Vehicle vehicle) {
		ParkingLocation result = null;
		Iterator<ParkingLocation> i = parkingLocations.iterator();
		while (i.hasNext()) {
			ParkingLocation parkingLocation = i.next();
			if (vehicle.equals(parkingLocation.getParkedVehicle())) {
				result = parkingLocation;
			}
		}

		return result;
	}

	/**
	 * Gets an empty parking location.
	 * 
	 * @return empty parking location or null if none available.
	 */
	public ParkingLocation getEmptyParkingLocation() {
		ParkingLocation result = null;

		// Get list of empty parking locations.
		List<ParkingLocation> emptyLocations = new ArrayList<ParkingLocation>(parkingLocations.size());
		Iterator<ParkingLocation> i = parkingLocations.iterator();
		while (i.hasNext()) {
			ParkingLocation parkingLocation = i.next();
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

	@Override
	public double getMaintenanceTime() {
		return vehicleCapacity * 50D;
	}

	@Override
	public void destroy() {
		super.destroy();

		vehicles.clear();
		vehicles = null;

		parkingLocations.clear();
		parkingLocations = null;
	}

	/**
	 * Inner class to represent a parking location in the building.
	 */
	public class ParkingLocation implements Serializable {

		private static final long serialVersionUID = 1L;
		
		// Data members
		private LocalPosition pos;
		private Vehicle parkedVehicle;

		protected ParkingLocation(LocalPosition pos) {
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
}
