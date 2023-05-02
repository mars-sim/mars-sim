/**
 * Mars Simulation Project
 * ScheduledEventHandler.java
 * @date 2023-05-01
 * @author Barry Evans
 */
package org.mars_sim.msp.core.events;

import java.io.Serializable;

import org.mars_sim.msp.core.time.MarsClock;

/**
 * Represents a handler that is notified when a scheduled event arrives.
 */
public interface ScheduledEventHandler extends Serializable {

    /**
     * Get a description of this future event
     * @return
     */
    String getEventDescription();

    /**
     * The event has arrived so it can be executed. The exeuction can return a positive number that
     * indicates the event should be rescheduled at millisols in the future.
     * @param currentTime The time when this handler was triggered
     * @return The millisols to a rescheduled event.
     */
    int execute(MarsClock currentTime);
}
