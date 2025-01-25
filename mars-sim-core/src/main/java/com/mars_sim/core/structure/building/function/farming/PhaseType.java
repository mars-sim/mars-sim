/*
 * Mars Simulation Project
 * PhaseType.java
 * @date 2023-05-06
 * @author Manny Kung
 */
package com.mars_sim.core.structure.building.function.farming;

public enum PhaseType {

	ANTHESIS("Anthesis"),
	BULB_INITIATION("Bulb Initiation"),
	DOUGH_DEVELOPING("Dough Development"), // including soft dough and hard dough
	EARLY_BULKING_UP("Early Bulking Up"),
	EARLY_VEGETATIVE("Early Vegatative"),
	FIFTY_PERCENT_HEAD_SIZE_REACHED("Half Head Size Reached"),
	FINISHED("Finished"),
	FLOWERING("Flowering"),
	FOLIAGE ("Foliage"),
	FRUITING("Fruiting"),
	GERMINATION("Germination"),
	GRAND_GROWTH("Grand Growth"),
	GROWING("Growing"),
	HEAD_DEVELOPMENT("Head Development"),
	INCUBATION("Incubation"),
	HARVESTING("Harvesting"),
	LATE_BULKING_UP("Late Bulking Up"),
	LEAF_DEVELOPMENT("Leaf Development"),
	LEAFING("Leafing"),
	MATURATION("Maturation"),
	MID_BULKING_UP("Mid Bulking Up"),
	MID_VEGETATIVE("Mid Vegatative"),
	MILK_DEVELOPMENT("Milk Development"), // or seed fill for wheat
	PLANTING("Planting"),
	POD_MATURING("Pod Maturing"),
	POST_EMERGENCE("Post Emergence"),
	REPLACEMENT_CORMS_DEVELOPMENT("Replacement Corms Development"),
	ROOT_DEVELOPMENT("Root Development"),
	SEED_FILL("Seed Fill"),
	SPROUTING("Sprouting"),
	STEM_ELONGATION("Stem Elongation"),
	TILLERING("Tillering"),
	TUBER_INITIATION("Tuber Initiation"),
	TUBER_FILLING("Tuber Filling"),
	VEGETATIVE_DEVELOPMENT("Vegetative Development");

	private String name;

	private PhaseType(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
}
