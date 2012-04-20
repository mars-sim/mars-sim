package org.mars_sim.msp.core.malfunction;

import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventManager;

/**
 * This class represents the historical action of a Malfunction occuring or
 * being resolved.
 */
public class MalfunctionEvent extends HistoricalEvent {

    final private static String FIXED_TYPE = "Malfunction fixed";
    final private static String UNFIXED_TYPE = "Malfunction occurred";

    /**
     * Create an event associated to a Malfunction.
     *
     * @param entity Malfunctionable entity with problem.
     * @param malfunction Problem that has occurred.
     * @param fixed Is the malfunction resolved.
     */
    public MalfunctionEvent(Malfunctionable entity, Malfunction malfunction, boolean fixed) {
        super(HistoricalEventManager.MALFUNCTION, (fixed ? FIXED_TYPE : UNFIXED_TYPE), 
        	entity, malfunction.getName() + (fixed? " fixed" : " occurred"));
    }
}