/*
 * Mars Simulation Project
 * LunarColonyManager.java
 * @date 2023-09-24
 * @author Manny Kung
 */

package com.mars_sim.core.moon;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.core.tool.RandomUtil;

/**
 * The lunar colony manager.
 */
public class LunarColonyManager implements Serializable, Temporal {

	private static record ColonySpec(String name, String sponsor, Coordinates coord) {}

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(LunarColonyManager.class.getName());

	private static List<ColonySpec> colonySpecs = List.of(
				new ColonySpec("Kennedy", "NASA", new Coordinates("-89.90 ", "0.00")),
				new ColonySpec("Kararmin", "ISRA", new Coordinates("88.63", "24.40")),
				new ColonySpec("Yue De", "CNSA", new Coordinates("25.85", "30.45")),
				new ColonySpec("Barmingrad", "RKA", new Coordinates("38.24", "-35.00")),
				new ColonySpec("LunarOne", "SPACEX", new Coordinates("14.00", "302.00")),
				new ColonySpec("Borington", "SPACEX", new Coordinates("23.70", "-47.40")),
				new ColonySpec("Peary", "MS", new Coordinates("9.70", "-20.10")),
				new ColonySpec("Selene", "ESA", new Coordinates("5.00", "120.50")),
				new ColonySpec("Pacifica", "CSA", new Coordinates("26.00", "148.00")),
				new ColonySpec("O'Neill", "BO", new Coordinates("-69.70", "-172.00")),
				new ColonySpec("Kala", "UAESA", new Coordinates("-42.00", "-177.90")),		
				new ColonySpec("Hilal", "SSA", new Coordinates("-30.20", "173.90")),
				new ColonySpec("Aadhira", "ISRO", new Coordinates("-19.00", "-95.00"))
			);

	private int maxColonies = 5;
	private Set<Colony> colonies = new HashSet<>();	
	private LunarWorld lunarWorld;

	/**
	 * Constructor.
	 */
	public LunarColonyManager(LunarWorld lunarWorld) {	
		this.lunarWorld = lunarWorld;
	}

	/**
	 * Set the maximum number of colonies that can be created.
	 * @param max
	 */
	public void setMaxColonies(int max) {
		// Don't exceed the number of predefined colony specs.
		this.maxColonies = Math.min(max, colonySpecs.size());
	}

	/**
	 * Adds a colony.
	 * 
	 * @param startup  Is it at the startup of the simulation ?
	 */
	private void addColony() {
		var aName = getNewColonySpec();
		if (aName == null)
			return;

		var authorityFactory = SimulationConfig.instance().getReportingAuthorityFactory();
		var authority = authorityFactory.getItem(aName.sponsor());

		Colony colony = new Colony(colonies.size(), aName.name(), authority, aName.coord());
		
		logger.config("The lunar colony " + aName.name() + " was founded and sponsored by " + authority.getName() + ".");
		
		colonies.add(colony);
	}
	
	/**
	 * Gets a new colony name.
	 * 
	 * @return
	 */
	private ColonySpec getNewColonySpec() {

		List<String> namesInUse = colonies.stream().map(Colony::getName).toList();
		List<ColonySpec> potentials = colonySpecs.stream()
				.filter(spec -> !namesInUse.contains(spec.name))
				.toList();

		return RandomUtil.getRandomElement(potentials);
	}


	@Override
	public boolean timePassing(ClockPulse pulse) {
		lunarWorld.timePassing(pulse);
		
		if ((colonies.size() < maxColonies) && (RandomUtil.getRandomInt(100) == 1)) {
			addColony();
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
}
