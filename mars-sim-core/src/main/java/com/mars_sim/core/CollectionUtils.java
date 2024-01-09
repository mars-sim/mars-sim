/*
 * Mars Simulation Project
 * CollectionUtils.java
 * @date 2022-06-24
 * @author Sebastien Venot
 */
package com.mars_sim.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import com.mars_sim.core.data.UnitSet;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleType;
import com.mars_sim.mapdata.location.Coordinates;

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
	
	public static <T extends Unit> Collection<T> sortByName(
		Collection<T> collection
	) {
		ConcurrentSkipListSet<T> sorted = new ConcurrentSkipListSet<>(
			new Comparator<>() {
				@Override
				public int compare(T o1, T o2) {
					return o1.getName().compareToIgnoreCase(o2.getName());
				}
			}
		);
		sorted.addAll(collection);
		return sorted;
	}

	/**
	 * Gets a list of associated people of a settlement in its vicinity.
	 * Note: a person can be either inside the settlement or within its vicinity
	 *
	 * @param settlement the settlement
	 * @return list of people to display.
	 */
	public static List<Person> getAssociatedPeopleInSettlementVicinity(Settlement settlement) {

		List<Person> result = new ArrayList<>();

		if (settlement != null) {
			Iterator<Person> i = settlement.getAllAssociatedPeople().iterator();
			while (i.hasNext()) {
				Person person = i.next();
				// Only select living people.
				if (!person.getPhysicalCondition().isDead()) {

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
	 * Gets a list of people (regardless their association) of a settlement in its vicinity.
	 * Note: a person can be either inside the settlement or within its vicinity
	 *
	 * @param settlement the settlement
	 * @return list of people to display.
	 */
	public static List<Person> getPeopleInSettlementVicinity(Settlement settlement) {

		List<Person> result = new ArrayList<>();

		if (settlement != null) {
			Iterator<Person> i = unitManager.getPeople().iterator();
			while (i.hasNext()) {
				Person person = i.next();
				// Only select living people.
				if (!person.getPhysicalCondition().isDead()) {

					// Select a person that is at the settlement coordinate.
					if (person.getCoordinates().equals(settlement.getCoordinates())) {
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
				// Select a vehicle at the settlement coordinate.
				if (vehicle.getCoordinates().equals(settlement.getCoordinates())) {
					result.add(vehicle);
				}
			}
		}

		return result;
	}
	
	/**
	 * Gets a list of robots associated people of a settlement in its vicinity.
 	 * Note: a person can be either inside the settlement or within its vicinity
	 * 
	 * @param settlement the settlement
	 * @return list of robots to display.
	 */
	public static List<Robot> getAssociatedRobotsInSettlementVicinity(Settlement settlement) {

		List<Robot> result = new ArrayList<Robot>();

		if (settlement != null) {
			Iterator<Robot> i = settlement.getAllAssociatedRobots().iterator();
			while (i.hasNext()) {
				Robot robot = i.next();

				// Only select functional robots.
				if (!robot.getSystemCondition().isInoperable()) {

					// Select a robot that is at the settlement location.
					Coordinates settlementLoc = settlement.getCoordinates();
					Coordinates personLoc = robot.getCoordinates();
					if (personLoc.equals(settlementLoc)) {
						result.add(robot);
					}
				}
			}
		}

		return result;
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
		Collection<Settlement> ss = unitManager.getSettlements();
		for (Settlement s : ss) {
			int num = s.getItemResourceStored(id);
			result += num;
		}

		return result;
	}
}
