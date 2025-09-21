/*
 * Mars Simulation Project
 * LunarColonyManager.java
 * @date 2023-09-24
 * @author Manny Kung
 */

package com.mars_sim.core.moon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.authority.AuthorityFactory;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.core.tool.RandomUtil;

public class LunarColonyManager implements Serializable, Temporal {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(LunarColonyManager.class.getName());

	private Set<Colony> colonies = new HashSet<>();

	private static AuthorityFactory raFactory = SimulationConfig.instance().getReportingAuthorityFactory();
	
//	private long tLast;
	
	private static Map<String, String> colonyNames = new HashMap<>();

	private static Map<String, String> initialColonyNames = new HashMap<>();
	
	private static List<Coordinates> coords = new ArrayList<>();
	
	private LunarWorld lunarWorld;
	
	static {
//		initialColonyNames.put("Kennedy", "NASA");
//		initialColonyNames.put("Kararmin", "ISRA");
//		initialColonyNames.put("Yue De", "CNSA");
//		initialColonyNames.put("Barmingrad", "RKA");
		initialColonyNames.put("LunarOne", "SPACEX");
		
		colonyNames.put("Kennedy", "NASA");
		colonyNames.put("Kararmin", "ISRA");
		colonyNames.put("Yue De", "CNSA");
		colonyNames.put("Barmingrad", "RKA");
		colonyNames.put("LunarOne", "SPACEX");
		
		colonyNames.put("Borington", "SPACEX");
		colonyNames.put("Peary", "MS");
		
		colonyNames.put("Selene", "ESA"); // Mahina
		colonyNames.put("Pacifica", "CSA");
		colonyNames.put("O'Neill", "BO");
		colonyNames.put("Kala", "UAESA");
		
		// https://momlovesbest.com/baby-names-that-mean-moon
		colonyNames.put("Hilal", "SSA");
		// https://momlovesbest.com/baby-names-that-mean-moon
		colonyNames.put("Aadhira", "ISRO");
		
		colonyNames.put("Lincoln", "NASA");
		// https://www.dearjapanese.com/japanese-boy-names-meaning-moon/
		colonyNames.put("Tsukito", "JAXA");
	}
	
	static {
		// Landing Site Studies
		// Descriptions of locations on the Moon where we have landed in the past or may land in the future.
		// See https://www.lpi.usra.edu/lunar/site_studies/	
		
		// Use Lunokhod 2's coordinate on the moon at Le Monnier crater (25.85 degrees N, 30.45 degrees E).
		// https://en.wikipedia.org/wiki/Lunokhod_2
		
		// Use Lunokhod 1's coordinate on the moon in western Mare Imbrium (Sea of Rains) at "38.2378 N", "35.0017 W", 
		// about 60 km south of the Promontorium Heraclides.
		// https://en.wikipedia.org/wiki/Lunokhod_1
		
		coords.add(new Coordinates("-89.90 ", "0.00"));
		coords.add(new Coordinates("88.63", "24.40"));
		coords.add(new Coordinates("25.85", "30.45"));
		coords.add(new Coordinates("38.24", "-35.00"));
		// coords.add(new Coordinates("38.2378 N", "35.0017 W"));
		
		// A distinctive echo pattern was found in the LRS data obtained around the Marius Hills Hole (MHH), 
		// a possible skylight of an intact lunar lava tube around an area (13.00–15.00°N, 301.85–304.01°E) 
		// https://agupubs.onlinelibrary.wiley.com/doi/10.1002/2017GL074998
		// https://www.lroc.asu.edu/posts/202
		
		coords.add(new Coordinates("14.00", "302.00"));
		
		// A list of the preferred landing sites addressing 3 or more of the Science Goals within 
		// Concept 3. Regions are the Procellarum KREEP Terrane (PKT), the South Pole-Aitken 
		// Terrane (SPAT), and the Feldspathic Highlands Terrane (FHT).
		// https://www.researchgate.net/figure/Schroedinger-basin-and-suggested-landing-sites-background-Clementine-UVVIS-mosaic_fig1_234420980
		
		coords.add(new Coordinates("23.70", "-47.40"));
		coords.add(new Coordinates("9.70", "-20.10"));
		coords.add(new Coordinates("5.00", "120.50"));
		coords.add(new Coordinates("26.00", "148.00"));
		
		coords.add(new Coordinates("-69.70 S", "-172.00"));
		coords.add(new Coordinates("-42.00 S", "-177.90"));
		coords.add(new Coordinates("-30.20 S", "173.90"));
		coords.add(new Coordinates("-19.00 S", "-95.00"));
	}
	
	/**
	 * Constructor.
	 */
	public LunarColonyManager(LunarWorld lunarWorld) {	
		this.lunarWorld = lunarWorld;
	}
	
	/**
	 * Adds an initial existing colony.
	 */
	public void addInitColonies() {
		// Assume this colony has existed for a while
		addColony(true);
	}
	
	/**
	 * Adds a colony.
	 * 
	 * @param startup  Is it at the startup of the simulation ?
	 */
	private void addColony(boolean startup) {
		String aName = getNewColonyName(startup);
		if (aName == null)
			return;
				
		// Proceed to creating a new colony
		Coordinates co = getNewCoord();
		if (co == null)
			return;
		
		Colony colony = new Colony(colonies.size(), aName, raFactory.getItem(colonyNames.get(aName)), co, startup);
		
		logger.config("The lunar colony " + aName + " was founded and sponsored by " + colonyNames.get(aName) + ".");
		
		colonies.add(colony);
	}
	
	/**
	 * Gets a new colony name.
	 * 
	 * @param startup  Is it at the startup of the simulation ?
	 * @return
	 */
	private String getNewColonyName(boolean startup) {

		Set<String> namesInUse = new HashSet<>();
		List<String> newNames = null;
		
		for (Colony c: colonies) {
			namesInUse.add(c.getName());
		}
		
		if (startup) {
			newNames = new ArrayList<>(initialColonyNames.keySet());
		}
		else {
			newNames = new ArrayList<>(colonyNames.keySet());
		}
		
		Collections.shuffle(newNames);
		
		for (String n: newNames) {
			if (!namesInUse.contains(n)) {
				return n;
			}
		}

		return null;
	}
	
	/**
	 * Gets a new coordinate.
	 * 
	 * @return
	 */
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
	

	@Override
	public boolean timePassing(ClockPulse pulse) {
		// DEBUG: Calculate the real time elapsed [in milliseconds] long tnow = System.currentTimeMillis();

		lunarWorld.timePassing(pulse);
		
		if (pulse.isNewHalfSol() || (RandomUtil.getRandomInt(100) == 1)) {
			addColony(false);
		}
					
		for (Colony c: colonies) {
			c.timePassing(pulse);
		}
		
		/**
		 * DEBUG: Calculate the real time elapsed [in milliseconds]
		 * tLast = System.currentTimeMillis();
		 * long elapsedMS = tLast - tnow;
		 * if (elapsedMS > 10)
		 * 	logger.severe("elapsedMS: " + elapsedMS);
		 */

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
