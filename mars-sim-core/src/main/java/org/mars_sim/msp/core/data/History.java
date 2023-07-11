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

import org.mars_sim.msp.core.time.MarsDate;
import org.mars_sim.msp.core.time.MarsTime;
import org.mars_sim.msp.core.time.MasterClock;

/**
 * Represents the history a value that changes over time.
 * Whenever a value is added it is timestamped with teh current marian time.
 */
public class History<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
     * Something happened in the history of this object.
     */
    public static class HistoryItem<T> implements Serializable {
    
		private static final long serialVersionUID = 1L;
		
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
    private int maxItems;
    
    /**
     * Create a History but define the maximum items to hold
     * @param maxItems
     */
    public History(int maxItems) {
        this.maxItems = maxItems;
    }

    /**
     * Crete a history that hold infinite items
     */
    public History() {
        this(-1);
    }

    /**
     * Add a value to the history and timestamp it. If the value is the same as the previous item 
     * this the item is not added.
     * If the timestamp of the previous item has not advance; then it is overwritten.
     * @param value New value to add
     */
    public boolean add(T value) {
        MarsTime now = master.getMarsTime();
        if (!history.isEmpty()) {
            HistoryItem<T> previous = history.get(history.size()-1);
            if (now.equals(previous.getWhen())) {
                // Time has not avance so replace existing
                history.remove(history.size()-1);
            }
            else if (value.equals(previous.getWhat())) {
                // Same value as last time so ignore
                return false;
            }
        }

        if (history.size() == maxItems) {
            // Rrmove first item (oldest)
            history.remove(0);
        }
        history.add(new HistoryItem<>(now, value));

        return true;
    }

    /**
     * Get the changes that have occured over time
     * @return List of changes.
     */
    public List<HistoryItem<T>> getChanges() {
        return history;
    }

    /**
     * Get the range of dates covered by this history
     * @return
     */
    public List<MarsDate> getRange() {
        return history.stream().map(i -> i.getWhen().getDate()).distinct().toList();
    }

    /**
     * Load up the reference to the master clock
     * @param mc
     */
    public static void initializeInstances(MasterClock mc) {
        master = mc;
    }
}
