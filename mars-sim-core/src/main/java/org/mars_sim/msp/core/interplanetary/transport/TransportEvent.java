/**
 * Mars Simulation Project
 * TransportEvent.java
 * @version 3.07 2014-12-06
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
	public TransportEvent(Transportable transportItem, EventType eventType, String description) {
		super(
			HistoricalEventCategory.TRANSPORT,
			eventType,
			transportItem, 
			description
		);
	}
}