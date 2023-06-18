/*
 * Mars Simulation Project
 * History.java
 * @date 2023-06-18
 * @author Barry Evans
 */
package org.mars_sim.msp.core.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.time.MarsTime;
import org.mars_sim.msp.core.time.MasterClock;

/**
 * Represents the history a value that changes over time.
 * Whenever a value is added it is timestamped with teh current marian time.
 */
public class History<T> {
    /**
     * Something happened in the history of this object.
     */
    public static class HistoryItem<T> implements Serializable {
        private MarsTime when;
        private T what;

        public HistoryItem(MarsTime when, T what) {
            this.when = when;
            this.what = what;
        }

        public MarsTime getWhen() {
            return when;
        }
        public T getWhat() {
            return what;
        }
        
    }

    private static MasterClock master;
    private List<HistoryItem<T>> history = new ArrayList<>();
    
    /**
     * Add a value to the history and timestamp it
     * @param value New value to add
     */
    public void add(T value) {
        history.add(new HistoryItem<>(master.getMarsTime(), value));
    }

    /**
     * Get the changes that have occured over time
     * @return List of changes.
     */
    public List<HistoryItem<T>> getChanges() {
        return history;
    }

    /**
     * Load up the reference to the master clock
     * @param mc
     */
    public static void initializeInstances(MasterClock mc) {
        master = mc;
    }
}
