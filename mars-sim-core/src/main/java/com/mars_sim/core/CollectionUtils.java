/*
 * Mars Simulation Project
 * CollectionUtils.java
 * @date 2025-08-27
 * @author Sebastien Venot
 */
package com.mars_sim.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import com.mars_sim.core.data.UnitSet;
import com.mars_sim.core.equipment.Equipment;
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
	
	public static Collection<Equipment> getEquipment(
		Collection<Unit> units
	) {
		return units
				.stream()
				.filter(u -> UnitType.CONTAINER == u.getUnitType()
							|| UnitType.EVA_SUIT == u.getUnitType())
				.map(Equipment.class::cast)
				.filter(u -> !u.isSalvaged())
				.collect(Collectors.toList());
	}

	
	public static Collection<Robot> getRobot(
		Collection<Unit> units
	) {

		return units
				.stream()
				.filter(u-> UnitType.ROBOT == u.getUnitType())
				.map(Robot.class::cast)
				.collect(Collectors.toList());
	}

	/**
	 * Gets the base mass of a vehicle type.
	 * 
	 * @param vehicleType
	 * @return
	 */
	public static double getVehicleTypeBaseMass(VehicleType vehicleType) {
		return simulationConfig.getVehicleConfiguration().getVehicleSpec(vehicleType.getName()).getEmptyMass();
	}
	
	
	/**
	 * Finds the settlement's unique id based on its name
	 *
	 * @param name
	 * @return
	 */
	public static int findSettlementID(String name) {
		if (unitManager == null)
			unitManager = Simulation.instance().getUnitManager();

		Collection<Settlement> ss = unitManager.getSettlements();
		for (Settlement s : ss) {
			if (s.getName().equals(name))
				return s.getIdentifier();
		}

		return -1;
	}

	/**
	 * Finds the settlement instance based on its name
	 *
	 * @param name
	 * @return
	 */
	public static Settlement findSettlement(String name) {
		if (unitManager == null)
			unitManager = Simulation.instance().getUnitManager();

		Collection<Settlement> ss = unitManager.getSettlements();
		for (Settlement s : ss) {
			if (s.getName().equals(name))
				return s;
		}

		return null;
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
	 * Sorts by name.
	 * 
	 * @param <T>
	 * @param collection
	 * @return
	 */
	public static <T extends Unit> Collection<T> sortByName(Collection<T> collection) {
		ConcurrentSkipListSet<T> sorted = new ConcurrentSkipListSet<>(
				(o1, o2) -> o1.getName().compareTo(o2.getName()));

		sorted.addAll(collection);
		return sorted;
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

		List<Person> result = new ArrayList<>();

		if (settlement != null) {
			Iterator<Person> i = unitManager.getPeople().iterator();
			while (i.hasNext()) {
				Person person = i.next();
				
				// if isCitizen is true, then it's required that this person must be associated with 
				// this settlement
				// If if isCitizen is false, then it's not required
				if ((!isCitizen || person.getAssociatedSettlement().equals(settlement)) 
					&& !person.getPhysicalCondition().isDead()) {

					// Select a person that is at the settlement location.
					Coordinates personLoc = person.getCoordinates();
					if (personLoc != null && personLoc.equals(settlement.getCoordinates())) {
						result.add(person);
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

		Set<Person> group0 = new UnitSet<>();
		group0.addAll(unitManager.getPeople());
		Set<Person> group1 = new UnitSet<>();
		group1.addAll(settlement.getAllAssociatedPeople());
		
		group0.removeAll(group1);
		return group0;
	}
	
	/**
	 * Gets a list of vehicles (regardless their association) of a settlement in its vicinity.
	 * Note: a vehicle can be either inside the settlement or within its vicinity
	 *
	 * @param settlement the settlement
	 * @return list of vehicles to display.
	 */
	public static List<Vehicle> getVehiclesInSettlementVicinity(Settlement settlement) {

		List<Vehicle> result = new ArrayList<>();

		if (settlement != null) {
			Iterator<Vehicle> i = unitManager.getVehicles().iterator();
			while (i.hasNext()) {
				Vehicle vehicle = i.next();
				Settlement settlementLoc = vehicle.getSettlement();
				// Select a vehicle at the settlement coordinate.
				if (settlementLoc != null && settlementLoc.equals(settlement)) {
					result.add(vehicle);
				}
			}
		}

		return result;
	}
	
	/**
	 * Gets a list of robots associated people of a settlement in its vicinity.
 	 * Note: a robot can be either inside the settlement or within its vicinity
	 * 
	 * @param settlement the settlement
	 * @return list of robots to display.
	 */
	public static List<Robot> getAssociatedRobotsInSettlementVicinity(Settlement settlement) {
		return new ArrayList<>(settlement.getAllAssociatedRobots());
	}
	
	/**
	 * Gets the total number of parts from all settlements
	 *
	 * @param id
	 * @return
	 */
	public static int getTotalNumPart(int id) {
		int result = 0;
		// Obtain the total # of this part in used from all settlements
		for (Settlement s : unitManager.getSettlements()) {
			int num = s.getItemResourceStored(id);
			result += num;
		}

		return result;
	}
}
