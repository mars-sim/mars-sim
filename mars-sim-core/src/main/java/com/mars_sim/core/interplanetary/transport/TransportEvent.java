/*
 * Mars Simulation Project
 * TransportEvent.java
 * @date 2025-10-16
 * @author Scott Davis
 */
package com.mars_sim.core.interplanetary.transport;

import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.events.HistoricalEventCategory;
import com.mars_sim.core.interplanetary.transport.resupply.Resupply;
import com.mars_sim.core.person.EventType;

/**
 * A historical event for interplanetary transportation.
 */
public class TransportEvent extends HistoricalEvent {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param transportItem the transport item.
	 * @param eventType     the event type string.
	 * @param location		the associated settlement where it belongs
	 */
	public TransportEvent(Transportable transportItem, EventType eventType, String location) {
		// Future: Add the type of rocket
		super(HistoricalEventCategory.TRANSPORT, eventType, transportItem, transportItem.getName(),
				null, null,
				// Not very nice
				(transportItem instanceof Resupply r ? r.getSettlement() : null),
				transportItem.getSettlementName(),
				transportItem.getLandingLocation()
		);
	}
}
