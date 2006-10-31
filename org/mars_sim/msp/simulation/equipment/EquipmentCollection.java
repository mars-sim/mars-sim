/**
 * Mars Simulation Project
 * EquipmentCollection.java
 * @version 2.74 2002-02-26
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.equipment;

import org.mars_sim.msp.simulation.*;
import java.util.*; // ArrayList
import java.io.Serializable;

/** The EquipmentCollection class is a homogenous collection of Equipment objects
 *  with useful methods for accessing and sorting them.
 */
public class EquipmentCollection extends MspCollection implements Serializable {

    // inner class to implement our type-safe iterator
    private class ThisIterator implements EquipmentIterator {
        private Iterator iterator;

        /** Constructor */
        ThisIterator(Collection collection) {
            iterator = collection.iterator();
        }

        /** Returns the next element in the interation.
         *  @return the next element in the interation
         */
        public Equipment next() {
            return (Equipment) iterator.next();
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
     *  Constructs a EquipmentCollection object
     */
    public EquipmentCollection() {
    }

    /**
     *  Constructs a EquipmentCollection object
     *  @param collection collection of elements to copy
     */
    public EquipmentCollection(EquipmentCollection collection) {
        EquipmentIterator iterator = collection.iterator();
        while(iterator.hasNext()) add(iterator.next());
    }

    /**
     *  Returns an iterator over the elements in this collection.
     *  @return an Iterator over the elements in this collection
     */
    public EquipmentIterator iterator() {
        return new ThisIterator(getUnits());
    }

    /** Sort by name
     *  @return equipment collection sorted by name
     */
    public EquipmentCollection sortByName() {
        EquipmentCollection sortedEquipment = new EquipmentCollection();
        EquipmentIterator outer = iterator();
        while (outer.hasNext()) {
            outer.next();
            String leastName = "ZZZZZZZZZZZZZZZZZZZ";
            Equipment leastEquipment = null;
            EquipmentIterator inner = iterator();
            while (inner.hasNext()) {
                Equipment tempEquipment = inner.next();
                String name = tempEquipment.getName();
                if ((name.compareTo(leastName) < 0) && !sortedEquipment.contains(tempEquipment)) {
                    leastName = name;
                    leastEquipment = tempEquipment;
                }
            }
            sortedEquipment.add(leastEquipment);
        }

        return sortedEquipment;
    }
}
