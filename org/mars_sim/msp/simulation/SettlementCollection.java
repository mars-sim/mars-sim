/**
 * Mars Simulation Project
 * SettlementCollection.java
 * @version 2.73 2001-10-24
 * @author Scott Davis 
 */

package org.mars_sim.msp.simulation;

import java.util.*; // ArrayList

/** The SettlementCollection class is a homogenous collection of Settlement objects
 *  with useful methods for accessing and sorting them. 
 */
public class SettlementCollection {

    // We can replace this with another type of collection if we need to.
    private ArrayList elements;  // Used internally to hold elements.

    // inner class to implement our type-safe iterator
    private class Iterator implements SettlementIterator {
        private Iterator iterator;

        /**
         *  Constructor
         */
        Iterator(SettlementCollection collection) {
            iterator = collection.iterator();
        } 

        public Settlement next() {
            return (Settlement) iterator.next();
        }
  
        /**
         *  Returns true if the iteration has more elements.
         *  @return true if the iterator has more elements.
         */
        public boolean hasNext() {
            return iterator.hasNext();
        }
  
        /**
         *  Removes from the underlying collection the 
         *  last element returned by the iterator.
         */
        public void remove() {
            iterator.remove();
        }
    }

    /** 
     *  Constructs a SettlementCollection object
     */
    public SettlementCollection() {
        elements = new ArrayList();
    }

    /** 
     *  Constructs a SettlementCollection object
     *  @param collection collection of elements to copy
     */
    public SettlementCollection(SettlementCollection collection) {
        elements = new ArrayList();
        Iterator iterator = collection.iterator();
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
    public boolean contains(Settlement o) {
        return elements.contains(o);
    }

    /**
     *  Returns an iterator over the elements in this collection.
     *  @return an Iterator over the elements in this collection
     */
    public Iterator iterator() {
        return new Iterator(this);
    }


    /**
     *  Ensures that this collection contains the specified element.
     *  @param o element whose presence in this collection is to be ensured.
     *  @return true if this collection changed as a result of the call
     */
    public boolean add(Settlement o) {
        return elements.add(o);
    }

    /**
     *  Removes a single instance of the specified element from this 
     *  collection, if it is present.
     *  @param o element to be removed from this collection, if present.
     *  @return true if this collection changed as a result of the call
     */
    public boolean remove(Settlement o) {
        return elements.remove(o);
    }

    /**
     *  Removes all of the elements from this collection.
     */
    public void clear() {
        elements.clear();
    }
}
