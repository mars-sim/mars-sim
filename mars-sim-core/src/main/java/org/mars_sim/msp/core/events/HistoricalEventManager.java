/*
 * Mars Simulation Project
 * HistoricalEventManager.java
 * @date 2022-09-24
 * @author Barry Evans
 */

package org.mars_sim.msp.core.events;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.time.MarsClock;


/**
 * This class provides a manager that maintains a model of the events that have
 * occurred during the current simulation run. It provides support for a
 * listener pattern so the external objects can be notified when new events have
 * been registered. The manager maintains an ordered list in terms of decreasing
 * time, i.e. most recent event first. It should be noted that the throughput of
 * new events of the manager can be in the order of 100 event per simulation
 * tick.
 */
public class HistoricalEventManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/**
	 * This defines the maximum number of events that are stored. It should be a
	 * standard property.
	 */
	private final static int TRANSIENT_EVENTS = 50;

	private transient List<HistoricalEventListener> listeners;

	// Static list - don't want to be serialized
	private static List<HistoricalEvent> lastEvents = new CopyOnWriteArrayList<>();

	// Note : marsClock CAN'T be initialized until the simulation start
	private MarsClock marsClock;

	/**
	 * Creates a new EventManager that represents a particular simulation.
	 */
	public HistoricalEventManager() {
		listeners = new CopyOnWriteArrayList<>();
	}

	/**
	 * Adds a historical event listener
	 *
	 * @param newListener listener to add.
	 */
	public void addListener(HistoricalEventListener newListener) {
		if (listeners == null)
			listeners = new CopyOnWriteArrayList<>();
		if (!listeners.contains(newListener))
			listeners.add(newListener);
	}

	/**
	 * Removes a historical event listener.
	 *
	 * @param oldListener listener to remove.
	 */
	public void removeListener(HistoricalEventListener oldListener) {
		if (listeners.contains(oldListener))
			listeners.remove(oldListener);
	}

	public boolean isSameEvent(HistoricalEvent newEvent) {
		if (lastEvents != null && !lastEvents.isEmpty()) {
			for (HistoricalEvent e : lastEvents) {
				if (e.getType() == newEvent.getType()
						&& e.getCategory() == newEvent.getCategory()
						&& e.getSource().equals(newEvent.getSource())
						&& e.getWhatCause().equals(newEvent.getWhatCause())
						&& e.getWhileDoing().equals(newEvent.getWhileDoing())
						&& e.getWho().equals(newEvent.getWho())
						&& e.getContainer().equals(newEvent.getContainer())
						&& e.getCoordinates().equals(newEvent.getCoordinates())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * An new event needs registering with the manager. The event will be time
	 * stamped with the current clock time and inserted at position zero.
	 *
	 * @param newEvent The event to register.
	 */
	public void registerNewEvent(HistoricalEvent newEvent) {
		if (newEvent.getCategory() == HistoricalEventCategory.TASK)
			return;

		EventType type = newEvent.getType();

		if (type == EventType.MISSION_START)
			return;
		else if (type == EventType.MISSION_JOINING)
			return;
		else if (type == EventType.MISSION_FINISH)
			return;
		else if (type == EventType.MISSION_NOT_ENOUGH_RESOURCES)
			return;
		else if (isSameEvent(newEvent))
			return;


		if (marsClock == null)
			marsClock = Simulation.instance().getMasterClock().getMarsClock();
		newEvent.setTimestamp(new MarsClock(marsClock));

		if (lastEvents == null)
			lastEvents = new CopyOnWriteArrayList<>();

		synchronized(lastEvents) {
			lastEvents.add(newEvent);
			if (lastEvents.size() > TRANSIENT_EVENTS)
				lastEvents.remove(0);
		}

		if (listeners != null) {
			for(HistoricalEventListener l : listeners) {
				l.eventAdded(newEvent);
			}
		}	
	}


	/**
	 * Gets the recent historical events.
	 * 
	 * @return
	 */
	public List<HistoricalEvent> getEvents() {
		return lastEvents;
	}
}
