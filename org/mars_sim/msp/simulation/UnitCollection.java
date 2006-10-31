/**
 * Mars Simulation Project
 * UnitCollection.java
 * @version 2.75 2002-05-24
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.vehicle.*;
import org.mars_sim.msp.simulation.equipment.*;
import java.util.*; // ArrayList
import java.io.Serializable;

/**
 * This class supports a heterogenenours list of Units. It extends the
 * MspCollection class that provides the basic list of Units functionality.
 * This calls adds to the functionality by allowing the selections of Unit to
 * be extracted from the underlying List according to the Unit subclass.
 * Also merging actions are supported.
 */
public class UnitCollection extends MspCollection implements Serializable {


    // inner class to implement our type-safe iterator
    private class ThisIterator implements UnitIterator {
        private Iterator iterator;

        /** Constructor */
        ThisIterator(Collection collection) {
            iterator = collection.iterator();
        }

        /** Returns the next element in the interation.
         *  @return the next element in the interation
         */
        public Unit next() {
            return (Unit) iterator.next();
        }

        /** Returns true if the iteration has more elements.
         *  @return true if the iterator has more elements
         */
        public boolean hasNext() {
            return iterator.hasNext();
        }

        /** Removes from the underlying collection the
         *  last element returned by the iterator.
         */
        public void remove() {
            iterator.remove();
        }
    }

    /** Constructs a UnitCollection object */
    public UnitCollection() {
    }

    /** Constructs a UnitCollection object
     *  @param collection collection of elements to copy
     */
    public UnitCollection(UnitCollection collection) {
        UnitIterator iterator = collection.iterator();
        while(iterator.hasNext()) add(iterator.next());
    }

    /** Returns an iterator over the elements in this collection.
     *  @return an Iterator over the elements in this collection
     */
    public UnitIterator iterator() {
        return new ThisIterator(getUnits());
    }

    /**
     * Merges a unit collection into this unit collection.
     * @param units unit collection to merge
     */
    public void mergeUnits(UnitCollection units) {
        UnitIterator i = units.iterator();
	    while (i.hasNext()) {
	        Unit unit = i.next();
	        if (!contains(unit)) add(unit);
	    }
    }

    /** Merges a settlement collection into this unit collection.
     *  @param settlements settlement collection to merge
     */
    public void mergeSettlements(SettlementCollection settlements) {
        SettlementIterator i = settlements.iterator();
        while (i.hasNext()) {
            Settlement settlement = i.next();
            if (!contains(settlement)) add(settlement);
        }
    }

    /** Gets a subset of this collection of all the settlements.
     *  @return settlements collection subset
     */
    public SettlementCollection getSettlements() {
        SettlementCollection settlements = new SettlementCollection();
        UnitIterator i = iterator();
        while (i.hasNext()) {
            Unit unit = i.next();
            if (unit instanceof Settlement) settlements.add((Settlement) unit);
        }
        return settlements;
    }

    /** Merges a vehicle collection into this unit collection.
     *  @param vehicles vehicle collection to merge
     */
    public void mergeVehicles(VehicleCollection vehicles) {
        VehicleIterator i = vehicles.iterator();
        while (i.hasNext()) {
            Vehicle vehicle = i.next();
            if (!contains(vehicle)) add(vehicle);
        }
    }

    /** Gets a subset of this collection of all the vehicles.
     *  @return vehicle collection subset
     */
    public VehicleCollection getVehicles() {
        VehicleCollection vehicles = new VehicleCollection();
        UnitIterator i = iterator();
        while (i.hasNext()) {
            Unit unit = i.next();
            if (unit instanceof Vehicle) vehicles.add((Vehicle) unit);
        }
        return vehicles;
    }

    /** Merges a person collection into this unit collection.
     *  @param people person collection to merge
     */
    public void mergePeople(PersonCollection people) {
        PersonIterator i = people.iterator();
        while (i.hasNext()) {
            Person person = i.next();
            if (!contains(person)) add(person);
        }
    }

    /** Gets a subset of this collection of all the people.
     *  @return person collection subset
     */
    public PersonCollection getPeople() {
        PersonCollection people = new PersonCollection();
        UnitIterator i = iterator();
        while (i.hasNext()) {
            Unit unit = i.next();
            if (unit instanceof Person) people.add((Person) unit);
        }
        return people;
    }

    /** Merges a equipment collection into this unit collection.
     *  @param equipment equipment collection to merge
     */
    public void mergeEquipment(EquipmentCollection equipment) {
        EquipmentIterator i = equipment.iterator();
        while (i.hasNext()) {
            Equipment equipmentUnit = i.next();
            if (!contains(equipmentUnit)) add(equipmentUnit);
        }
    }

    /** Gets a subset of this collection of all the equipment.
     *  @return equipment collection subset
     */
    public EquipmentCollection getEquipment() {
        EquipmentCollection equipment = new EquipmentCollection();
        UnitIterator i = iterator();
        while (i.hasNext()) {
            Unit unit = i.next();
            if (unit instanceof Equipment) equipment.add((Equipment) unit);
        }
        return equipment;
    }
}
