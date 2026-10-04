/*
 * Mars Simulation Project
 * HistoryTracable.java
 * @date 2026-10-04
 * @author Barry Evans
 */
package com.mars_sim.core.data;

import com.mars_sim.core.Entity;
import com.mars_sim.core.tool.MsgContext;

/**
 * An object that records its history of significant events or actions.
 */
public interface HistoryTracable extends Entity {

    /**
     * Gets the history of the entity.
     * 
     * @return the history of the entity
     */
    History<MsgContext> getHistory();
    
	/**
	 * Adds an entry to the history. This represents a significant event or action related to the entity.
	 * @param entry the history entry to add.
	 */
	void addHistoryEntry(MsgContext entry);
}
