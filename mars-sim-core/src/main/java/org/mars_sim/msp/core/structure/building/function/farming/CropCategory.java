/*
 * Mars Simulation Project
 * CropCategory.java
 * @date 2023-04-18
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure.building.function.farming;

public enum CropCategory {
	
	BULBS("Bulbs"),
	CORMS("Corms"),
	FLOWERS("Flowers"),
	FRUITS("Fruits"),
	
	FUNGI("Fungi"),
	GRAINS("Grains"),
	GRASSES("Grasses"),
	HERBS("Herbs"),	
	LEAVES("Leaves"),
	
	LEGUMES("Legumes"),
	ROOTS("Roots"),
	SEEDS("Seeds"),
	//SPICES("spices"),
	
	STEMS("Stems"),
	TUBERS("Tubers");
	

	private String name;

	private CropCategory(String name) {
		this.name = name;
	}	

	public String getName() {
		// TODO change all names to i18n-keys for accessing messages.properties
		return this.name;
	}

	@Override
	public final String toString() {
		return getName();
	}
}
