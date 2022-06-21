/*
 * Mars Simulation Project
 * CropCategory.java
 * @date 2022-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure.building.function.farming;

public enum CropCategory {
	
	BULBS("bulbs"),
	CORMS("corms"),
	FLOWERS("flowers"),
	FRUITS("fruits"),
	
	FUNGI("fungi"),
	GRAINS("grains"),
	GRASSES("grasses"),
	LEAVES("leaves"),
	
	LEGUMES("legumes"),
	ROOTS("roots"),
	SEEDS("seeds"),
	//SPICES("spices"),
	
	STEMS("stems"),
	TUBERS("tubers");
	

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
