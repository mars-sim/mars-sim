/**
 * Mars Simulation Project
 * TransportEvent.java
 * @version 3.1.0 2017-10-05
 * @author Scott Davis
 */
package org.mars_sim.msp.core.interplanetary.transport;

import java.io.Serializable;

import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.core.person.EventType;

/**
 * A historical event for interplanetary transportation.
 */
public class TransportEvent
extends HistoricalEvent implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
	/**
	 * Constructor.
	 * @param transportItem the transport item.
	 * @param eventType the event type string.
	 * @param description of the event.
	 */
	public TransportEvent(Transportable transportItem, EventType eventType, String location, String description) {
		super(
			HistoricalEventCategory.TRANSPORT,
			eventType,
			transportItem,
			null, // TODO : define the sponsor 
			location,
			description
		);
	}
}