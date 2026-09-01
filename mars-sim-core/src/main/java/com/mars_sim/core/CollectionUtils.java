/*
 * Mars Simulation Project
 * CollectionUtils.java
 * @date 2026-08-07
 * @author Sebastien Venot
 */
package com.mars_sim.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.data.UnitSet;
import com.mars_sim.core.data.collection.DataCollectionSite;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleType;

/**
 * This class provides general collection manipulation convenience methods.
 */
public class CollectionUtils {

	private static UnitManager unitManager = Simulation.instance().getUnitManager();
	private static SimulationConfig simulationConfig = SimulationConfig.instance();

	private CollectionUtils() {
		// nothing
	}

	/**
	 * Gets the base mass of a vehicle type.
	 * 
	 * @param vehicleType
	 * @return
	 */
	public static double getVehicleTypeBaseMass(VehicleType vehicleType) {
		if (simulationConfig == null)
			simulationConfig = SimulationConfig.instance();
		
		return simulationConfig.getVehicleConfiguration().getVehicleSpec(vehicleType.getName()).getEmptyMass();
	}
	
	/**
	 * Finds a nearby vehicle based on its coordinates.
	 *
	 * @param c {@link Coordinates}
	 * @return
	 */
	public static Vehicle findVehicle(Coordinates c) {
		// Use LocationTag's findSettlementVicinity() for faster search
		// if the unit is known
		
		if (unitManager == null)
			unitManager = Simulation.instance().getUnitManager();
	
		Collection<Vehicle> list = unitManager.getVehicles();
		for (Vehicle v : list) {
			if (v.getCoordinates().equals(c) || v.getCoordinates() == c)
				return v;
		}

		return null;
	}
	
	/**
	 * Finds a nearby settlement based on its coordinates.
	 *
	 * @param c {@link Coordinates}
	 * @return
	 */
	public static Settlement findSettlement(Coordinates c) {
		if (unitManager == null)
			unitManager = Simulation.instance().getUnitManager();

		return unitManager.findSettlement(c);
	}

	/**
	 * Gets a list of people of a settlement in its vicinity.
	 * Note: a person can be either inside the settlement or within its vicinity
	 *
	 * @param settlement the settlement
	 * @param isCitizen are these people associated with this settlement
	 * @return list of people to display.
	 */
	public static List<Person> getPeopleInSettlementVicinity(Settlement settlement, boolean isCitizen) {
		if (unitManager == null)
			unitManager = Simulation.instance().getUnitManager();

		List<Person> result = new ArrayList<>();

		if (settlement != null) {
			if (isCitizen) {
				// Faster search with less people
				// if isCitizen is true, then it's required that this person must be the same as 
				// this settlement
				Iterator<Person> i = settlement.getAllAssociatedPeople().iterator();
				while (i.hasNext()) {
					Person person = i.next();
					if (person.getAssociatedSettlement().equals(settlement)
							&& !person.getPhysicalCondition().isDead()) {

						// Select a person that is at the settlement location.
						Coordinates personLoc = person.getCoordinates();
						if (personLoc != null && personLoc.equals(settlement.getCoordinates())) {
							result.add(person);
						}
					}
				}
			}
			else {
				Iterator<Person> i = unitManager.getPeople().iterator();
				while (i.hasNext()) {
					Person person = i.next();
					
					// If if isCitizen is false, then it's not a citizen, then this person doesn't have to the same as  
					// this settlement, but the coordinates must still be the same as this settlement
					if (!person.getPhysicalCondition().isDead()) {
						// Select a person that is at the settlement location.
						Coordinates personLoc = person.getCoordinates();
						if (personLoc != null && personLoc.equals(settlement.getCoordinates())) {
							result.add(person);
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Gets a list of people of a vehicle's vicinity.
	 *
	 * @param Vehicle
	 * @param isCitizen are these people associated with this vehicle's associated settlement
	 * @return list of people to display.
	 */
	public static List<Person> getPeopleInVehicleVicinity(Vehicle vehicle, boolean isCitizen) {
		if (unitManager == null)
			unitManager = Simulation.instance().getUnitManager();

		List<Person> result = new ArrayList<>();

		if (vehicle != null) {
			if (isCitizen) {
				// Faster search with less people
				Settlement vehicleSettlement = vehicle.getAssociatedSettlement();
				// if isCitizen is true, then it's required that this person must be the same as 
				// this vehicle's associated settlement
				Iterator<Person> i = vehicleSettlement.getAllAssociatedPeople().iterator();
				while (i.hasNext()) {
					Person person = i.next();
					if (person.getAssociatedSettlement().equals(vehicleSettlement)
							&& !person.getPhysicalCondition().isDead()) {

						// Select a person that is at the settlement location.
						Coordinates personLoc = person.getCoordinates();
						if (personLoc != null && personLoc.equals(vehicle.getCoordinates())) {
							result.add(person);
						}
					}
				}
			}
			else {
				Iterator<Person> i = unitManager.getPeople().iterator();
				while (i.hasNext()) {
					Person person = i.next();
					
					// If if isCitizen is false, then it's not a citizen, then this person doesn't 
					// have to be the same as this vehicle's associated settlement, but the 
					// coordinates must still be the same as this vehicle's associated settlement
					if (!person.getPhysicalCondition().isDead()) {
						// Select a person that is at the settlement location.
						Coordinates personLoc = person.getCoordinates();
						if (personLoc != null && personLoc.equals(vehicle.getCoordinates())) {
							result.add(person);
						}
					}
				}
			}
		}


		return result;
	}
	/**
	 * Gets a set of other people that are NOT on this settlement.
	 * Note: a person can be either inside the settlement or within its vicinity
	 *
	 * @param settlement the settlement
	 * @return
	 */
	public static Set<Person> getOtherPeople(Settlement settlement) {
		if (unitManager == null)
			unitManager = Simulation.instance().getUnitManager();
		
		Set<Person> group0 = new UnitSet<>();
		group0.addAll(unitManager.getPeople());
		Set<Person> group1 = new UnitSet<>();
		group1.addAll(settlement.getAllAssociatedPeople());
		
		group0.removeAll(group1);
		return group0;
	}
	
	/**
	 * Gets all the data collection sites in the simulation.
	 * 
	 * @return
	 */
	public static Collection<DataCollectionSite> getDataCollectionSites() {
		if (unitManager == null)
			unitManager = Simulation.instance().getUnitManager();
	
		Set<DataCollectionSite> sites = new HashSet<>();
		
		Collection<Settlement> settlements = unitManager.getSettlements();
		for (Settlement s: settlements) {
			sites.addAll(s.getAllDataCollectionSites());
		}
		
		return sites;		
	}
	
	/**
	 * Gets a list of robots associated people of a settlement in its vicinity.
 	 * Note: a robot can be either inside the settlement or within its vicinity
	 * 
	 * @param settlement the settlement
	 * @return list of robots to display.
	 */
	public static Collection<Robot> getAssociatedRobotsInSettlementVicinity(Settlement settlement) {
		// In near future, track robots in vicinity and indoor.
		return settlement.getAllAssociatedRobots();
	}
	
	/**
	 * Gets the total number of parts from all settlements
	 *
	 * @param id
	 * @return
	 */
	public static int getTotalNumPart(int id) {
		if (unitManager == null)
			unitManager = Simulation.instance().getUnitManager();
		
		int result = 0;
		// Obtain the total # of this part in used from all settlements
		for (Settlement s : unitManager.getSettlements()) {
			int num = s.getEquipmentInventory().getItemResourceStored(id);
			result += num;
		}

		return result;
	}
}
