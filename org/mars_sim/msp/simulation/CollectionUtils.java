/**
 * Mars Simulation Project
 * MspCollection.java
 * @version 2.84 12.5.2008
 * @author Sebastien Venot
 */
package org.mars_sim.msp.simulation;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.mars_sim.msp.simulation.equipment.Equipment;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.Vehicle;

/**
 * This class gather general collection manipulation methods
 */
public class CollectionUtils {

    public synchronized static Collection<Equipment> getEquipment(Collection<Unit> units) {

	ConcurrentLinkedQueue<Equipment> equipment = new ConcurrentLinkedQueue<Equipment>();

	Iterator<Unit> i = units.iterator();
	while (i.hasNext()) {
	    Unit unit = i.next();
	    if (unit instanceof Equipment)
		equipment.add((Equipment) unit);
	}
	return equipment;

    }

    public synchronized static void mergeEquipments(Collection<Unit> units,
	    			       Collection<Equipment> equipments) {

	Iterator<Equipment> i = equipments.iterator();

	while (i.hasNext()) {
	    Equipment equipmentUnit = i.next();
	    if (!units.contains(equipmentUnit))
		units.add(equipmentUnit);
	}
    }

    
    public synchronized static Collection<Vehicle> getVehicle(Collection<Unit> units) {

	ConcurrentLinkedQueue<Vehicle> vehicles = new ConcurrentLinkedQueue<Vehicle>();

	Iterator<Unit> i = units.iterator();
	while (i.hasNext()) {
	    Unit unit = i.next();
	    if (unit instanceof Vehicle)
		vehicles.add((Vehicle) unit);
	}
	return vehicles;
    }

    public synchronized static void mergeVehicles(Collection<Unit> units,
	    			     Collection<Vehicle> vehicles) {

	Iterator<Vehicle> i = vehicles.iterator();

	while (i.hasNext()) {
	    Vehicle vehicleUnit = i.next();
	    
	    if (!units.contains(vehicleUnit))
		units.add(vehicleUnit);
	}
    }
    
    public synchronized static Collection<Person> getPerson(Collection<Unit> units) {

	ConcurrentLinkedQueue<Person> persons = new ConcurrentLinkedQueue<Person>();

	Iterator<Unit> i = units.iterator();
	while (i.hasNext()) {
	    Unit unit = i.next();
	    if (unit instanceof Person)
		persons.add((Person) unit);
	}
	return persons;
    }

    public synchronized static void mergePersons(Collection<Unit> units,
	    			     Collection<Person> persons) {

	Iterator<Person> i = persons.iterator();

	while (i.hasNext()) {
	    Person personUnit = i.next();
	    
	    if (!units.contains(personUnit))
		units.add(personUnit);
	}
    }
    
    public synchronized static Collection<Settlement> getSettlement(Collection<Unit> units) {

	ConcurrentLinkedQueue<Settlement> settlements = new ConcurrentLinkedQueue<Settlement>();

	Iterator<Unit> i = units.iterator();
	while (i.hasNext()) {
	    Unit unit = i.next();
	    if (unit instanceof Settlement)
		settlements.add((Settlement) unit);
	}
	return settlements;
    }

    public synchronized static void mergeSettlements(Collection<Unit> units,
	    			        Collection<Settlement> settlements) {

	Iterator<Settlement> i = settlements.iterator();

	while (i.hasNext()) {
	    Settlement settlementUnit = i.next();
	    
	    if (!units.contains(settlementUnit))
		units.add(settlementUnit);
	}
    }
    
 
    public synchronized static Settlement getRandomSettlement(Collection collection) {
	Object [] array = collection.toArray();
        int r = RandomUtil.getRandomInt(collection.size() - 1);
        return (Settlement)  array[r];
    }


    public synchronized static Settlement getRandomRegressionSettlement(Collection collection) {
        Settlement result = null;
        int size = collection.size();
        if (size > 0) {
            Object [] array = collection.toArray();
            int chosenSettlementNum = RandomUtil.getRandomRegressionInteger(size);
            result = (Settlement) array[chosenSettlementNum - 1];
        }
        
        return result;
    }
    
    public synchronized static Settlement getSettlement(Collection collection, String name) {
        Iterator i = collection.iterator();
        Settlement result = null;
        while (i.hasNext()) {
            Settlement settlement = (Settlement)i.next();
            if (name.equals(settlement.getName())) result = settlement;
        }
        return result;
    }
    
    public synchronized static <T extends Unit> Collection<T> sortByName(Collection<T> collection) {
    	ConcurrentLinkedQueue<T> sorted = new ConcurrentLinkedQueue<T>();
	
        Iterator<T> outer = collection.iterator();
        while (outer.hasNext()) {
            outer.next();
            String leastName = "ZZZZZZZZZZZZZZZZZZZ";
            T leastUnit = null;
            Iterator<T> inner = collection.iterator();
            while (inner.hasNext()) {
                T tempUnit = inner.next();
                String name = tempUnit.getName();
                if ((name.compareToIgnoreCase(leastName) < 0) && !sorted.contains(tempUnit)) {
                    leastName = name;
                    leastUnit = tempUnit;
                }
            }
            sorted.add(leastUnit);
        }

        return sorted;
    }
    
    public synchronized static <T extends Unit> Collection<T> sortByProximity(Collection<T> collection, 
    		Coordinates location) {
    	ConcurrentLinkedQueue<T> sorted = new ConcurrentLinkedQueue<T>();

        Iterator outer = collection.iterator();
        
        while (outer.hasNext()) {
            outer.next();
            double closestDistance = Double.MAX_VALUE;
            T closestUnit = null;
            Iterator<T> inner = collection.iterator();
            while (inner.hasNext()) {
                T tempUnit = inner.next();
                double distance = location.getDistance(tempUnit.getCoordinates());
                if ((distance < closestDistance) && !sorted.contains(tempUnit)) {
                    closestDistance = distance;
                    closestUnit = tempUnit;
                }
            }
            sorted.add(closestUnit);
        }

        return sorted;
    }
}
