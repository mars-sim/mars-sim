/*
 * Mars Simulation Project
 * LunarColonyManager.java
 * @date 2023-09-24
 * @author Manny Kung
 */

package com.mars_sim.core.moon;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.authority.AuthorityFactory;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.mapdata.location.Coordinates;

public class LunarColonyManager implements Serializable, Temporal {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(LunarColonyManager.class.getName());

	private Set<Colony> colonies = new HashSet<>();

	private static AuthorityFactory raFactory = SimulationConfig.instance().getReportingAuthorityFactory();
	
//	private long tLast;
	
	/**
	 * Constructor.
	 */
	public LunarColonyManager() {	
	}
	
	/**
	 * Adds a set of colonies. (Temporary only).
	 */
	public void addColonies() {
		
		Colony colony0 = new Colony(colonies.size(), "Shackleton", raFactory.getItem("NASA"), new Coordinates("89.9 S", "0 E"));

		Colony colony1 = new Colony(colonies.size(), "Peary", raFactory.getItem("MS"), new Coordinates("88.63 N", "24.4 E"));

		// Use Lunokhod 2's coordinate on the moon at Le Monnier crater (25.85 degrees N, 30.45 degrees E).
		// https://en.wikipedia.org/wiki/Lunokhod_2
		Colony colony2 = new Colony(colonies.size(), "Yue De", raFactory.getItem("CNSA"), new Coordinates("25.85 N", "30.45 E"));

		// Use Lunokhod 1's coordinate on the moon in western Mare Imbrium (Sea of Rains), 
		// about 60 km south of the Promontorium Heraclides.
		// https://en.wikipedia.org/wiki/Lunokhod_1
		Colony colony3 = new Colony(colonies.size(), "Barmingrad", raFactory.getItem("RKA"), new Coordinates("38.2378 N", "35.0017 W"));

		colonies.add(colony0);
		colonies.add(colony1);
		colonies.add(colony2);
		colonies.add(colony3);	
	}
	
	public void init() {
		for (Colony c: colonies) {
			c.init();
		}
	}
	
	@Override
	public boolean timePassing(ClockPulse pulse) {
		// DEBUG: Calculate the real time elapsed [in milliseconds]
//		long tnow = System.currentTimeMillis();

		for (Colony c: colonies) {
			c.timePassing(pulse);
		}
		
		// DEBUG: Calculate the real time elapsed [in milliseconds]
//		tLast = System.currentTimeMillis();
//		long elapsedMS = tLast - tnow;
//		if (elapsedMS > 10)
//			logger.severe("elapsedMS: " + elapsedMS);
	
		return true;
	}

	public Set<Colony> getColonySet() {
		return colonies;
	}
	
	/**
	 * Prepares for deletion.
	 */
	public void destroy() {
		for (Colony c: colonies) {
			c.destroy();
		}
		colonies = null;
		raFactory = null;  
	}
}
