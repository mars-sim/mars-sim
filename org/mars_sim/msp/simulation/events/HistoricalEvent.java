/**
 * Mars Simulation Project
 * MSPEvent.java
 * @version 2.75 2004-01-14
 * @author Barry Evans
 */

package org.mars_sim.msp.simulation.events;

import org.mars_sim.msp.simulation.time.MarsClock;

/**
 * This class represents a time based event that has occuried in the simulation.
 * It is aimed at being subclassed to reflect the real simulation specific
 * events.
 * An event consists of a time stamp when it occured, a description, an
 * optional Unit that is the source of the event and an optional Object that has
 * triggered the event.
 */
public abstract class HistoricalEvent {

	private String		category;       // Category of event (see HistoricalEventManager)
    private String      type;           // Type of event
    private String      description;    // Long description
    private MarsClock   timestamp;      // Time event occuried
    private Object      source;         // Source of event may be null.

    /**
     * Construct an event with the appropriate information. The time is not
     * defined until the evnet is registered with the Event Manager.
     *
     * @param category Category of event.
     * @param type Type of event.
     * @param source The object that has produced the event, if this is null
     * then it is a global simulation event. It could be a Unit or a Facility.
     * @param description Long description of event.
     *
     * @see org.mars_sim.msp.simulation.events.HistoricalEventManager#registerNewEvent(HistoricalEvent)
     */
    public HistoricalEvent(String category, String type, Object source, String description) {
    	this.category = category;
        this.type = type;
        this.source = source;
        this.description = description;
    }

    /**
     * Set the timestamp for this event.
     * @see org.mars_sim.msp.simulation.events.HistoricalEventManager#registerNewEvent(HistoricalEvent)
     */
    void setTimestamp(MarsClock timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Get description.
     * @return Description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the Unit source.
     * @return Object as the source of event.
     */
    public Object getSource() {
        return source;
    }

    /**
     * Get event time.
     * @return Time the event happened
     */
    public MarsClock getTimestamp() {
        return timestamp;
    }

    /**
     * Get the type of event.
     * @return String representing the type.
     */
    public String getType() {
        return type;
    }
    
    /*
     * Gets the category of the event.
     * @return event category (see HistoricalEventManager)
     */
    public String getCategory() {
    	return category;
    }
}