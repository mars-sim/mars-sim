/**
 * Mars Simulation Project
 * PersonCollection.java
 * @version 2.73 2001-10-24
 * @author Edgar Crisostomo 
 */

package org.mars_sim.msp.simulation;

import java.util.*; // ArrayList

/** The PersonCollection class is a homogenous collection of Person objects
 *  with useful methods for accessing and sorting them. 
 */
public class PersonCollection {

    // We can replace this with another type of collection if we need to.
    private ArrayList elements;  // Used internally to hold elements.

    // inner class to implement our type-safe iterator
    private class Iterator implements PersonIterator {
        private Iterator iterator;

        /**
         *  Constructor
         */
        Iterator(PersonCollection collection) {
            iterator = collection.iterator();
        } 

        public Person next() {
            return (Person) iterator.next();
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
     *  Constructs a PersonCollection object
     */
    public PersonCollection() {
        elements = new ArrayList();
    }

    /** 
     *  Constructs a PersonCollection object
     *  @param collection collection of elements to copy
     */
    public PersonCollection(PersonCollection collection) {
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
    public boolean contains(Person o) {
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
    public boolean add(Person o) {
        return elements.add(o);
    }

    /**
     *  Removes a single instance of the specified element from this 
     *  collection, if it is present.
     *  @param o element to be removed from this collection, if present.
     *  @return true if this collection changed as a result of the call
     */
    public boolean remove(Person o) {
        return elements.remove(o);
    }

    /**
     *  Removes all of the elements from this collection.
     */
    public void clear() {
        elements.clear();
    }
}
