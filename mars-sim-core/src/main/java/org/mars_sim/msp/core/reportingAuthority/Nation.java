/*
 * Mars Simulation Project
 * Nation.java
 * @date 2023-10-08
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.Temporal;

public class Nation implements Serializable, Temporal {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(Nation.class.getName());

	private String name;
	
	public Nation(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public boolean timePassing(ClockPulse pulse) {

		return false;
	}


}
