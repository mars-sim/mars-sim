/**
 * Mars Simulation Project
 * SettlementCollection.java
 * @version 2.74 2002-01-13
 * @author Scott Davis 
 */

package org.mars_sim.msp.simulation.structure;

import org.mars_sim.msp.simulation.*;
import java.util.*; // ArrayList

/** The SettlementCollection class is a homogenous collection of Settlement objects
 *  with useful methods for accessing and sorting them. 
 */
public class SettlementCollection {

    // We can replace this with another type of collection if we need to.
    private ArrayList elements;  // Used internally to hold elements.

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
        elements = new ArrayList();
    }

    /** Constructs a SettlementCollection object
     *  @param collection collection of elements to copy
     */
    public SettlementCollection(SettlementCollection collection) {
        elements = new ArrayList();
        SettlementIterator iterator = collection.iterator();
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
    public boolean contains(Settlement o) {
        return elements.contains(o);
    }

    /** Returns an iterator over the elements in this collection.
     *  @return an Iterator over the elements in this collection
     */
    public SettlementIterator iterator() {
        return new ThisIterator(elements);
    }


    /** Ensures that this collection contains the specified element.
     *  @param o element whose presence in this collection is to be ensured
     *  @return true if this collection changed as a result of the call
     */
    public boolean add(Settlement o) {
        return elements.add(o);
    }

    /** Removes a single instance of the specified element from this 
     *  collection, if it is present.
     *  @param o element to be removed from this collection, if present
     *  @return true if this collection changed as a result of the call
     */
    public boolean remove(Settlement o) {
        return elements.remove(o);
    }

    /** Removes all of the elements from this collection. */
    public void clear() {
        elements.clear();
    }
    
    /** Gets a randomly selected settlement.
     *  @return settlement
     */
    public Settlement getRandomSettlement() {
        int r = RandomUtil.getRandomInt(elements.size() - 1);
        return (Settlement) elements.get(r);
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
            result = (Settlement) elements.get(chosenSettlementNum - 1);
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
