/*
 * Mars Simulation Project
 * ResupplySchedule.java
 * @date 2024-08-10
 * @author Scott Davis
 */
package com.mars_sim.core.interplanetary.transport.resupply;

import java.io.Serializable;

import com.mars_sim.core.time.EventSchedule;

/**
 * A template for resupply mission information.
 */
public class ResupplySchedule implements Serializable {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

    // Data members
    private String name;
    private ResupplyManifest supplies;
    private EventSchedule schedule;

    
    /**
     * Constructor.
     * 
     * @param maximum
     * @param frequency
     */
    public ResupplySchedule(String name, EventSchedule schedule, ResupplyManifest supplies) {
        this.name = name;
        this.schedule = schedule;
        this.supplies = supplies;
    }

    /**
     * Gets the template name.
     * 
     * @return name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the schedule for this resupply.
     * 
     * @return arrival time (Sols).
     */
    public EventSchedule getSchedule() {
        return schedule;
    }

    /**
     * The payload to be delivered every schedule.
     */
    public ResupplyManifest getManifest() {
        return supplies;
    }

    /**
     * The number of resupply missions that are active with this schedule.
     * 
     * @return
     */
    public int getActiveMissions() {
        int frequency = schedule.getFrequency();
        if (frequency <= 0) {
            // This is a one off so only schedule a single instance
            return 1;
        }
        return  Math.floorDiv(ResupplyUtil.getAverageTransitTime(), frequency) + 1;
    }
}
