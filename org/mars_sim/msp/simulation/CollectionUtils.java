/**
 * Mars Simulation Project
 * MspCollection.java
 * @version 2.83 20.3.2008
 * @author Sebastien Venot
 */
package org.mars_sim.msp.simulation;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.mars_sim.msp.simulation.equipment.Equipment;

/**
 * This class gather general collection manipulation methods
 */
public class CollectionUtils {

    public static Collection<Equipment> getEquipment(Collection<Unit> units) {

	ConcurrentLinkedQueue<Equipment> equipment = new ConcurrentLinkedQueue<Equipment>();

	Iterator<Unit> i = units.iterator();
	while (i.hasNext()) {
	    Unit unit = i.next();
	    if (unit instanceof Equipment)
		equipment.add((Equipment) unit);
	}
	return equipment;

    }

    public static void mergeEquipments(Collection<Unit> units,
	    Collection<Equipment> equipments) {

	Iterator<Equipment> i = equipments.iterator();

	while (i.hasNext()) {
	    Equipment equipmentUnit = i.next();
	    if (!units.contains(equipmentUnit))
		units.add(equipmentUnit);
	}
    }

    public Collection<Unit> sortByName(Collection<Unit> collection) {
	ConcurrentLinkedQueue<Unit> sorted = new ConcurrentLinkedQueue<Unit>();
	Iterator<Unit> outer = collection.iterator();

	while (outer.hasNext()) {
	    outer.next();
	    String leastName = "ZZZZZZZZZZZZZZZZZZZ";
	    Unit leastUnit = null;
	    Iterator<Unit> inner = collection.iterator();

	    while (inner.hasNext()) {
		Unit tempUnit = inner.next();
		String name = tempUnit.getName();
		if ((name.compareTo(leastName) < 0)
			&& !sorted.contains(tempUnit)) {
		    leastName = name;
		    leastUnit = tempUnit;
		}
	    }
	    sorted.add(leastUnit);
	}

	return sorted;
    }

}
