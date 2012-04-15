/**
 * Mars Simulation Project
 * ResupplyMissionTemplate.java
 * @version 3.00 2010-08-25
 * @author Scott Davis
 */
package org.mars_sim.msp.core.interplanetary.transport.resupply;

import java.io.Serializable;

/**
 * A template for resupply mission information.
 */
public class ResupplyMissionTemplate implements Serializable {

    // Data members
    private String name;
    private double arrivalTime;
    
    /**
     * Constructor
     */
    public ResupplyMissionTemplate(String name, double arrivalTime) {
        this.name = name;
        this.arrivalTime = arrivalTime;
    }

    /**
     * Gets the template name.
     * @return name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the arrival time from the start of the simulation.
     * @return arrival time (Sols).
     */
    public double getArrivalTime() {
        return arrivalTime;
    }
}