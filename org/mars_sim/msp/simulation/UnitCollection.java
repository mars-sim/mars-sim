/**
 * Mars Simulation Project
 * UnitCollection.java
 * @version 2.73 2001-11-25
 * @author Scott Davis 
 */

package org.mars_sim.msp.simulation;

import java.util.*; // ArrayList

/** The UnitCollection class is a homogenous collection of Unit objects
 *  with useful methods for accessing and sorting them. 
 */
public class UnitCollection {

    // We can replace this with another type of collection if we need to.
    private ArrayList elements;  // Used internally to hold elements.

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
        elements = new ArrayList();
    }

    /** Constructs a UnitCollection object
     *  @param collection collection of elements to copy
     */
    public UnitCollection(UnitCollection collection) {
        elements = new ArrayList();
        UnitIterator iterator = collection.iterator();
        while(iterator.hasNext()) elements.add(iterator.next());
    }

    /** Returns the number of elements in this collection.
     *  @return the number of elements in this collection
     */ 
    public int size() {
        return elements.size();
    }

    /** Returns true if this collection has no elements.
     *  @return true if this collection contains no elements
     */
    public boolean isEmpty() {
        if (elements.size() == 0) return true;
        else return false;
    }

    /** Returns true if this collection contains the specific element.
     *  @param o element whose presence in this collection is to be tested
     *  @return true if this collection contains the specified element
     */
    public boolean contains(Unit o) {
        return elements.contains(o);
    }

    /** Returns an iterator over the elements in this collection.
     *  @return an Iterator over the elements in this collection
     */
    public UnitIterator iterator() {
        return new ThisIterator(elements);
    }


    /** Ensures that this collection contains the specified element.
     *  @param o element whose presence in this collection is to be ensured
     *  @return true if this collection changed as a result of the call
     */
    public boolean add(Unit o) {
        return elements.add(o);
    }

    /** Removes a single instance of the specified element from this 
     *  collection, if it is present.
     *  @param o element to be removed from this collection, if present
     *  @return true if this collection changed as a result of the call
     */
    public boolean remove(Unit o) {
        return elements.remove(o);
    }

    /** Removes all of the elements from this collection. */
    public void clear() {
        elements.clear();
    }
    
    /** Merges a settlement collection into this unit collection.
     *  @param settlements settlement collection to merge
     */
    public void mergeSettlements(SettlementCollection settlements) {
        SettlementIterator i = settlements.iterator();
        while (i.hasNext()) {
            Settlement settlement = i.next();
            if (!elements.contains(settlement)) elements.add(settlement);
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
            if (!elements.contains(vehicle)) elements.add(vehicle);
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
            if (!elements.contains(person)) elements.add(person);
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
}
