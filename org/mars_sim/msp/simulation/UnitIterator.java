/**
 * Mars Simulation Project
 * UnitIterator.java
 * @version 2.73 2001-10-24
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.util.*;

/** 
 *  The UnitIterator class is an iterator over a UnitCollection. 
 */
public class UnitIterator implements Iterator {
    
    private Iterator iterator;

    /**
     *  Constructor
     */
    UnitIterator(Collection collection) {
        iterator = collection.iterator();
    } 

    /**
     *  Returns true if the iteration has more elements.
     *  @return true if the iterator has more elements.
     */
    public boolean hasNext() {
        return iterator.hasNext();
    }

    /**
     *  Returns the next element in the iteration.
     *  @return the next element in the iteration.
     */
    public Object next() {
        return iterator.next();
    }
 
    /**
     *  Removes from the underlying collection the 
     *  last element returned by the iterator.
     */
    public void remove() {
        iterator.remove();
    }
 
    /**
     *  Returns the next Unit in the iteration.
     *  @return the next Unit in the iteration.
     */
    public Unit nextUnit() {
        return (Unit) iterator.next();
    }

    /**
     *  Returns the next Person in the iteration.
     *  @return the next Person in the iteration.
     */
    public Person nextPerson() {
        return (Person) iterator.next();
    }

    /**
     *  Returns the next Settlement in the iteration.
     *  @return the next Settlement in the iteration.
     */
    public Settlement nextSettlement() {
        return (Settlement) iterator.next();
    }

    /**
     *  Returns the next Vehicle in the iteration.
     *  @return the next Vehicle in the iteration.
     */
    public Vehicle nextVehicle() {
        return (Vehicle) iterator.next();
    }   
}
