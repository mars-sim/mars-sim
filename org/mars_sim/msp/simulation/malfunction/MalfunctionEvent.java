package org.mars_sim.msp.simulation.malfunction;

import org.mars_sim.msp.simulation.events.HistoricalEvent;
import org.mars_sim.msp.simulation.Unit;

/**
 * This class represents the historical action of a Malfunciton occuring or
 * being resolved.
 */
public class MalfunctionEvent extends HistoricalEvent {

    final private static String FIXED_TYPE = "Malfunction fixed";
    final private static String UNFIXED_TYPE = "Malfunction occurred";

    /**
     * Create an event associated to a Malfunction.
     *
     * @param unit Unit with the malfunction.
     * @param malfunction Problem that has occured.
     * @param fixed Is the malfunction resolved.
     */
    public MalfunctionEvent(Unit unit, Malfunction malfunction, boolean fixed) {
        super((fixed ? FIXED_TYPE : UNFIXED_TYPE), unit, malfunction.getName());
    }
}