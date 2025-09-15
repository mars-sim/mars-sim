/*
 * Mars Simulation Project
 * Agency.java
 * @date 2023-10-08
 * @author Manny Kung
 */

package com.mars_sim.core.authority;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.time.ClockPulse;

public class Agency extends Organization {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(Agency.class.getName());

	/**
	 * Constructor.
	 * 
	 * @param acronym
	 * @param fullName
	 */
	Agency(String acronym, String fullName) {
		super(acronym, fullName);
	}
	
	public boolean isCorporation() {
		return false;
	}
	
	@Override
	public boolean timePassing(ClockPulse pulse) {
		// May be used in future
		return false;
	}

}
