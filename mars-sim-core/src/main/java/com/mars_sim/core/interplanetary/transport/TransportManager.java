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
import com.mars_sim.core.events.HistoricalEventCategory;
import com.mars_sim.core.events.HistoricalEventManager;
import com.mars_sim.core.events.ScheduledEventManager;
import com.mars_sim.core.interplanetary.transport.resupply.Resupply;
import com.mars_sim.core.interplanetary.transport.resupply.ResupplyUtil;
import com.mars_sim.core.interplanetary.transport.settlement.ArrivingSettlement;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.EventType;
import com.mars_sim.core.structure.SettlementTemplateConfig;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MasterClock;
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

	private MasterClock clock;

	/**
	 * Constructor.
	 */
	public TransportManager(Simulation sim) {
		this.eventManager = sim.getEventManager();
		this.clock = sim.getMasterClock();

		Transportable.initalizeInstances(this);

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
	 * @param settlementTemplateConfig
	 * @param raFactory 
	 */
	public void loadArrivingSettments(Scenario scenario, SettlementTemplateConfig settlementTemplateConfig,
									  AuthorityFactory raFactory) {
		// Create initial arriving settlements.
		for (ArrivingSettlement a : scenario.getArrivals()) {
			// Check the defines values are correct; these throw exception
			settlementTemplateConfig.getItem(a.getTemplate());
			
			if (raFactory.getItem(a.getSponsorCode()) == null) {
				throw new IllegalArgumentException("Arriving settlement has a incorrect RAcode " + a.getSponsorCode());
			}
			
			a.scheduleLaunch();
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

		fireEvent(TransportManager.createEvent(transportItem, EventType.TRANSPORT_ITEM_CREATED));
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
	 * Create an event concerning a transport item but this is not fired yet
	 * 
	 * @param transportItem
	 * @param action
	 * @param reason
	 */
	public static HistoricalEvent createEvent(Transportable transportItem, EventType action) {
		return new HistoricalEvent(HistoricalEventCategory.TRANSPORT, action, transportItem,
						transportItem.getName(), "", "", transportItem,
						(transportItem instanceof Resupply r ? r.getSettlement() : null),
						transportItem.getLandingLocation()
		);
	}

	/**
	 * Fires a historical event.
	 * 
	 */
	void fireEvent(HistoricalEvent event) {
		eventManager.registerNewEvent(event);

		var transportItem = (Transportable) event.getSource();
		logger.info(transportItem.getSettlementName() + " - A transport item was launched on "
				+ transportItem.getLaunchDate() + ", arriving at " + transportItem.getArrivalDate() + ".");
	}

	/**
	 * Resets links to the managers classes after a reload. This also reinit's the TransportItems
	 */
	public void reinitalizeInstances(Simulation sim) {
		this.eventManager = sim.getEventManager();
		Transportable.initalizeInstances(this);

		UnitManager um = sim.getUnitManager();
		for(Transportable t : transportItems) {
			t.reinit(um);
		}
	}

	public ScheduledEventManager getFutureEvents() {
		return futures;
	}

	/**
	 * Get the current mars time
	 * @return
	 */
	public MarsTime getMarsTime() {
		return clock.getMarsTime();
	}
}
