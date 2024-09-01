/*
 * Mars Simulation Project
 * Narrator.java
 * @date 2024-08-10
 * @author Manny Kung
 */

package com.mars_sim.core.narrator;

import java.io.Serializable;

import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.events.HistoricalEventCategory;
import com.mars_sim.core.person.EventType;

public class Narrator implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
	public Narrator() {
		
	}
	
	public void translate(HistoricalEvent event) {
		String cause = event.getWhatCause();
	    HistoricalEventCategory category = event.getCategory();
		//Object source = event.getSource();
		Object who = event.getWho();
		String home = event.getHomeTown();
		String doing = event.getWhileDoing();
		EventType eventType = event.getType();
	}
	
}
