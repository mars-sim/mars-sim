/**
 * Mars Simulation Project
 * VehicleCollection.java
 * @version 2.74 2002-01-13
 * @author Scott Davis 
 */

package org.mars_sim.msp.simulation.vehicle;

import java.util.*; // ArrayList
import java.io.Serializable;

/** The VehicleCollection class is a homogenous collection of Vehicle objects
 *  with useful methods for accessing and sorting them. 
 */
public class VehicleCollection implements Serializable {

    // We can replace this with another type of collection if we need to.
    private ArrayList elements;  // Used internally to hold elements.

    // inner class to implement our type-safe iterator
    private class ThisIterator implements VehicleIterator {
        private Iterator iterator;

        /** Constructor */
        ThisIterator(Collection collection) {
            iterator = collection.iterator();
        } 

        /** Returns the next element in the interation.
         *  @return the next element in the interation
         */
        public Vehicle next() {
            return (Vehicle) iterator.next();
        }
  
        /** Returns true if the iteration has more elements.
         *  @return true if the iterator has more elements.
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

    /** 
     *  Constructs a VehicleCollection object
     */
    public VehicleCollection() {
        elements = new ArrayList();
    }

    /** 
     *  Constructs a VehicleCollection object
     *  @param collection collection of elements to copy
     */
    public VehicleCollection(VehicleCollection collection) {
        elements = new ArrayList();
        VehicleIterator iterator = collection.iterator();
        while(iterator.hasNext()) elements.add(iterator.next());
    }

    /** 
     *  Returns the number of elements in this collection.
     *  @return the number of elements in this collection
     */ 
    public int size() {
        return elements.size();
    }

    /**
     *  Returns true if this collection has no elements.
     *  @return true if this collection contains no elements
     */
    public boolean isEmpty() {
        if (elements.size() == 0) return true;
        else return false;
    }

    /**
     *  Returns true if this collection contains the specific element.
     *  @param o element whose presence in this collection is to be tested.
     *  @return true if this collection contains the specified element
     */
    public boolean contains(Vehicle o) {
        return elements.contains(o);
    }

    /**
     *  Returns an iterator over the elements in this collection.
     *  @return an Iterator over the elements in this collection
     */
    public VehicleIterator iterator() {
        return new ThisIterator(elements);
    }


    /**
     *  Ensures that this collection contains the specified element.
     *  @param o element whose presence in this collection is to be ensured.
     *  @return true if this collection changed as a result of the call
     */
    public boolean add(Vehicle o) {
        return elements.add(o);
    }

    /**
     *  Removes a single instance of the specified element from this 
     *  collection, if it is present.
     *  @param o element to be removed from this collection, if present.
     *  @return true if this collection changed as a result of the call
     */
    public boolean remove(Vehicle o) {
        return elements.remove(o);
    }

    /**
     *  Removes all of the elements from this collection.
     */
    public void clear() {
        elements.clear();
    }
    
    /** Sort by name
     *  @return vehicle collection sorted by name
     */
    public VehicleCollection sortByName() {
        VehicleCollection sortedVehicles = new VehicleCollection();
        VehicleIterator outer = iterator();
        while (outer.hasNext()) {
            outer.next();
            String leastName = "ZZZZZZZZZZZZZZZZZZZ";
            Vehicle leastVehicle = null;
            VehicleIterator inner = iterator();
            while (inner.hasNext()) {
                Vehicle tempVehicle = inner.next();
                String name = tempVehicle.getName();
                if ((name.compareTo(leastName) < 0) && !sortedVehicles.contains(tempVehicle)) {
                    leastName = name;
                    leastVehicle = tempVehicle;
                }
            }
            sortedVehicles.add(leastVehicle);
        }
        
        return sortedVehicles;
    }
}
