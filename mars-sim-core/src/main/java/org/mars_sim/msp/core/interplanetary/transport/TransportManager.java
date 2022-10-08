/*
 * Mars Simulation Project
 * TransportManager.java
 * @date 2022-09-25
 * @author Scott Davis
 */
package org.mars_sim.msp.core.interplanetary.transport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.configuration.Scenario;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.interplanetary.transport.resupply.ResupplyUtil;
import org.mars_sim.msp.core.interplanetary.transport.settlement.ArrivingSettlement;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityFactory;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.Temporal;

/**
 * A manager for interplanetary transportation.
 */
public class TransportManager implements Serializable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** Average transit time for arriving settlements from Earth to Mars (sols). */
	private static int AVG_TRANSIT_TIME = 250;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(TransportManager.class.getName());

	private Collection<Transportable> transportItems;

	private transient HistoricalEventManager eventManager;
	private transient SimulationConfig simConfig;
	private transient Simulation sim;

	/**
	 * Constructor.
	 * 
	 * @param scenario 
	 * @param raFactory 
	 */
	public TransportManager(SimulationConfig simConfig, Simulation sim) {
		this.eventManager = sim.getEventManager();
		this.sim = sim;
		this.simConfig = simConfig;

		// Initialize data
		transportItems = new ArrayList<>();
	}
	
	public void init() {
		transportItems.addAll(ResupplyUtil.loadInitialResupplyMissions());
	}
	
	/**
	 * Add any arriving Settlements that are defined in a Scenario
	 * @param scenario
	 * @param settlementConfig 
	 * @param raFactory 
	 */
	public void loadArrivingSettments(Scenario scenario, SettlementConfig settlementConfig,
									  ReportingAuthorityFactory raFactory) {
		MarsClock now = Simulation.instance().getMasterClock().getMarsClock();
		// Create initial arriving settlements.
		for (ArrivingSettlement a : scenario.getArrivals()) {
			// Check the defines values are correct; these throw exception
			settlementConfig.getItem(a.getTemplate());
			
			if (raFactory.getItem(a.getSponsorCode()) == null) {
				throw new IllegalArgumentException("Arriving settlement has a incorrect RAcode " + a.getSponsorCode());
			}
			
			a.scheduleLaunch(now, AVG_TRANSIT_TIME);
			logger.info("New settlement called " + a.getName() + " arriving in sols "
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
		HistoricalEvent newEvent = new TransportEvent(transportItem, EventType.TRANSPORT_ITEM_CREATED,
				"Mission Control", transportItem.getSettlementName());
		eventManager.registerNewEvent(newEvent);
		logger.info("A transport item was added: " + transportItem.toString());
	}

	/**
	 * Gets the transport items that are planned or in transit.
	 * 
	 * @return transportables.
	 */
	public List<Transportable> getIncomingTransportItems() {
		List<Transportable> incoming = new ArrayList<>();

		Iterator<Transportable> i = transportItems.iterator();
		while (i.hasNext()) {
			Transportable transportItem = i.next();
			TransitState state = transportItem.getTransitState();
			if (TransitState.PLANNED == state || TransitState.IN_TRANSIT == state) {
				incoming.add(transportItem);
			}
		}

		return incoming;
	}

	/**
	 * Gets the transport items that have already arrived.
	 * 
	 * @return transportables.
	 */
	public List<Transportable> getArrivedTransportItems() {
		List<Transportable> arrived = new ArrayList<>();

		Iterator<Transportable> i = transportItems.iterator();
		while (i.hasNext()) {
			Transportable transportItem = i.next();
			TransitState state = transportItem.getTransitState();
			if (TransitState.ARRIVED == state) {
				arrived.add(transportItem);
			}
		}

		return arrived;
	}

	/**
	 * Cancels a transport item.
	 * 
	 * @param transportItem the transport item.
	 */
	public void cancelTransportItem(Transportable transportItem) {
		transportItem.setTransitState(TransitState.CANCELED);
		HistoricalEvent cancelEvent = new TransportEvent(transportItem, EventType.TRANSPORT_ITEM_CANCELLED, "Reserved",
				transportItem.getSettlementName());
		eventManager.registerNewEvent(cancelEvent);
		logger.info("A transport item was cancelled: ");
	}

	/**
	 * Time passing.
	 *
	 * @param pulse Pulse of the simulation
	 * @throws Exception if error.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		for(Transportable transportItem : transportItems) {
			switch(transportItem.getTransitState()) {
				case PLANNED:
					if (MarsClock.getTimeDiff(pulse.getMarsTime(), transportItem.getLaunchDate()) >= 0D) {
						// Transport item is launched.
						transportItem.setTransitState(TransitState.IN_TRANSIT);
						HistoricalEvent deliverEvent = new TransportEvent(transportItem, EventType.TRANSPORT_ITEM_LAUNCHED,
								"Transport item launched", transportItem.getSettlementName());
						eventManager.registerNewEvent(deliverEvent);
						logger.info("Transport item launched: " + transportItem.toString());
					}
					break;

				case IN_TRANSIT:
					if (MarsClock.getTimeDiff(pulse.getMarsTime(), transportItem.getArrivalDate()) >= 0D) {
						// Transport item has arrived on Mars.
						transportItem.setTransitState(TransitState.ARRIVED);
						transportItem.performArrival(simConfig, sim);
						HistoricalEvent arrivalEvent = new TransportEvent(transportItem, EventType.TRANSPORT_ITEM_ARRIVED,
								transportItem.getSettlementName(), "Transport item arrived on Mars");
						eventManager.registerNewEvent(arrivalEvent);
						logger.info("Transport item arrived at " + transportItem.toString());
					}
					break;

				default:
			}
		}
		
		return true;
	}

	/**
	 * Reset links to the managers classes after a reload. This also reinit's the TransportItems
	 */
	public void initalizeInstances(SimulationConfig simConfig, Simulation sim) {
		this.sim = sim;
		this.simConfig = simConfig;

		UnitManager um = sim.getUnitManager();
		for(Transportable t : transportItems) {
			t.reinit(um);
		}
	}
}
