/*
 * Mars Simulation Project
 * Corporation.java
 * @date 2023-10-08
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.time.ClockPulse;

public class Corporation extends Organization {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(Corporation.class.getName());

	
	Corporation(String acronym, String fullName) {
		super(acronym, fullName);
	}
	
	public boolean isCorporation() {
		return true;
	}
	
	@Override
	public boolean timePassing(ClockPulse pulse) {
		// TODO Auto-generated method stub
		return false;
	}

}
