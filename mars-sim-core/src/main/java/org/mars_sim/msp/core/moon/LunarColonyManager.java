/*
 * Mars Simulation Project
 * LunarColonyManager.java
 * @date 2023-09-24
 * @author Manny Kung
 */

package org.mars_sim.msp.core.moon;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.mars.sim.mapdata.location.Coordinates;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.Temporal;

public class LunarColonyManager implements Serializable, Temporal {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(LunarColonyManager.class.getName());

	Set<Colony> colonies = new HashSet<>();
	
	public LunarColonyManager() {
		Colony colonyOne = new Colony("Shackleton", new Coordinates("89.9 S", "0 E"));
		Colony colonyTwo = new Colony("Peary", new Coordinates("88.63 N", "24.4 E"));
		colonies.add(colonyOne);
		colonies.add(colonyTwo);
	}
	
	@Override
	public boolean timePassing(ClockPulse pulse) {
		for (Colony c: colonies) {
			c.timePassing(pulse);
		}
		return true;
	}

	public Set<Colony> getColonySet() {
		return colonies;
	}
	
}
