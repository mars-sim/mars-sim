package com.mars_sim.core.moon;

import com.mars_sim.core.logging.SimLogger;

public class Colonist {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Colonist.class.getName());
	
	private String name;
	
	private Colony colony;
	
	public Colonist(String name, Colony colony) {
		this.name = name;
		this.colony = colony;
	}
	
	public String getName() {
		return name;
	}
	
	public Colony getColony() {
		return colony;
	}
}
