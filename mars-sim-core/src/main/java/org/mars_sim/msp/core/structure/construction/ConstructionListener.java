/**
 * Mars Simulation Project
 * ConstructionListener.java
 * @version 3.1.2 2020-09-02
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
