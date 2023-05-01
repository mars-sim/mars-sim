/**
 * Mars Simulation Project
 * ResupplyMissionTemplate.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.interplanetary.transport.resupply;

import java.io.Serializable;

import org.mars_sim.msp.core.interplanetary.transport.resupply.ResupplyConfig.SupplyManifest;

/**
 * A template for resupply mission information.
 */
public class ResupplyMissionTemplate implements Serializable {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

    // Data members
    private String name;
    private double arrivalTime;
    private SupplyManifest supplies;
    
    /**
     * Constructor
     */
    public ResupplyMissionTemplate(String name, double arrivalTime, SupplyManifest supplies) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.supplies = supplies;
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

    public SupplyManifest getManifest() {
        return supplies;
    }
}
