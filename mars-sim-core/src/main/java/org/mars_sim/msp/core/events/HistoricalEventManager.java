/**
 * Mars Simulation Project
 * HistoricalEventManager.java
 * @version 3.1.0 2017-09-09
 * @author Barry Evans
 */

package org.mars_sim.msp.core.events;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.narrator.Narrator;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.time.MarsClock;

import java.io.Serializable;
import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;


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
//	private final static int TRANSIENT_EVENTS = 5000;

	private transient List<HistoricalEventListener> listeners;

	// Static list - don't want to be serialized
	private volatile static List<HistoricalEvent> lastEvents = new ArrayList<>();

	// The following list cannot be static since it needs to be serialized
	private List<SimpleEvent> eventsRegistry;

	// The following 4 list cannot be static since they need to be serialized
	private List<String> whatList;
	private List<String> whileDoingList;
	private List<String> whoList;
	private List<String> loc0List;
	private List<String> loc1List;

	// Note : marsClock CAN'T be initialized until the simulation start
	private MarsClock marsClock;

	/**
	 * Create a new EventManager that represents a particular simulation.
	 */
	public HistoricalEventManager() {
		listeners = new ArrayList<HistoricalEventListener>();
		eventsRegistry = new ArrayList<>();
		initMaps();
	}

	private void initMaps() {
		whatList = new ArrayList<>();
		whileDoingList = new ArrayList<>();
		whoList = new ArrayList<>();
		loc0List = new ArrayList<>();
		loc1List = new ArrayList<>();
	}

	/**
	 * Add a historical event listener
	 * 
	 * @param newListener listener to add.
	 */
	public void addListener(HistoricalEventListener newListener) {
		if (listeners == null)
			listeners = new ArrayList<HistoricalEventListener>();
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

//	/**
//	 * Get the event at a specified index.
//	 * @param index Index of event to retrieve.
//	 * @return Historical event.
//	 */
//	public HistoricalEvent getEvent(int index) {		
//		return events.get(index);
//	}

	/**
	 * Get the event at a specified index.
	 * 
	 * @param index Index of event to retrieve.
	 * @return Historical event.
	 */
	public SimpleEvent getEvent(int index) {
		return eventsRegistry.get(index);
	}
	
	public boolean isSameEvent(HistoricalEvent newEvent) {
		if (lastEvents != null && !lastEvents.isEmpty()) {
			for (HistoricalEvent e : lastEvents) {
				if (e.getType() == newEvent.getType() && e.getCategory() == newEvent.getCategory()
						&& e.getWhatCause().equals(newEvent.getWhatCause())
						&& e.getWhileDoing().equals(newEvent.getWhileDoing()) 
						&& e.getWho().equals(newEvent.getWho())
						&& e.getLocation0().equals(newEvent.getLocation0())
						&& e.getLocation1().equals(newEvent.getLocation1())) {
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
	public synchronized void registerNewEvent(HistoricalEvent newEvent) {
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

		if (lastEvents == null)
			lastEvents = new ArrayList<>();

		lastEvents.add(newEvent);
		if (lastEvents.size() > 7)
			lastEvents.remove(0);

		// check if event is MALFUNCTION or MEDICAL, save it for notification box
		// display
		// Make space for the new event.
//			if (events.size() >= TRANSIENT_EVENTS) {
//				int excess = events.size() - (TRANSIENT_EVENTS - 1);
//				removeEvents(events.size() - excess, excess);
//			}
		
		if (marsClock == null)
			marsClock = Simulation.instance().getMasterClock().getMarsClock();

		MarsClock timestamp = (MarsClock) marsClock.clone();

		if (timestamp == null)
			throw new IllegalStateException("timestamp is null");

		newEvent.setTimestamp(timestamp);

		SimpleEvent se = convert2SimpleEvent(newEvent, timestamp);

		if (listeners == null) {
			listeners = new ArrayList<HistoricalEventListener>();
		}

		Iterator<HistoricalEventListener> iter = listeners.iterator();
		while (iter.hasNext()) {
			HistoricalEventListener l = iter.next();
			l.eventAdded(0, se, newEvent);
		}
	}

	private SimpleEvent convert2SimpleEvent(HistoricalEvent event, MarsClock timestamp) {
		short missionSol = (short) (timestamp.getMissionSol());//event.getTimestamp().getMissionSol());
		float millisols = (float) (event.getTimestamp().getMillisol());
		byte cat = (byte) (event.getCategory().ordinal());
		byte type = (byte) (event.getType().ordinal());
		short what = (short) (getID(whatList, event.getWhatCause()));
		short whileDoing = (short) (getID(whileDoingList, event.getWhileDoing()));
		short who = (short) (getID(whoList, event.getWho()));
		short loc0 = (short) (getID(loc0List, event.getLocation0()));
		short loc1 = (short) (getID(loc1List, event.getLocation1()));
		short id = (short) CollectionUtils.findSettlementID(event.getAssociatedSettlement());
		
		SimpleEvent se = new SimpleEvent(missionSol, millisols, cat, type, what, whileDoing, who, loc0, loc1, id);
		eventsRegistry.add(0, se);
		return se;
	}

	public int getID(List<String> list, String s) {
		if (list.contains(s)) {
			return list.indexOf(s);
		} else {
			int size = list.size();
			list.add(s);
			return size;
		}
	}
	
	public String getWhat(int id) {
		return whatList.get(id);
	}

	public String getWhileDoing(int id) {
		return whileDoingList.get(id);
	}

	public String getWho(int id) {
		return whoList.get(id);
	}

	public String getLoc0(int id) {
		return loc0List.get(id);
	}

	public String getLoc1(int id) {
		return loc1List.get(id);
	}

	public List<SimpleEvent> getEvents() {
		return eventsRegistry;
	}

	public List<SimpleEvent> getEvents(int settlementID) {
		return eventsRegistry
				.stream()
				.filter(e -> e.getSettlementID() == settlementID)
				.collect(Collectors.toList());
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
//		listeners.clear();
		listeners = null;
		eventsRegistry.clear();
		eventsRegistry = null;
	}
}