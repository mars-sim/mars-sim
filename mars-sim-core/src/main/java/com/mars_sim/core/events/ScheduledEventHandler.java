/*
 * Mars Simulation Project
 * ScheduledEventHandler.java
 * @date 2023-05-01
 * @author Barry Evans
 */
package com.mars_sim.core.events;

import java.io.Serializable;

import com.mars_sim.core.time.MarsTime;

/**
 * Represents a handler that is notified when a scheduled event arrives.
 */
public interface ScheduledEventHandler extends Serializable {

    /**
     * Gets a description of this future event.
     * 
     * @return
     */
    String getEventDescription();

    /**
     * The event has arrived so it can be executed. The execution can return a positive number that
     * indicates the event should be rescheduled at millisols in the future.
     * 
     * @param currentTime The time when this handler was triggered
     * @return The millisols to a rescheduled event.
     */
    int execute(MarsTime currentTime);
}
