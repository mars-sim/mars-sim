/**
 * Mars Simulation Project
 * TransportEvent.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.interplanetary.transport;

import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventType;

/**
 * A historical event for interplanetary transportation.
 */
public class TransportEvent
extends HistoricalEvent {

	// TODO Transport item event types should be an enum.
	public static final String TRANSPORT_ITEM_CREATED = "Transport Item Created";
	public static final String TRANSPORT_ITEM_CANCELLED = "Transport Item Canceled";
	public static final String TRANSPORT_ITEM_LAUNCHED = "Transport Item Launched";
	public static final String TRANSPORT_ITEM_ARRIVED = "Transport Item Arrived";
	public static final String TRANSPORT_ITEM_MODIFIED = "Transport Item Modified";

	/**
	 * Constructor.
	 * @param transportItem the transport item.
	 * @param eventType the event type string.
	 * @param description of the event.
	 */
	public TransportEvent(Transportable transportItem, String eventType, String description) {
		super(
			HistoricalEventType.TRANSPORT,
			eventType,
			transportItem, 
			description
		);
	}
}