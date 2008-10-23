/**
 * Mars Simulation Project
 * ConstructionListener.java
 * @version 2.85 2008-10-22
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.construction;

/**
 * Interface for a construction event listener.
 */
public interface ConstructionListener {

    /**
     * Catch construction update event.
     * @param event the mission event.
     */
    public void constructionUpdate(ConstructionEvent event);
}