/**
 * Mars Simulation Project
 * CollectionUtils.java
 * @version 3.07 2015-01-21
 * @author Sebastien Venot
 */
package org.mars_sim.msp.core;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * This class provides general collection manipulation convenience methods.
 */
public class CollectionUtils {

	public static Collection<Equipment> getEquipment(
		Collection<Unit> units
	) {
		return units
				.stream()
				.filter(u-> u instanceof Equipment)
				.map(u -> (Equipment) u)
				.filter(u-> !u.isSalvaged())
				.collect(Collectors.toList());
/*
		ConcurrentLinkedQueue<Equipment> equipment = new ConcurrentLinkedQueue<Equipment>();
		for (Unit unit : units) {
			if (unit instanceof Equipment) {
				Equipment equipmentUnit = (Equipment) unit;
				if (!equipmentUnit.isSalvaged())
					equipment.add(equipmentUnit);
			}
		}
		return equipment;
*/
	}

	public synchronized static void mergeEquipments(Collection<Unit> units,
		Collection<Equipment> equipments
	) {
		for (Equipment equipmentUnit : equipments) {
			if (!units.contains(equipmentUnit))
				units.add(equipmentUnit);
		}
	}

	public static Collection<Vehicle> getVehicle(
		Collection<Unit> units
	) {
		return units
				.stream()
				.filter(u-> u instanceof Vehicle)
				.map(u -> (Vehicle) u)
				.filter(u-> !u.isSalvaged())
				.collect(Collectors.toList());
/*
		ConcurrentLinkedQueue<Vehicle> vehicles = new ConcurrentLinkedQueue<Vehicle>();
		for (Unit unit : units) {
			if (unit instanceof Vehicle) {
				Vehicle vehicleUnit = (Vehicle) unit;
				if (!vehicleUnit.isSalvaged()) vehicles.add(vehicleUnit);
			}
		}
		return vehicles;
*/
	}

	public synchronized static void mergeVehicles(Collection<Unit> units,
		Collection<Vehicle> vehicles
	) {
		for (Vehicle vehicleUnit : vehicles) {
			if (!units.contains(vehicleUnit))
				units.add(vehicleUnit);
		}
	}

	public static Collection<Robot> getRobot(
		Collection<Unit> units
	) {
		return units
				.stream()
				.filter(u-> u instanceof Robot)
				.map(u -> (Robot) u)
				.collect(Collectors.toList());
/*
		ConcurrentLinkedQueue<Robot> robots = new ConcurrentLinkedQueue<Robot>();
		for (Unit unit : units) {
			if (unit instanceof Robot)
				robots.add((Robot) unit);
		}
		return robots;
*/
	}

	public static void mergeRobots(Collection<Unit> units,
		Collection<Robot> robots
	) {
		for (Robot robotUnit : robots) {
			if (!units.contains(robotUnit))
				units.add(robotUnit);
		}
	}


	public static Collection<Person> getPerson(
		Collection<Unit> units
	) {

		return units
				.stream()
				.filter(u-> u instanceof Person)
				.map(u -> (Person) u)
				.collect(Collectors.toList());
/*
		ConcurrentLinkedQueue<Person> persons = new ConcurrentLinkedQueue<Person>();
		Iterator<Unit> i = units.iterator(); // switch to iterator to avoid concurrent modification exception
		while (i.hasNext()) {
			Unit unit = i.next();
			if (unit instanceof Person)
				persons.add((Person) unit);
		}
		return persons;
*/
	}

	public synchronized static void mergePersons(Collection<Unit> units,
		Collection<Person> persons
	) {
		for (Person personUnit : persons) {
			if (!units.contains(personUnit))
				units.add(personUnit);
		}
	}

	public static Collection<Settlement> getSettlement(
		Collection<Unit> units
	) {
		return units
				.stream()
				.filter(u-> u instanceof Settlement)
				.map(u -> (Settlement) u)
				.collect(Collectors.toList());
/*
		ConcurrentLinkedQueue<Settlement> settlements = new ConcurrentLinkedQueue<Settlement>();
		for (Unit unit : units) {
			if (unit instanceof Settlement)
				settlements.add((Settlement) unit);
		}
		return settlements;
*/
	}

	public synchronized static void mergeSettlements(Collection<Unit> units,
		Collection<Settlement> settlements
	) {
		for (Settlement settlementUnit : settlements) {

			if (!units.contains(settlementUnit))
				units.add(settlementUnit);
		}
	}

	public static Settlement getRandomSettlement(
		Collection<Settlement> collection
	) {
	    Settlement result = null;
		Object[] array = collection.toArray();
		if (collection.size() > 0) {
		    int r = RandomUtil.getRandomInt(collection.size() - 1);
		    result = (Settlement) array[r];
		}

		return result;
	}

	public static Settlement getRandomRegressionSettlement(
		Collection<Settlement> collection
	) {
		Settlement result = null;
		int size = collection.size();
		if (size > 0) {
			Object[] array = collection.toArray();
			int chosenSettlementNum = RandomUtil.getRandomRegressionInteger(size);
			result = (Settlement) array[chosenSettlementNum - 1];
		}

		return result;
	}

	public static Settlement getSettlement(
		Collection<Settlement> collection,
		String name
	) {
/*
		Iterator<Settlement> i = collection.iterator();
		Settlement result = null;
		while (result == null && i.hasNext()) {
			Settlement settlement = i.next();
			if (name.equals(settlement.getName()))
				result = settlement;
		}

		return result;
*/
		return collection
				.stream()
				.filter(u-> name.equals(u.getName()))
				.map(u -> (Settlement) u)
				.findFirst().orElse(null);//.get();

	}

	public static <T extends Unit> Collection<T> sortByName(
		Collection<T> collection
	) {
		ConcurrentSkipListSet<T> sorted = new ConcurrentSkipListSet<T>(
			new Comparator<T>() {
				@Override
				public int compare(T o1, T o2) {
					return o1.getName().compareToIgnoreCase(o2.getName());
				}
			}
		);
		sorted.addAll(collection);
		return sorted;
	}

	public static <T extends Unit> Collection<T> sortByProximity(
		Collection<T> collection,
		final Coordinates location
	) {
		ConcurrentSkipListSet<T> sorted = new ConcurrentSkipListSet<T>(
			new Comparator<T>() {
				@Override
				public int compare(T o1, T o2) {
					return Double.compare(
							location.getDistance(o1.getCoordinates()),
							location.getDistance(o2.getCoordinates())
							);
				}
			}
		);
		sorted.addAll(collection);
		return sorted;
	}
}
