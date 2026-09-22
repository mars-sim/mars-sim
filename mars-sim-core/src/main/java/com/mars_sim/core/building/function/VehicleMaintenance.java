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
import com.mars_sim.core.person.ai.task.util.Worker;
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
	
	// Indoor parking locations for 3 types of vehicles
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
				VehicleMaintenance maintFunction = building.getVehicleMaintenance();
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
     * Gets the value of this function.
     * 
     * @return value (VP) of building function.
     */
    public double getFunctionValue() {

        // Settlements need enough recreation buildings to support population.
        double demand = getSettlement().getNumCitizens();
    	
		// Demand is one ground vehicle capacity for every ground vehicles.
		demand += getSettlement().getOwnedVehicleNum();
  
        double wearModifier = ((getBuilding().getMalfunctionManager().getWearCondition() / 100D) * .75D) + .25D;
        
        double supply = (getRoverCapacity() + .25 * getUtilityVehicleCapacity() + .5 * getFlyerCapacity()) * wearModifier;
        
        return demand / (supply + 1D);
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

		ParkingLocation<T> location = getAssignedLocation(locations, newVehicle);
		
		if (location != null) {
			logger.log(newVehicle, Level.INFO, 1000,  "Already garaged in " + building + ".");
			return true;
		}
		
		// Put vehicle in assigned parking location within building.
		location = getEmptyLocation(locations);
		if (location == null) {
			logger.log(newVehicle, Level.INFO, 1000, building + ": No empty parking location found.");	
			return false;
		}
		
		// If parking spot is found
		
		location.parkVehicle(newVehicle);
		
		// Change the vehicle status
		newVehicle.setPrimaryStatus(StatusType.GARAGED);
		
		// Relocate/transfer the crew to the garage.
		if (transferCrew)
			relocateCrewToGarage(newVehicle);
		
		double newFacing = getBuilding().getFacing();
		
		newVehicle.updateCrewLocation(location.getPosition(), newFacing);

		// Directly update the states
		newVehicle.setContainerUnit(newVehicle.getSettlement());
		
		// Fire the unit update
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
		else if (vehicle instanceof LightUtilityVehicle luv 
				&& !luv.hasNoCrew()) {
			Worker occupant = luv.getOccupant();
            occupant.transfer(building);
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
		// Set the parking spot to null
		found.parkVehicle(null);
		// Park it in vicinity
		parkInVicinity(oldVehicle);
		// Directly update the states
		oldVehicle.setContainerUnit(oldVehicle.getSettlement());
		// Fire the unit update
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
		else if (vehicle instanceof LightUtilityVehicle luv 
				&& !luv.hasNoCrew()) {
			Worker occupant = luv.getOccupant();
            occupant.transfer(vehicle);
		}
	}

	
	/**
	 * Parks the vehicle in settlement vicinity upon being removed from garage.
	 * 
	 * @param vehicle
	 */
	private void parkInVicinity(Vehicle vehicle) {
		// Set the primary status to PARKED
		vehicle.setPrimaryStatus(StatusType.PARKED);
		// Find a new parking location
		vehicle.findNewParkingLoc();
	}
	
	/**
	 * Checks if a rover is in the building.
	 * 
	 * @param v
	 * @return true if rover is in the building.
	 */
	public boolean containsRover(Rover v) {
		return roverLocations.stream()
				.anyMatch(loc -> v.equals(loc.getVehicle()));
	}

	/**
	 * Checks if a LUV is in the building.
	 * 
	 * @param v
	 * @return true if LUV is in the building.
	 */
	public boolean containsUtilityVehicle(LightUtilityVehicle v) {
		return luvLocations.stream()
				.anyMatch(loc -> v.equals(loc.getVehicle()));
	}
	
	/**
	 * Checks if a flyer is in the building.
	 * 
	 * @param v
	 * @return true if flyer is in the building.
	 */
	public boolean containsFlyer(Flyer v) {
		return flyerLocations.stream()
				.anyMatch(loc -> v.equals(loc.getVehicle()));
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
	 * Gets the assigned parking Location of a vehicle.
	 * 
	 * @param v the parked Vehicle.
	 * @return Location or null if none.
	 */
	private <T extends Vehicle> ParkingLocation<T> getAssignedLocation(List<ParkingLocation<T>> potential, T v) {
		return potential.stream()
			// Use v first as other since could be null
			.filter(loc -> v.equals(loc.getVehicle()))
			.findAny().orElse(null);
	}
	
	/**
	 * Finds an empty unused vehicle Location.
	 * 
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
		return 2.5 * roverLocations.size() + flyerLocations.size() + 1.5 * luvLocations.size();
	}

    
	/**
	 * Inner class to represent a parking location for vehicles in the building.
	 */
	private static class ParkingLocation<T extends Vehicle> implements Serializable {

		private static final long serialVersionUID = 1L;
		
		private String name;
		private LocalPosition pos;
		private T parkedVehicle;

		private ParkingLocation(String name, LocalPosition pos) {
			this.name = name;
			this.pos = pos;
			parkedVehicle = null;
		}

		public String getName() {
			return name;
		}

		public LocalPosition getPosition() {
			return pos;
		}

		public T getVehicle() {
			return parkedVehicle;
		}

		public boolean hasParkedVehicle() {
			return (parkedVehicle != null);
		}

		protected void parkVehicle(T vehicle) {
			parkedVehicle = vehicle;
		}
	}
}
