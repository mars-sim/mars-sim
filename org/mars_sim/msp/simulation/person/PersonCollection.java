/**
 * Mars Simulation Project
 * PersonCollection.java
 * @version 2.74 2002-02-26
 * @author Edgar Crisostomo 
 */

package org.mars_sim.msp.simulation.person;

import org.mars_sim.msp.simulation.*;
import java.util.*; // ArrayList
import java.io.Serializable;

/** The PersonCollection class is a homogenous collection of Person objects
 *  with useful methods for accessing and sorting them. 
 */
public class PersonCollection extends MspCollection implements Serializable {

    // We can replace this with another type of collection if we need to.
    private ArrayList elements;  // Used internally to hold elements.

    // inner class to implement our type-safe iterator
    private class ThisIterator implements PersonIterator {
        private Iterator iterator;

        /** Constructor */
        ThisIterator(Collection collection) {
            iterator = collection.iterator();
        } 

        /** Returns the next element in the interation.
         *  @return the next element in the interation
         */
        public Person next() {
            return (Person) iterator.next();
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
        PersonIterator iterator = collection.iterator();
        while(iterator.hasNext()) add(iterator.next());
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
    public PersonIterator iterator() {
        return new ThisIterator(elements);
    }


    /**
     *  Ensures that this collection contains the specified element.
     *  @param o element whose presence in this collection is to be ensured.
     *  @return true if this collection changed as a result of the call
     */
    public boolean add(Person o) {
	fireMspCollectionEvent(new MspCollectionEvent(this, "add"));
        return elements.add(o);
    }

    /**
     *  Removes a single instance of the specified element from this 
     *  collection, if it is present.
     *  @param o element to be removed from this collection, if present.
     *  @return true if this collection changed as a result of the call
     */
    public boolean remove(Person o) {
        fireMspCollectionEvent(new MspCollectionEvent(this, "remove"));
        return elements.remove(o);
    }

    /**
     *  Removes all of the elements from this collection.
     */
    public void clear() {
        fireMspCollectionEvent(new MspCollectionEvent(this, "clear"));
        elements.clear();
    }
    
    /** Sort by name
     *  @return person collection sorted by name
     */
    public PersonCollection sortByName() {
        PersonCollection sortedPeople = new PersonCollection();
        PersonIterator outer = iterator();
        while (outer.hasNext()) {
            outer.next();
            String leastName = "ZZZZZZZZZZZZZZZZZZZ";
            Person leastPerson = null;
            PersonIterator inner = iterator();
            while (inner.hasNext()) {
                Person tempPerson = inner.next();
                String name = tempPerson.getName();
                if ((name.compareTo(leastName) < 0) && !sortedPeople.contains(tempPerson)) {
                    leastName = name;
                    leastPerson = tempPerson;
                }
            }
            sortedPeople.add(leastPerson);
        }
        
        return sortedPeople;
    }
}
