/**
 * Mars Simulation Project
 * SettlementCollection.java
 * @version 2.74 2002-02-26
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure;

import org.mars_sim.msp.simulation.*;
import java.util.*; // ArrayList
import java.io.Serializable;

/** The SettlementCollection class is a homogenous collection of Settlement objects
 *  with useful methods for accessing and sorting them.
 */
public class SettlementCollection extends MspCollection implements Serializable {

    // inner class to implement our type-safe iterator
    private class ThisIterator implements SettlementIterator {
        private Iterator iterator;

        /**
         *  Constructor
         */
        ThisIterator(Collection collection) {
            iterator = collection.iterator();
        }

        /** Returns the next element in the interation.
         *  @return the next element in the interation
         */
        public Settlement next() {
            return (Settlement) iterator.next();
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

    /** Constructs a SettlementCollection object */
    public SettlementCollection() {
    }

    /** Constructs a SettlementCollection object
     *  @param collection collection of elements to copy
     */
    public SettlementCollection(SettlementCollection collection) {
        SettlementIterator iterator = collection.iterator();
        while(iterator.hasNext()) add(iterator.next());
    }


    /** Returns an iterator over the elements in this collection.
     *  @return an Iterator over the elements in this collection
     */
    public SettlementIterator iterator() {
        return new ThisIterator(getUnits());
    }


    /** Gets a randomly selected settlement.
     *  @return settlement
     */
    public Settlement getRandomSettlement() {
        int r = RandomUtil.getRandomInt(size() - 1);
        return (Settlement) getUnits().get(r);
    }

    /** Gets a randomly selected settlement with earlier elements in the
     *  collection being more likely to be chosen.
     *  ex. 1st element: 50%; 2nd element: 25%, ect.
     *  Returns null if collection is empty.
     *  @return randomly selected settlement
     */
    public Settlement getRandomRegressionSettlement() {
        Settlement result = null;
        if (size() > 0) {
            int chosenSettlementNum = RandomUtil.getRandomRegressionInteger(size());
            result = (Settlement) getUnits().get(chosenSettlementNum - 1);
        }
        return result;
    }

    /** Gets the first settlement in the collection with a given name.
     *  If no settlement in the collection has the given name, returns null.
     *  @param name the name of the settlement wanted
     *  @return settlement with given name
     */
    public Settlement getSettlement(String name) {
        SettlementIterator i = iterator();
        Settlement result = null;
        while (i.hasNext()) {
            Settlement settlement = i.next();
            if (name.equals(settlement.getName())) result = settlement;
        }
        return result;
    }

    /** Sort by name
     *  @return settlement collection sorted by name
     */
    public SettlementCollection sortByName() {
        SettlementCollection sortedSettlements = new SettlementCollection();
        SettlementIterator outer = iterator();
        while (outer.hasNext()) {
            outer.next();
            String leastName = "ZZZZZZZZZZZZZZZZZZZ";
            Settlement leastSettlement = null;
            SettlementIterator inner = iterator();
            while (inner.hasNext()) {
                Settlement tempSettlement = inner.next();
                String name = tempSettlement.getName();
                if ((name.compareTo(leastName) < 0) && !sortedSettlements.contains(tempSettlement)) {
                    leastName = name;
                    leastSettlement = tempSettlement;
                }
            }
            sortedSettlements.add(leastSettlement);
        }

        return sortedSettlements;
    }

    /** Sorts the settlement collection by proximity to a given location.
     *  @param location the given location
     *  @return the sorted settlement collection
     */
    public SettlementCollection sortByProximity(Coordinates location) {
        SettlementCollection sortedSettlements = new SettlementCollection();
        SettlementIterator outer = iterator();
        while (outer.hasNext()) {
            outer.next();
            double closestDistance = Double.MAX_VALUE;
            Settlement closestSettlement = null;
            SettlementIterator inner = iterator();
            while (inner.hasNext()) {
                Settlement tempSettlement = inner.next();
                double distance = location.getDistance(tempSettlement.getCoordinates());
                if ((distance < closestDistance) && !sortedSettlements.contains(tempSettlement)) {
                    closestDistance = distance;
                    closestSettlement = tempSettlement;
                }
            }
            sortedSettlements.add(closestSettlement);
        }

        return sortedSettlements;
    }
}
