/**
 * Mars Simulation Project
 * Narrator.java
 * @version 3.1.0 2018-06-19
 * @author Manny Kung
 */

package org.mars_sim.msp.core.narrator;

import java.io.Serializable;

import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventCategory;

public class Narrator implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
	public Narrator() {
		
	}
	
	public void translate(HistoricalEvent event) {
		String description = event.getDescription();
	    HistoricalEventCategory category = event.getCategory();
		Object source = event.getSource();
		Object actor = event.getActor();
		String location = event.getLocation();
		String eventType = event.getType().getName();
		
		
	}
	
	
}
