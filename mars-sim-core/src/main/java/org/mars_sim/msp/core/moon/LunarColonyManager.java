/*
 * Mars Simulation Project
 * LunarColonyManager.java
 * @date 2023-09-24
 * @author Manny Kung
 */

package org.mars_sim.msp.core.moon;

import java.io.Serializable;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.Temporal;

public class LunarColonyManager implements Serializable, Temporal {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(LunarColonyManager.class.getName());

	@Override
	public boolean timePassing(ClockPulse pulse) {
		// TODO Auto-generated method stub
		return false;
	}

}
