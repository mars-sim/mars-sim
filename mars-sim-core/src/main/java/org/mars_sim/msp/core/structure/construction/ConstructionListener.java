/**
 * Mars Simulation Project
 * ConstructionListener.java
 * @version 3.1.0 2017-09-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.construction;

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