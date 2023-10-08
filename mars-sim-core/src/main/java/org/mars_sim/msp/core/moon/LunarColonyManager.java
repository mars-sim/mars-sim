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
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityFactory;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.Temporal;

public class LunarColonyManager implements Serializable, Temporal {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(LunarColonyManager.class.getName());

	private Set<Colony> colonies = new HashSet<>();

	private static ReportingAuthorityFactory raFactory = SimulationConfig.instance().getReportingAuthorityFactory();
	
	public LunarColonyManager() {	
	}
	
	public void addColonies() {
		
		Colony colony0 = new Colony("Shackleton", raFactory.getItem("NASA"), new Coordinates("89.9 S", "0 E"));
		Colony colony1 = new Colony("Peary", raFactory.getItem("MS"), new Coordinates("88.63 N", "24.4 E"));
		// Use Lunokhod 2's coordinate on the moon at Le Monnier crater (25.85 degrees N, 30.45 degrees E).
		// https://en.wikipedia.org/wiki/Lunokhod_2
		Colony colony2 = new Colony("Yue De", raFactory.getItem("CNSA"), new Coordinates("25.85 N", "30.45 E"));
		// Use Lunokhod 1's coordinate on the moon in western Mare Imbrium (Sea of Rains), 
		// about 60 km south of the Promontorium Heraclides.
		// https://en.wikipedia.org/wiki/Lunokhod_1
		Colony colony3 = new Colony("Barmingrad", raFactory.getItem("RKA"), new Coordinates("38.2378 N", "35.0017 W"));
		
		colonies.add(colony0);
		colonies.add(colony1);
		colonies.add(colony2);
		colonies.add(colony3);	
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
