/**
 * Mars Simulation Project
 * HistoricalEventManager.java
 * @version 2.81 2007-08-20
 * @author Barry Evans
 */

package org.mars_sim.msp.simulation.events;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.time.MarsClock;

/**
 * This class provides a manager that maintains a model of the events that
 * have occured during the current simulation run. It provides support for a
 * listener pattern so the external objects can be notified when new events have
 * been registered.
 * The manager maintains an ordered list in terms of descreasing time, i.e.
 * most recent event first.
 * It should be noted that the throughput of new events of the manager can be
 * in the order of 100 event per simulation tick.
 */
public class HistoricalEventManager {

    /** This defines the maximum number of events that are stored.
     *  It should be a standard property.
     */
    private final static int TRANSIENT_EVENTS = 1000;

	// Event categories
	public final static String MEDICAL = "Medical";
	public final static String MALFUNCTION = "Malfunction";
	public final static String MISSION = "Mission";
	public final static String TASK = "Task";
	public final static String SUPPLY = "Supply";

    private List<HistoricalEventListener> listeners = new ArrayList<HistoricalEventListener>();
    private List<HistoricalEvent> events = new LinkedList<HistoricalEvent>();
    private MarsClock mainClock;

    /**
     * Create a new EventManager that represnets a particular simulation.
     */
    public HistoricalEventManager() {

        // The main clock is not initialised until the simulation start
        this.mainClock = null;
    }

    /**
     * Add a historical event listener
     * @param newListener listener to add.
     */
    public void addListener(HistoricalEventListener newListener) {
    	if (!listeners.contains(newListener)) listeners.add(newListener);
    }
    
    /**
     * Removes a historical event listener.
     * @param oldListener listener to remove.
     */
    public void removeListener(HistoricalEventListener oldListener) {
    	if (listeners.contains(oldListener)) listeners.remove(oldListener);
    }

    /**
     * Get the event at a specified index.
     * @param index Index of event to retrieve.
     * @return Historical event.
     */
    public HistoricalEvent getEvent(int index) {
        return events.get(index);
    }

    /**
     * An new event needs registering with the manager. The event will be
     * timestamped with the current clock time and inserted at position zero.
     *
     * @param newEvent The event to register.
     */
    public void registerNewEvent(HistoricalEvent newEvent) {

        // Make space for the new event.
        if (events.size() >= TRANSIENT_EVENTS) {
            int excess = events.size() - (TRANSIENT_EVENTS - 1);
            removeEvents(events.size() - excess, excess);
        }

        if (mainClock == null) {
            mainClock = Simulation.instance().getMasterClock().getMarsClock();
        }
        newEvent.setTimestamp((MarsClock) mainClock.clone());
        events.add(0, newEvent);

        Iterator<HistoricalEventListener> iter = listeners.iterator();
        while (iter.hasNext()) iter.next().eventAdded(0, newEvent);
    }

    /**
     * An event is removed from the list.
     *
     * @param index Index of the event to be removed.
     * @param number Number to remove.
     */
    private void removeEvents(int index, int number) {

        // Remove the rows
        for(int i = index; i < (index + number); i++) {
            events.remove(i);
        }

        Iterator<HistoricalEventListener> iter = listeners.iterator();
        while(iter.hasNext()) iter.next().eventsRemoved(index, index + number);
    }

    /**
     * Get the number of events in the manager.
     * @return Stored evnet count.
     */
    public int size() {
        return events.size();
    }
}