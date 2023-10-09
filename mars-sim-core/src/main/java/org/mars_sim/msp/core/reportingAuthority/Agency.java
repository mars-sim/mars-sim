/*
 * Mars Simulation Project
 * Agency.java
 * @date 2023-10-08
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.time.ClockPulse;

public class Agency extends Organization {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(Agency.class.getName());

	
	Agency(String acronym, String fullName) {
		super(acronym, fullName);
	}
	
	public boolean isCorporation() {
		return false;
	}
	
	@Override
	public boolean timePassing(ClockPulse pulse) {
		// TODO Auto-generated method stub
		return false;
	}

}
