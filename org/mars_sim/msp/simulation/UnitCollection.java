/**
 * Mars Simulation Project
 * UnitCollection.java
 * @version 2.73 2001-10-24
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.util.*;

/** The UnitCollection class is a collection of Unit objects
 *  with useful methods for accessing and sorting them. 
 */
public class UnitCollection implements Collection {

    // We can replace this with another type of collection if we need to.
    private Vector units;  // Used internally to hold elements.

    /** 
     *  Constructs a UnitCollection object
     */
    public UnitCollection() {
        units = new Vector();
    }

    /** 
     *  Constructs a UnitCollection object
     *  @param collection collection of units to copy
     */
    public UnitCollection(Collection collection) {
        units = new Vector();
        Iterator iterator = collection.iterator();
        while(iterator.hasNext()) units.addElement(iterator.next());
    }

    /** 
     *  Returns the number of elements in this collection.
     *  @return the number of elements in this collection
     */ 
    public int size() {
        return units.size();
    }

    /**
     *  Returns true if this collection has no elements.
     *  @return true if this collection contains no elements
     */
    public boolean isEmpty() {
        if (units.size() == 0) return true;
        else return false;
    }

    /**
     *  Returns true if this collection contains the specific element.
     *  @param o element whose presence in this collection is to be tested.
     *  @return true if this collection contains the specified element
     */
    public boolean contains(Object o) {
        return units.contains(o);
    }

    /**
     *  Returns an iterator over the elements in this collection.
     *  @return an Iterator over the elements in this collection
     */
    public Iterator iterator() {
        return new UnitIterator(units);
    }

    /**
     *  Returns an array containing all of the elements in this collection.
     *  @return an array containing all of the elements in this collection
     */
    public Object[] toArray() {
        return units.toArray();
    }

    /**
     *  Returns an array containing all of the elements in this collection 
     *  whose runtime type is that of the specified array. 
     *  @param a the array into which the elements of this collection are to be stored.
     *  @return an array containing the elements of this collection
     */
    public Object[] toArray(Object[] a) {
        return units.toArray(a);
    }

    /**
     *  Ensures that this collection contains the specified element.
     *  @param o element whose presence in this collection is to be ensured.
     *  @return true if this collection changed as a result of the call
     */
    public boolean add(Object o) {
        return units.add(o);
    }

    /**
     *  Removes a single instance of the specified element from this 
     *  collection, if it is present.
     *  @param o element to be removed from this collection, if present.
     *  @return true if this collection changed as a result of the call
     */
    public boolean remove(Object o) {
        return units.remove(o);
    }

    /**
     *  Returns true if this collection contains all of the elements 
     *  in the specified collection.
     *  @param c collection to be checked for containment in this collection.
     *  @return true if this collection contains all of the elements in the 
     *  specified collection
     */
    public boolean containsAll(Collection c) {
        return units.containsAll(c);
    }

    /**
     *  Adds all of the elements in the specified collection to this collection.
     *  @param c elements to be inserted into this collection.
     *  @return true if this collection changed as a result of the call
     */
    public boolean addAll(Collection c) {
        return units.addAll(c);
    }

    /**
     *  Removes all this collection's elements that are also contained 
     *  in the specified collection.
     *  @param c elements to be removed from this collection.
     *  @return true if this collection changed as a result of the call
     */
    public boolean removeAll(Collection c) {
        return units.removeAll(c);
    }

    /** 
     *  Retains only the elements in this collection that are contained 
     *  in the specified collection.
     *  @param c elements to be retained in this collection.
     *  @return true if this collection changed as a result of the call
     */
    public boolean retainAll(Collection c) {
        return units.retainAll(c);
    }

    /**
     *  Removes all of the elements from this collection.
     */
    public void clear() {
        units.clear();
    }
}
