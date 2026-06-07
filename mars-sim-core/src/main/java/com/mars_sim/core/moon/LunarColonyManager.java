/*
 * Mars Simulation Project
 * LunarColonyManager.java
 * @date 2026-05-05
 * @author Manny Kung
 */

package com.mars_sim.core.moon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.events.ScheduledEventHandler;
import com.mars_sim.core.events.ScheduledEventManager;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.core.tool.RandomUtil;

/**
 * The lunar colony manager.
 */
public class LunarColonyManager implements Serializable, Temporal {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(LunarColonyManager.class.getName());

	private int coloniesScheduled = 0;

	private int maxColonies = 5;
	
	private List<ColonySpec> colonySpecs;
	
	private Set<Colony> colonies = new HashSet<>();	
	
	private LunarWorld lunarWorld;

	private ScheduledEventManager futures;


	/**
	 * Constructor.
	 */
	public LunarColonyManager(LunarWorld lunarWorld, ScheduledEventManager futures) {	
		this.lunarWorld = lunarWorld;
		this.futures = futures;
		
		colonySpecs = SimulationConfig.instance().getMoonConfig().getColonySpecs();
	}

	/**
	 * Sets the maximum number of colonies that can be created.
	 * 
	 * @param max
	 */
	public void setMaxColonies(int max) {
		// Don't exceed the number of predefined colony specs.
		this.maxColonies = Math.min(max, colonySpecs.size());
		logger.info("Maximum number of lunar colonies set to " + this.maxColonies);
	}

	/**
	 * Adds a colony. Reduce the count of scheduled colonies.
	 * 
	 * @param spec the colony specification
	 */
	private void addColony(ColonySpec spec) {

		var authorityFactory = SimulationConfig.instance().getReportingAuthorityFactory();
		var authority = authorityFactory.getItem(spec.sponsor());

		Colony colony = new Colony(colonies.size(), spec.name(), authority, spec.coord());
		
		logger.config("The lunar colony " + spec.name() + " was founded and sponsored by " + authority.getName() + ".");
		
		coloniesScheduled--;
		colonies.add(colony);
	}
	
	/**
	 * Gets a new colony name. Avoid existing ones and scheduled ones.	
	 * 
	 * @param scheduledSpecs the already scheduled colony specs
	 */
	private ColonySpec getNewColonySpec(Collection<String> scheduledSpecs) {

		List<String> namesInUse = new ArrayList<>(scheduledSpecs);
		namesInUse.addAll(colonies.stream().map(Colony::getName).toList());
		List<ColonySpec> potentials = colonySpecs.stream()
				.filter(spec -> !namesInUse.contains(spec.name()))
				.toList();

		if (potentials.isEmpty()) {
			logger.warning("No pre-defined colony specs available. Creating a random");
			return new ColonySpec("Colony #" + namesInUse.size(), "MS", Coordinates.getRandomLocation());
		}
		return RandomUtil.getRandomElement(potentials);
	}


	@Override
	public boolean timePassing(ClockPulse pulse) {
		lunarWorld.timePassing(pulse);
		
		if ((colonies.size() + coloniesScheduled) < maxColonies) {
			// Scheduled creation
			scheduleColonyCreation();
		}
					
		for (Colony c: colonies) {
			c.timePassing(pulse);
		}
		return true;
	}

	/**
	 * A scheduled event handler to create a colony.
	 */
	private class ColonyCreation implements ScheduledEventHandler {
		private final ColonySpec spec;

		public ColonyCreation(ColonySpec spec) {
			this.spec = spec;
		}

		@Override
		public String getEventDescription() {
			return "Created a lunar colony called '" + spec.name() + "'";
		}

		@Override
		public int execute(MarsTime currentTime) {
			addColony(spec);
			return 0;
		}
	
	}

	/**
	 * Schedules colony creation future events for the shortfall spread over 1000 Msol.
	 */
	private void scheduleColonyCreation() {
		coloniesScheduled = maxColonies - colonies.size();
		logger.info("Scheduling " + coloniesScheduled + " lunar colony creation events.");

		List<String> scheduledNames = new ArrayList<>();

		// Spread out the colony creation over 1000 Msol
		int msolPerCol = (int)1000D/coloniesScheduled;
		for (int i = 0; i < coloniesScheduled; i++) {
			var spec = getNewColonySpec(scheduledNames);

			// Schedule for the future and randomize within the allocated slot
			int colDelay = RandomUtil.getRandomInt(msolPerCol);
			var future = new ColonyCreation(spec);
			futures.addEvent((i * msolPerCol) + colDelay, future);
			scheduledNames.add(spec.name());
		}
	}

	public Set<Colony> getColonySet() {
		return colonies;
	}
}
