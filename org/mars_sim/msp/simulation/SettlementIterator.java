/**
 * Mars Simulation Project
 * SettlementIterator.java
 * @version 2.73 2001-10-24
 * @author Scott Davis 
 */

package org.mars_sim.msp.simulation;

/** 
 *  The SettlementIterator class is an iterator over a SettlementCollection. 
 */
public interface SettlementIterator {
 
    /**
     *  Returns the next element in the iteration.
     *  @return the next element in the iteration.
     */
    public Settlement next();

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
