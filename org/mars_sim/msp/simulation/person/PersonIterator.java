/**
 * Mars Simulation Project
 * PersonIterator.java
 * @version 2.74 2002-01-13
 * @author Edgar Crisostomo 
 */

package org.mars_sim.msp.simulation.person;

/** 
 *  The PersonIterator class is an iterator over a PersonCollection. 
 */
public interface PersonIterator {
 
    /**
     *  Returns the next element in the iteration.
     *  @return the next element in the iteration.
     */
    public Person next();

    /**
     *  Returns true if the iteration has more elements.
     *  @return true if the iterator has more elements.
     */
    public boolean hasNext();

    /**
     *  Removes from the underlying collection the 
     *  last element returned by the iterator.
     */
    public void remove();
}
