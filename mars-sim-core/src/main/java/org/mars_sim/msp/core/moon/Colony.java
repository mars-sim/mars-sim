/*
 * Mars Simulation Project
 * Colony.java
 * @date 2023-09-25
 * @author Manny Kung
 */

package org.mars_sim.msp.core.moon;

import java.io.Serializable;

import org.mars.sim.mapdata.location.Coordinates;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.Temporal;

public class Colony implements Serializable, Temporal {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(Colony.class.getName());

	private String name;
	
	private Coordinates location;
	
	private Population population;
	
	public Colony(String name, Coordinates location) {
		this.name = name;
		this.location = location;
		
		population = new Population();
	}

	@Override
	public boolean timePassing(ClockPulse pulse) {

		return true;
	}
	
	public Population getPopulation() {
		return population;
	}
	
	public Coordinates getLocation() {
		return location;
	}
}



