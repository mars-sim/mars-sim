/*
 * Mars Simulation Project
 * Colony.java
 * @date 2023-09-25
 * @author Manny Kung
 */

package org.mars_sim.msp.core.moon;

import java.io.Serializable;

import org.mars.sim.mapdata.location.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.logging.Loggable;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.Temporal;

public class Colony implements Serializable, Temporal, Loggable, Comparable<Colony> {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(Colony.class.getName());

	private String name;
	
	private Coordinates location;
	
	private Population population;
	
	private Simulation sim;
	
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

	/**
	 * Compares this object with the specified object for order.
	 *
	 * @param o the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 */
	@Override
	public int compareTo(Colony	o) {
		return name.compareToIgnoreCase(o.name);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Coordinates getCoordinates() {
		return location;
	}

	@Override
	public Unit getContainerUnit() {
		if (sim == null) {
			sim = Simulation.instance();
		}
		return sim.getUnitManager().getMoon();
	}
}



