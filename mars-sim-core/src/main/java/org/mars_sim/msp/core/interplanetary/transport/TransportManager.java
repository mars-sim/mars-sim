/*
 * Mars Simulation Project
 * TransportManager.java
 * @date 2023-05-01
 * @author Scott Davis
 */
package org.mars_sim.msp.core.interplanetary.transport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.configuration.Scenario;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.events.ScheduledEventManager;
import org.mars_sim.msp.core.interplanetary.transport.resupply.ResupplyUtil;
import org.mars_sim.msp.core.interplanetary.transport.settlement.ArrivingSettlement;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityFactory;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.Temporal;

/**
 * A manager for interplanetary transportation.
 */
public class TransportManager implements Serializable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(TransportManager.class.getName());

	private List<Transportable> transportItems;

	private transient HistoricalEventManager eventManager;

	private ScheduledEventManager futures;

	/**
	 * Constructor.
	 * 
	 * @param scenario 
	 * @param raFactory 
	 */
	public TransportManager(Simulation sim) {
		this.eventManager = sim.getEventManager();

		Transportable.initalizeInstances(sim.getMasterClock().getMarsClock(), this);

		// Initialize data
		transportItems = new ArrayList<>();

		this.futures = new ScheduledEventManager(sim.getMasterClock().getMarsClock());
	}
	
	public void init() {
		transportItems.addAll(ResupplyUtil.loadInitialResupplyMissions());
	}
	
	/**
	 * Adds any arriving Settlements that are defined in a Scenario.
	 * 
	 * @param scenario
	 * @param settlementConfig 
	 * @param raFactory 
	 */
	public void loadArrivingSettments(Scenario scenario, SettlementConfig settlementConfig,
									  ReportingAuthorityFactory raFactory) {
		// Create initial arriving settlements.
		for (ArrivingSettlement a : scenario.getArrivals()) {
			// Check the defines values are correct; these throw exception
			settlementConfig.getItem(a.getTemplate());
			
			if (raFactory.getItem(a.getSponsorCode()) == null) {
				throw new IllegalArgumentException("Arriving settlement has a incorrect RAcode " + a.getSponsorCode());
			}
			
			a.scheduleLaunch(futures);
			logger.config("Scheduling a new settlement called '" + a.getName() + "' to arrive at Sol "
						+ a.getArrivalDate().getTrucatedDateTimeStamp());
			transportItems.add(a);
		}
	}
	
	/**
	 * Adds a new transport item.
	 * 
	 * @param transportItem the new transport item.
	 */
	public void addNewTransportItem(Transportable transportItem) {
		transportItems.add(transportItem);

		fireEvent(transportItem, EventType.TRANSPORT_ITEM_CREATED);
	}

	/**
	 * Gets the transport items 
	 * 
	 * @return transportables.
	 */
	public List<Transportable> getTransportItems() {
		return transportItems;
	}

	/**
	 * Time passing.
	 *
	 * @param pulse Pulse of the simulation
	 * @throws Exception if error.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		futures.timePassing(pulse);
		return true;
	}

	/**
	 * Fire an event concerning a transport item
	 * @param transportItem
	 * @param action
	 * @param reason
	 */
	public void fireEvent(Transportable transportItem, EventType action) {
		HistoricalEvent deliverEvent = new TransportEvent(transportItem, action,
										transportItem.getSettlementName());
		eventManager.registerNewEvent(deliverEvent);
		logger.info("A transport item launched on " + transportItem.toString());
	}

	/**
	 * Reset links to the managers classes after a reload. This also reinit's the TransportItems
	 */
	public void reinitalizeInstances(Simulation sim) {
		this.eventManager = sim.getEventManager();
		Transportable.initalizeInstances(sim.getMasterClock().getMarsClock(), this);

		UnitManager um = sim.getUnitManager();
		for(Transportable t : transportItems) {
			t.reinit(um);
		}
	}

	public ScheduledEventManager getFutureEvents() {
		return futures;
	}
}
