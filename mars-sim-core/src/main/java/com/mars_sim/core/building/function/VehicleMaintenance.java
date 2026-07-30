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
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.config.FunctionSpec;
import com.mars_sim.core.building.config.VehicleMaintenanceSpec;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.Flyer;
import com.mars_sim.core.vehicle.LightUtilityVehicle;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * The VehicleMaintenance interface is a building function for a building
 * capable of maintaining vehicles.
 */
public class VehicleMaintenance extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(VehicleMaintenance.class.getName());

	// Event type for when a vehicle is garaged in the building.
	public static final String GARAGED = "garaged";
	
	private List<ParkingLocation<Rover>> roverLocations;
	private List<ParkingLocation<LightUtilityVehicle>> luvLocations;
	private List<ParkingLocation<Flyer>> flyerLocations;
	
	/**
	 * Constructor.
	 * 
	 * @param function the name of the child function.
	 * @param building the building this function is for.
	 */
	public VehicleMaintenance(Building building, FunctionSpec spec) {
		// Use Function constructor.
		super(FunctionType.VEHICLE_MAINTENANCE, spec, building);
		
		VehicleMaintenanceSpec vms = (VehicleMaintenanceSpec) spec;
		
		roverLocations = vms.getRoverParking().stream()
							.map(p -> new ParkingLocation<Rover>(p.name(), p.position().toPosition(building)))
							.toList();
		flyerLocations = vms.getFlyerParking().stream()
							.map(p -> new ParkingLocation<Flyer>(p.name(), p.position().toPosition(building)))
							.toList();
		luvLocations = vms.getUtilityParking().stream()
							.map(p -> new ParkingLocation<LightUtilityVehicle>(p.name(), p.position().toPosition(building)))
							.toList();
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
	 * How many available rover locations unoccupied does the garage have?
	 * 
	 * @param Available rover parking locations.
	 */
	public int getAvailableRoverCapacity() {
		return (int)roverLocations.stream().filter(p -> !p.hasParkedVehicle()).count();
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
	 * How many available luv locations unoccupied does the garage have?
	 * 
	 * @param Available luv parking locations.
	 */
	public int getAvailableUtilityVehicleCapacity() {
		return (int)luvLocations.stream().filter(p -> !p.hasParkedVehicle()).count();
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
	 * How many available flyer locations unoccupied does the garage have?
	 * 
	 * @param Available flyer locations.
	 */
	public int getAvailableFlyerCapacity() {
		return (int)flyerLocations.stream().filter(p -> !p.hasParkedVehicle()).count();
	}

	/**
	 * Assigns an available parking location.
	 * 
	 * @param locations
	 * @param newVehicle
	 * @return
	 */
	public <T extends Vehicle> ParkingLocation<T> assignParkingLocation(List<ParkingLocation<T>> locations, T newVehicle) {
		if (getAssignedLocation(locations, newVehicle) != null) {
			logger.log(newVehicle, Level.INFO, 1000,  "Already garaged in " + building + ".");
			return null;
		}
		// Put vehicle in assigned parking location within building.
		ParkingLocation<T> location = getEmptyLocation(locations);
		if (location == null) {
			logger.log(newVehicle, Level.INFO, 1000, building + " already full.");
			return null;
		}
		
		return location;
	}
	
	/**
	 * Add the vehicle to a parking location and optionally relocate the crew.
	 * 
	 * @param locations
	 * @param newVehicle
	 * @param transferCrew
	 * @return
	 */
	private <T extends Vehicle> boolean addVehicle(List<ParkingLocation<T>> locations, T newVehicle, boolean transferCrew) {
		if (newVehicle == null) {
			logger.log(newVehicle, Level.INFO, 1000, building + "Vehicle cannot be null.");
			return false;
		}

		ParkingLocation<T> location = assignParkingLocation(locations, newVehicle);
		
		if (location == null) {
			logger.log(newVehicle, Level.INFO, 1000, building + " has no empty parking location.");	
			return false;
		}
		
		location.parkVehicle(newVehicle);
		
		// Change the vehicle status
		newVehicle.setPrimaryStatus(StatusType.GARAGED);
		
		// Relocate/transfer the crew to the garage.
		if (transferCrew)
			relocateCrewToGarage(newVehicle);
		
		// Update the vehicle's location state type
//		newVehicle.setLocationStateType(LocationStateType.INSIDE_SETTLEMENT);
		
		double newFacing = getBuilding().getFacing();
		newVehicle.setParkedLocation(location.getPosition(), newFacing);

		getBuilding().fireUnitUpdate(GARAGED);
		return true;
	}

	
	/**
	 * Relocates the crew to the garage building.
	 * 
	 * @param vehicle
	 */
	public void relocateCrewToGarage(Vehicle vehicle) {
		
		if (vehicle instanceof Crewable c) {
			for (Person p: new ArrayList<>(c.getCrew())) {
				p.transfer(building);
			}
			for (Robot r: new ArrayList<>(c.getRobotCrew())) {
				r.transfer(building);
			}
		}
	}
	

	/**
	 * Adds vehicle to building if there's room for parking.
	 * 
	 * @param vehicle the vehicle to be added.
	 * @param transferCrew
	 * @return true if vehicle can be added.
	 */
	public boolean addRover(Rover rover, boolean transferCrew) {
		return addVehicle(roverLocations, rover, transferCrew);
	}
	
	/**
	 * Adds vehicle to building if there's room for parking.
	 * 
	 * @param vehicle the vehicle to be added.
	 * @param transferCrew
	 * @return true if vehicle can be added.
	 */
	public boolean addUtilityVehicle(LightUtilityVehicle luv, boolean transferCrew) {
		return addVehicle(luvLocations, luv, transferCrew);
	}

	/**
	 * Adds flyer to building if there's room for parking.
	 * 
	 * @param flyer the flyer to be added.
	 * @param transferCrew
	 * @return true if flyer can be added.
	 */
	public boolean addFlyer(Flyer flyer, boolean transferCrew) {
		return addVehicle(flyerLocations, flyer, transferCrew);
	}

	
	/**
	 * Remove a rover from garage building.
	 * 
	 * @param rover the rover to be removed.
	 * @param transferCrew
	 * @return true if successfully removed
	 */
	public boolean removeRover(Rover rover, boolean transferCrew) {
		return removeVehicle(roverLocations, rover, transferCrew);
	}

	/**
	 * Remove a vehicle from a parking location and optionally transfer the crew.
	 * 
	 * @param locations
	 * @param oldVehicle
	 * @param transferCrew
	 * @return
	 */
	private <T extends Vehicle> boolean removeVehicle(List<ParkingLocation<T>> locations, T oldVehicle, boolean transferCrew) {
		if (oldVehicle == null) {
			throw new IllegalArgumentException("Vehicle cannot be null.");
		}

		var found = getAssignedLocation(locations, oldVehicle);
		if (found == null) {
			return false;
		}

		// Relocate/transfer the crew to the garage.
		if (transferCrew)
			relocateCrewToVehicle(oldVehicle);
		
		found.parkVehicle(null);
		parkInVicinity(oldVehicle);

		getBuilding().fireUnitUpdate(GARAGED);
			
		return true;
	}
	
	/**
	 * Remove a utility vehicle from garage building.
	 * 
	 * @param vehicle the vehicle to be removed.
	 * @return true if successfully removed
	 */
	public boolean removeUtilityVehicle(LightUtilityVehicle luv, boolean transferCrew) {
		return removeVehicle(luvLocations, luv, transferCrew);
	}
	
	/**
	 * Remove flyer from garage building.
	 * 
	 * @param flyer the flyer to be removed
	 * @param transferCrew
	 * @return true if successfully removed
	 */
	public boolean removeFlyer(Flyer flyer, boolean transferCrew) {
		return removeVehicle(flyerLocations, flyer, transferCrew);
	}
	
	/**
	 * Relocates the crew.
	 * 
	 * @param vehicle
	 */
	public void relocateCrewToVehicle(Vehicle vehicle) {

		if (vehicle instanceof Crewable c) {
			for (Person p: new ArrayList<>(c.getCrew())) {
				p.transfer(vehicle);
			}
			for (Robot r: new ArrayList<>(c.getRobotCrew())) {
				r.transfer(vehicle);
			}
		}
	}

	
	/**
	 * Parks the vehicle in settlement vicinity upon being removed from garage.
	 * 
	 * @param vehicle
	 */
	private void parkInVicinity(Vehicle vehicle) {
		vehicle.setPrimaryStatus(StatusType.PARKED);
		// Update the vehicle's location state type
//		vehicle.setLocationStateType(LocationStateType.SETTLEMENT_VICINITY);
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
		return getAssignedLocation(roverLocations, rover) != null;
	}

	/**
	 * Checks if a LUV is in the building.
	 * 
	 * @param luv
	 * @return true if LUV is in the building.
	 */
	public boolean containsUtilityVehicle(LightUtilityVehicle luv) {
		return getAssignedLocation(luvLocations, luv) != null;

	}
	
	/**
	 * Checks if a flyer is in the building.
	 * 
	 * @param flyer
	 * @return true if flyer is in the building.
	 */
	public boolean containsFlyer(Flyer flyer) {
		return getAssignedLocation(flyerLocations, flyer) != null;
	}
	
	/**
	 * Gets a collection of rovers in the building.
	 * 
	 * @return Collection of rovers in the building.
	 */
	public Collection<Rover> getRovers() {
		return roverLocations.stream()
					.filter(p -> p.hasParkedVehicle())
					.map(p -> p.getVehicle())
					.toList();
	}

	/**
	 * Gets a collection of luvs in the building.
	 * 
	 * @return Collection of luvs in the building.
	 */
	public Collection<LightUtilityVehicle> getUtilityVehicles() {
		return luvLocations.stream()
					.filter(p -> p.hasParkedVehicle())
					.map(p -> p.getVehicle())
					.toList();
	}
	
	/**
	 * Gets a collection of flyers in the building.
	 * 
	 * @return Collection of flyers in the building.
	 */
	public Collection<Flyer> getFlyers() {
		return flyerLocations.stream()
					.filter(p -> p.hasParkedVehicle())
					.map(p -> p.getVehicle())
					.toList();
	}

	/**
	 * Gets the parking Location of a specific Vehicle.
	 * 
	 * @param v the parked Vehicle.
	 * @return Location or null if none.
	 */
	private <T extends Vehicle> ParkingLocation<T> getAssignedLocation(List<ParkingLocation<T>> potential, T v) {
		return potential.stream()
			// Use v first as other since could be null
			.filter(p -> v.equals(p.getVehicle()))
			.findAny().orElse(null);
	}
	
	/**
	 * Find an empty unused Vehicle Location.
	 * @param <T>
	 * @param potential
	 * @return
	 */
	private <T extends Vehicle> ParkingLocation<T> getEmptyLocation(List<ParkingLocation<T>> potential) {
		List<ParkingLocation<T>> empty = potential.stream()
										.filter(p -> !p.hasParkedVehicle())
										.toList();
		
		return RandomUtil.getRandomElement(empty);
	}
	
	@Override
	public double getMaintenanceTime() {
		return roverLocations.size() * 5D;
	}
	
	
	/**
	 * Gets the value of the function for a named building.
	 * 
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
		Iterator<Building> i = settlement.getBuildingManager().getBuildingSet(FunctionType.VEHICLE_MAINTENANCE).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
				removedBuilding = true;
			}
			else {
				VehicleMaintenance maintFunction = building.getVehicleParking();
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += maintFunction.getRoverCapacity() * wearModifier;
			}
		}

		double vehicleCapacityValue = demand / (supply + 1D);

		var spec = (VehicleMaintenanceSpec) buildingConfig.getFunctionSpec(buildingName, FunctionType.VEHICLE_MAINTENANCE);
		double roverCapacity = spec.getRoverParking().size() + 1.0;

		double luvCapacity = spec.getUtilityParking().size() + 1.0;
		
		double flyerCapacity = spec.getFlyerParking().size() + 1.0;
		
		return (roverCapacity + luvCapacity + flyerCapacity) * vehicleCapacityValue;
	}

	/**
	 * Inner class to represent a parking location for vehicles in the building.
	 */
	private static class ParkingLocation<T extends Vehicle> implements Serializable {

		private static final long serialVersionUID = 1L;
		
		private String name;
		private LocalPosition pos;
		private T parked;

		private ParkingLocation(String name, LocalPosition pos) {
			this.name = name;
			this.pos = pos;
			parked = null;
		}

		public String getName() {
			return name;
		}

		public LocalPosition getPosition() {
			return pos;
		}

		public T getVehicle() {
			return parked;
		}

		public boolean hasParkedVehicle() {
			return (parked != null);
		}

		protected void parkVehicle(T vehicle) {
			parked = vehicle;
		}
	}
}
