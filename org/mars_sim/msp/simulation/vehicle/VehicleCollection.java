/**
 * Mars Simulation Project
 * VehicleCollection.java
 * @version 2.74 2002-02-26
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.vehicle;

import org.mars_sim.msp.simulation.*;
import java.util.*; // ArrayList
import java.io.Serializable;

/** The VehicleCollection class is a homogenous collection of Vehicle objects
 *  with useful methods for accessing and sorting them.
 */
public class VehicleCollection extends MspCollection implements Serializable {

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
    }

    /**
     *  Constructs a VehicleCollection object
     *  @param collection collection of elements to copy
     */
    public VehicleCollection(VehicleCollection collection) {
        VehicleIterator iterator = collection.iterator();
        while(iterator.hasNext()) add(iterator.next());
    }


    /**
     *  Returns an iterator over the elements in this collection.
     *  @return an Iterator over the elements in this collection
     */
    public VehicleIterator iterator() {
        return new ThisIterator(getUnits());
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
