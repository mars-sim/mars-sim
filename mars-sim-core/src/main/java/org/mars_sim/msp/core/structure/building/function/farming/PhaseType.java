/*
 * Mars Simulation Project
 * PhaseType.java
 * @version 3.1.0 2016-06-29
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure.building.function.farming;

import java.util.logging.Logger;

public enum PhaseType {

	/** default logger. */
	//private static Logger logger = Logger.getLogger(Phase.class.getName());

	/* generic phases of development */
	INCUBATION("Incubation"),
	PLANTING("Planting"),
	GERMINATION("Germination"), //include initial sprouting of a seedling
	GROWING("Growing"),
	HARVESTING("Harvesting"),
	FINISHED("Finished"),
	
	/* For Tubers */
	//INCUBATION("Incubation"),
	//PLANTING("Planting"),
	SPROUTING("Sprouting"),
	LEAF_DEVELOPMENT("Leaf Development"),
	TUBER_INITIATION("Tuber Initiation"),
	TUBER_FILLING("Tuber Filling"),
	MATURING("Maturing");
	//HARVESTING("Harvesting");
	//FINISHED("Finished"),
	

	private String name;

	private PhaseType(String name) {
		this.name = name;
	}	

	public String getName() {
		// TODO change all names to i18n-keys for accessing messages.properties
		return this.name;
	}
}
