/**
 * Mars Simulation Project
 * ScheduledEventManager.java
 * @date 2023-01-01
 * @author Barry Evans
 */
package org.mars_sim.msp.core.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.Temporal;

/**
 * This class manages a list off future scheduled events.
 */
public class ScheduledEventManager implements Serializable, Temporal {
    
    /**
     * Represents an event that is scheduled for future execution.
     */
    public class ScheduledEvent implements Comparable<ScheduledEvent>, Serializable {
        private MarsClock when;
        private ScheduledEventHandler handler;


        public ScheduledEvent(MarsClock when, ScheduledEventHandler handler) {
            this.when = when;
            this.handler = handler;
        }

        public MarsClock getWhen() {
            return when;
        }
        
        /**
         * Get the description of the target handler.
         */
        public String getDescription() {
            return handler.getEventDescription();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            return prime * when.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ScheduledEvent other = (ScheduledEvent) obj;
            if (!when.equals(other.when))
                return false;
            else
                return handler.equals(other.handler);
        }

        /**
         * Compare the scheduled events according to when the event is scheduled
         * @param o
         * @return
         */
        @Override
        public int compareTo(ScheduledEvent o) {
            return Double.compare(when.getTotalMillisols(), o.when.getTotalMillisols());
        }
    }

    private MarsClock marsClock;
    private List<ScheduledEvent> eventQueue = new ArrayList<>();

    public ScheduledEventManager(MarsClock clock) {
        this.marsClock = clock;
    }

    /**
     * Add an event ti be executed in the future
     * @param duration Duratin in miliisol until the event is executed
     * @param handler Handler when the event expires
     */
    public ScheduledEvent addEvent(int duration, ScheduledEventHandler handler) {
        // I hate he MarsCLock imlementation not being immutable
        MarsClock when = new MarsClock(marsClock);
        when.addTime(duration);

        ScheduledEvent result = new ScheduledEvent(when, handler);
        addEvent(result);
        return result;
    }

    private void addEvent(ScheduledEvent newEvent) {
        synchronized(eventQueue) {
            eventQueue.add(newEvent);
            Collections.sort(eventQueue);
        }
    }
    /**
     * What events are scheduled for the futureS
     * @return
     */
    public List<ScheduledEvent> getEvents() {
        return Collections.unmodifiableList(eventQueue);
    }

    /**
     * Process any expired events
     * @param clockPulse
     */
    @Override
    public boolean timePassing(ClockPulse clockPulse) {
        synchronized(eventQueue) {
            if (!eventQueue.isEmpty()) {
                MarsClock currentTime = clockPulse.getMarsTime();
                ScheduledEvent next = eventQueue.get(0);

                // Keep executing events that have past
                while((next != null) && next.when.getTotalMillisols() <= currentTime.getTotalMillisols()) {
                    eventQueue.remove(next);
                    int repeatInterval = next.handler.execute();
                    if (repeatInterval > 0) {
                        // Update the when and add back intot he queue
                        next.when.addTime(repeatInterval);
                        addEvent(next);
                    }

                    // Get next event
                    if (eventQueue.isEmpty()) {
                        next = null;
                    }
                    else {
                        next = eventQueue.get(0);
                    }
                }
            }
        }
        
        return true;
    }
}
