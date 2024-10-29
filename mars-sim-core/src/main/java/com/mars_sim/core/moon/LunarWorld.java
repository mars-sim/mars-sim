/*
 * Mars Simulation Project
 * LunarWorld.java
 * @date 2023-09-24
 * @author Manny Kung
 */

package com.mars_sim.core.moon;

import java.io.Serializable;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;

public class LunarWorld implements Serializable, Temporal {

	private static final long serialVersionUID = 1L;

	private static final SimLogger logger = SimLogger.getLogger(LunarWorld.class.getName());

	@Override
	public boolean timePassing(ClockPulse pulse) {		
		// Future: Simulate the sunlight cycle that affects the power generation
		return false;
	}

}
