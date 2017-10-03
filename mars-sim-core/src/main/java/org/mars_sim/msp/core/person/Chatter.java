/**
 * Mars Simulation Project
 * Chatter.java
 * @version 3.1.0 2016-11-26
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import java.io.Serializable;
import java.util.logging.Logger;

import org.mars_sim.msp.core.time.MarsClock;

/**
 * The Chatter class holds the comment of a settler 
 */
public class Chatter implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /* default logger. */
	private static transient Logger logger = Logger.getLogger(Chatter.class.getName());
    
	private String personName, settlementName, buildingName, vehicleName, comment;
	private MarsClock clock;
	
	public Chatter(String personName, String settlementName, String buildingName, String vehicleName, String comment, MarsClock clock) {

		
	}
	
	
	
}
