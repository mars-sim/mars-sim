/**
 * Mars Simulation Project
 * TransportEvent.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.interplanetary.transport;

import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.person.EventType;

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
				(transportItem instanceof Resupply ? ((Resupply) transportItem).getSettlement() : null),
				transportItem.getSettlementName(),
				transportItem.getLandingLocation()
		);
	}
}
