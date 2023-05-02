/**
 * Mars Simulation Project
 * ResupplySchedule.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.interplanetary.transport.resupply;

import java.io.Serializable;

import org.mars_sim.msp.core.interplanetary.transport.resupply.ResupplyConfig.SupplyManifest;

/**
 * A template for resupply mission information.
 */
public class ResupplySchedule implements Serializable {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

    // Data members
    private String name;
    private double arrivalTime;
    private SupplyManifest supplies;
    private int frequency;

    
    /**
     * Constructor
     * @param maximum
     * @param frequency
     */
    public ResupplySchedule(String name, double arrivalTime, SupplyManifest supplies, int frequency) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.supplies = supplies;
        this.frequency = frequency;
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
    public double getFirstArrival() {
        return arrivalTime;
    }

    /**
     * The payload to be delivered every schedule
     */
    public SupplyManifest getManifest() {
        return supplies;
    }

    /**
     * The Sols between each arrival on this schedule.
     * @return Value of -1 means there is no repeating delivery
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * The number of resupply missions that are active with this schedule.
     * @return
     */
    public int getActiveMissions() {
        return  Math.floorDiv(ResupplyUtil.getAverageTransitTime(), frequency) + 1;
    }
}
