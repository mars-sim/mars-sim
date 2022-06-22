/**
 * Mars Simulation Project
 * CollectionUtils.java
 * @version 3.2.0 2021-06-20
 * @author Sebastien Venot
 */
package org.mars_sim.msp.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * This class provides general collection manipulation convenience methods.
 */
public class CollectionUtils {

	private static UnitManager unitManager = Simulation.instance().getUnitManager();
	private static SimulationConfig simulationConfig = SimulationConfig.instance();

	public static Collection<Equipment> getEquipment(
		Collection<Unit> units
	) {
		return units
				.stream()
				.filter(u -> u instanceof Equipment)
				.map(u -> (Equipment) u)
				.filter(u -> !u.isSalvaged())
				.collect(Collectors.toList());
	}

	
	public static Collection<Robot> getRobot(
		Collection<Unit> units
	) {

//		return units
//				.stream()
//				.filter(u-> u instanceof Robot)
//				.map(u -> (Robot) u)
//				.collect(Collectors.toList());

		ConcurrentLinkedQueue<Robot> robots = new ConcurrentLinkedQueue<Robot>();
		for (Unit unit : units) {
			if (unit instanceof Robot)
				robots.add((Robot) unit);
		}
		return robots;

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
	 * Finds a nearby settlement based on its coordinate
	 *
	 * @param c {@link Coordinates}
	 * @return
	 */
	public static Settlement findSettlement(Coordinates c) {
		if (unitManager == null)
			unitManager = Simulation.instance().getUnitManager();

		Collection<Settlement> ss = unitManager.getSettlements();
		for (Settlement s : ss) {
			if (s.getCoordinates().equals(c) || s.getCoordinates() == c)
				return s;
		}

		return null;
		// WARNING : using associated settlement needs to exercise more caution
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

	/**
	 * Gets a list of people to display on a settlement map.
	 * Note: a person can be either inside the settlement or within its vicinity
	 *
	 * @param settlement the settlement
	 * @return list of people to display.
	 */
	public static List<Person> getPeopleToDisplay(Settlement settlement) {

		List<Person> result = new ArrayList<Person>();

		if (settlement != null) {
			Iterator<Person> i = unitManager.getPeople().iterator();
			while (i.hasNext()) {
				Person person = i.next();

				// Only select living people.
				if (!person.getPhysicalCondition().isDead()) {

					// Select a person that is at the settlement location.
					Coordinates settlementLoc = settlement.getCoordinates();
					Coordinates personLoc = person.getCoordinates();
					if (personLoc.equals(settlementLoc)) {
						result.add(person);
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
