/**
 * Mars Simulation Project
 * EquipmentIterator.java
 * @version 2.74 2002-02-21
 * @author Scott Davis 
 */

package org.mars_sim.msp.simulation.equipment;

/** 
 *  The EquipmentIterator class is an iterator over a EquipmentCollection. 
 */
public interface EquipmentIterator {
 
    /**
     *  Returns the next element in the iteration.
     *  @return the next element in the iteration.
     */
    public Equipment next();

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
