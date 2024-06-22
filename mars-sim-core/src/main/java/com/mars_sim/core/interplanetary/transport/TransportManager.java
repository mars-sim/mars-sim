/*
 * Mars Simulation Project
 * TransportManager.java
 * @date 2023-05-01
 * @author Scott Davis
 */
package com.mars_sim.core.interplanetary.transport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.authority.AuthorityFactory;
import com.mars_sim.core.configuration.Scenario;
import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.events.HistoricalEventManager;
import com.mars_sim.core.events.ScheduledEventManager;
import com.mars_sim.core.interplanetary.transport.resupply.ResupplyUtil;
import com.mars_sim.core.interplanetary.transport.settlement.ArrivingSettlement;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.EventType;
import com.mars_sim.core.structure.SettlementConfig;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;

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

		Transportable.initalizeInstances(sim.getMasterClock(), this);

		// Initialize data
		transportItems = new ArrayList<>();

		this.futures = new ScheduledEventManager(sim.getMasterClock());
	}
	
	public void init(Simulation sim) {
		transportItems.addAll(ResupplyUtil.loadInitialResupplyMissions(sim));
	}
	
	/**
	 * Adds any arriving Settlements that are defined in a Scenario.
	 * 
	 * @param scenario
	 * @param settlementConfig 
	 * @param raFactory 
	 */
	public void loadArrivingSettments(Scenario scenario, SettlementConfig settlementConfig,
									  AuthorityFactory raFactory) {
		// Create initial arriving settlements.
		for (ArrivingSettlement a : scenario.getArrivals()) {
			// Check the defines values are correct; these throw exception
			settlementConfig.getItem(a.getTemplate());
			
			if (raFactory.getItem(a.getSponsorCode()) == null) {
				throw new IllegalArgumentException("Arriving settlement has a incorrect RAcode " + a.getSponsorCode());
			}
			
			a.scheduleLaunch(futures);
			logger.config("Scheduling a new settlement called '" + a.getName() + "' to arrive at Sol "
						+ a.getArrivalDate().getTruncatedDateTimeStamp());
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
	 * Fires an event concerning a transport item.
	 * 
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
	 * Resets links to the managers classes after a reload. This also reinit's the TransportItems
	 */
	public void reinitalizeInstances(Simulation sim) {
		this.eventManager = sim.getEventManager();
		Transportable.initalizeInstances(sim.getMasterClock(), this);

		UnitManager um = sim.getUnitManager();
		for(Transportable t : transportItems) {
			t.reinit(um);
		}
	}

	public ScheduledEventManager getFutureEvents() {
		return futures;
	}
}
