/**
 * Mars Simulation Project
 * PersonCollection.java
 * @version 2.75 2003-05-12
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
    }

    /**
     *  Constructs a PersonCollection object
     *  @param collection collection of elements to copy
     */
    public PersonCollection(PersonCollection collection) {
        PersonIterator iterator = collection.iterator();
        while(iterator.hasNext()) add(iterator.next());
    }

    /**
     *  Returns an iterator over the elements in this collection.
     *  @return an Iterator over the elements in this collection
     */
    public PersonIterator iterator() {
        return new ThisIterator(getUnits());
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
    
    /** 
     * Checks if this person collection contains the same units in the same order
     * as another person collection.
     *
     * @return true if collections contain the same units.
     */
    public boolean equals(Object o) {
        boolean result = true;
        
        if (o instanceof PersonCollection) {
            PersonCollection pc = (PersonCollection) o;
            if (size() == pc.size()) {
                PersonIterator i1 = iterator();
                PersonIterator i2 = pc.iterator();
                while (i1.hasNext()) {
                    if (i1.hasNext() != i2.hasNext()) result = false;
                }
            }
            else result = false;
        }
        else result = false;
        
        return result;
    }
}
