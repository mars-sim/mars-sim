/*
 * Mars Simulation Project
 * LunarColonyManager.java
 * @date 2023-09-24
 * @author Manny Kung
 */

package com.mars_sim.core.moon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.authority.AuthorityFactory;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.mapdata.location.Coordinates;
import com.mars_sim.tools.util.RandomUtil;

public class LunarColonyManager implements Serializable, Temporal {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(LunarColonyManager.class.getName());

	private Set<Colony> colonies = new HashSet<>();

	private static AuthorityFactory raFactory = SimulationConfig.instance().getReportingAuthorityFactory();
	
//	private long tLast;
	
	private static Map<String, String> colonyNames = new HashMap<>();

	private static List<Coordinates> coords = new ArrayList<>();
	
	static {
		colonyNames.put("Shackleton", "NASA");
		colonyNames.put("Peary", "MS");
		colonyNames.put("Yue De", "CNSA");
		colonyNames.put("Barmingrad", "RKA");
	}
	
	static {
		coords.add(new Coordinates("89.9 S", "0 E"));
		coords.add(new Coordinates("88.63 N", "24.4 E"));
		coords.add(new Coordinates("25.85 N", "30.45 E"));
		coords.add(new Coordinates("38.2378 N", "35.0017 W"));
	}
	
	/**
	 * Constructor.
	 */
	public LunarColonyManager() {	
	}
	
	/**
	 * Adds a initial set of colonies. (Temporary only).
	 */
	public void addInitColonies() {
		
		int rand = RandomUtil.getRandomInt(10);
		if (rand == 1) {
			Colony colony0 = new Colony(colonies.size(), "Shackleton", raFactory.getItem("NASA"), new Coordinates("89.9 S", "0 E"), false);
			colonies.add(colony0);
		}
		else if (rand == 2) {
			Colony colony1 = new Colony(colonies.size(), "Peary", raFactory.getItem("MS"), new Coordinates("88.63 N", "24.4 E"), false);
			colonies.add(colony1);
		}
		else if (rand == 3) {
			// Use Lunokhod 2's coordinate on the moon at Le Monnier crater (25.85 degrees N, 30.45 degrees E).
			// https://en.wikipedia.org/wiki/Lunokhod_2
			Colony colony2 = new Colony(colonies.size(), "Yue De", raFactory.getItem("CNSA"), new Coordinates("25.85 N", "30.45 E"), false);
			colonies.add(colony2);
		}
		else if (rand == 4) {
			// Use Lunokhod 1's coordinate on the moon in western Mare Imbrium (Sea of Rains), 
			// about 60 km south of the Promontorium Heraclides.
			// https://en.wikipedia.org/wiki/Lunokhod_1
			Colony colony3 = new Colony(colonies.size(), "Barmingrad", raFactory.getItem("RKA"), new Coordinates("38.2378 N", "35.0017 W"), false);
			colonies.add(colony3);
		}
	}
	
	private void addColony() {
		String aName = getNewColonyName();
		if (aName == null)
			return;
		
		Coordinates co = getNewCoord();
		if (co == null)
			return;
		
		Colony colony = new Colony(colonies.size(), aName, raFactory.getItem(colonyNames.get(aName)), co, true);
		
		colonies.add(colony);
	}
	
	private String getNewColonyName() {

		Set<String> namesInUse = new HashSet<>();
		for (Colony c: colonies) {
			namesInUse.add(c.getName());
		}
		
		for (String n: colonyNames.keySet()) {
			if (!namesInUse.contains(n)) {
				return n;
			}
		}
		
		return null;
	}
	
	private Coordinates getNewCoord() {

		Set<Coordinates> inUse = new HashSet<>();
		for (Colony c: colonies) {
			inUse.add(c.getCoordinates());
		}
		
		for (Coordinates co: coords) {
			if (!inUse.contains(co)) {
				return co;
			}
		}
		
		return null;
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

		if (pulse.isNewHalfSol()) {
			int rand = RandomUtil.getRandomInt(50);
			if (rand == 1) {
				addColony();
			}
			
		}
		
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
