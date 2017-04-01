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

	/* Default or Generic Phases */
	INCUBATION("Incubation"),
	PLANTING("Planting"),
	GERMINATION("Germination"), //include initial sprouting of a seedling
	GROWING("Growing"),
	HARVESTING("Harvesting"),
	FINISHED("Finished"),

	/* For Bulbs */
	//INCUBATION("Incubation"),
	//PLANTING("Planting"),
	CLOVE_SPROUTING("Clove Sprouting"),
	POST_EMERGENCE("Post Emergence"),
	LEAFING("Leafing"),
	BULB_INITIATION("Bulb Initiation"),
	MATURATION("Maturation"),
	//HARVESTING("Harvesting");
	//FINISHED("Finished"),

	/* For Corms */
	//INCUBATION("Incubation"),
	//PLANTING("Planting"),
	BUD_SPROUTING("Bud Sprouting"),
	VEGETATIVE_DEVELOPMENT("Vegetative Development"),
	FLOWERING("FLowering"),
	REPLACEMENT_CORMS_DEVELOPMENT("Replacement Corms Development"),
	ANTHESIS("Anthesis"),
	//HARVESTING("Harvesting");
	//FINISHED("Finished"),

	/* For Fruits */
	//INCUBATION("Incubation"),
	//PLANTING("Planting"),
	//GERMINATION("Germination"), //include initial sprouting of a seedling
	//VEGETATIVE_DEVELOPMENT("Vegetative Development"),
	//FLOWERING("Flowering"),
	FRUITING("Fruiting"),
	//HARVESTING("Harvesting");
	//FINISHED("Finished"),

	/* For Grains */
	//INCUBATION("Incubation"),
	//PLANTING("Planting"),
	//GERMINATION("Germination"),
	//LEAFING("Leafing"),
	TILLERING("Tillering"),
	// for rice only //PANICLE_INITIATION("Panicle Initiation"),
	STEM_ELONGATION("Stem Elongation"),
	//FLOWERING("Flowering"),
	MILK_DEVELOPMENT("Milk Development"), // or seed fill for wheat
	DOUGH_DEVELOPING("Dough Development"), // including soft dough and hard dough
	//MATURATION("Maturation"), not using RIPENING("Ripening"); // including milk,
	//HARVESTING("Harvesting"),
	//FINISHED("Finished"),

	/* For Grasses */
	//INCUBATION("Incubation"),
	//PLANTING("Planting"),
	//GERMINATION("Germination"),
	//TILLERING("Tillering"),
	GRAND_GROWTH("Grand Growth"),
	//MATURATION("Maturation"), not using RIPENING("Ripening"); // including milk,
	//HARVESTING("Harvesting"),
	//FINISHED("Finished"),

	/* For Leaves */
	//INCUBATION("Incubation"),
	//PLANTING("Planting"),
	//GERMINATION("Germination"), //include initial sprouting of a seedling
	//POST_EMERGENCE("Post Emergence"),
	HEAD_DEVELOPMENT("Head Development"),
	FIFTY_PERCENT_HEAD_SIZE_REACHED("Half Head Size Reached"),
	//HARVESTING("Harvesting");
	//FINISHED("Finished"),


	/* For Legumes */
	//INCUBATION("Incubation"),
	//PLANTING("Planting"),
	//GERMINATION("Germination"),
	//LEAFING("Leafing"),
	//FLOWERING("Flowering"),
	SEED_FILL("Seed Fill"),
	POD_MATURING("Pod Maturing"),
	//HARVESTING("Harvesting"),
	//FINISHED("Finished"),

	/* For Tubers */
	//INCUBATION("Incubation"),
	//PLANTING("Planting"),
	SPROUTING("Sprouting"),
	LEAF_DEVELOPMENT("Leaf Development"),
	TUBER_INITIATION("Tuber Initiation"),
	TUBER_FILLING("Tuber Filling");
	//MATURATION("Maturation"),
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
