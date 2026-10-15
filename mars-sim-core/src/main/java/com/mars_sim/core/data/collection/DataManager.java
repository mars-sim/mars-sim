/*
 * Mars Simulation Project
 * DataManager.java
 * @date 2026-08-27
 * @author Manny Kung
 */

package com.mars_sim.core.data.collection;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.mars_sim.core.EntityManagerListener;
import com.mars_sim.core.structure.Settlement;


public class DataManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** The settlement's data library. */
	private Map<Settlement, List<FieldDataSet>> dataArchive;
	
	private transient Set<EntityManagerListener> listeners;
	
	/**
	 * Constructor for a {@link DataManager}.
	 */
	public DataManager() {
		dataArchive = new ConcurrentHashMap<>();	
	}
	
	/**
	 * Gets the data archive map.
	 * 
	 * @return
	 */
	public Map<Settlement, List<FieldDataSet>> getDataArchive() {
		if (dataArchive.isEmpty())
			return Collections.emptyMap();
		return dataArchive;
	}
	
	/**
	 * Gets the settlement set.
	 * 
	 * @return
	 */
	public Set<Settlement> getSettlementSet() {
		if (dataArchive.isEmpty())
			return Collections.emptySet();
		
		Set<Settlement> set = dataArchive.keySet();
		if (set.isEmpty())
			return Collections.emptySet();
		
		return set;
	}
	
	
	/**
	 * Adds a listener.
	 * 
	 * @param listener the listener.
	 */
	public void addListener(EntityManagerListener listener) {
		if (listeners == null) {
			listeners = new HashSet<>();
		}
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/**
	 * Removes a previously registered listener.
	 * 
	 * @param listener
	 */
	public void removeListener(EntityManagerListener listener) {
		if (listeners != null) {
			synchronized (listeners) {
				listeners.remove(listener);
			}
		}
	}
}
