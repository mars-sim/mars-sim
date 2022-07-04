/*
 * Mars Simulation Project
 * SimulationListener.java
 * @date 2022-07-04
 * @author Barry Evans
 */
package org.mars_sim.msp.core;

/**
 * Listeners to major event in the simulation
 */
public interface SimulationListener {
    
    /**
     * Event action when a save is completed
     */
    public static final String SAVE_COMPLETED = "SaveCompleted";

    /**
     * The Simulation has performed an event
     */
    void eventPerformed(String action);
}
