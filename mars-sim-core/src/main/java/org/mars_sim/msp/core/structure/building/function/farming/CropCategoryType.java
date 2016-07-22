/*
 * Mars Simulation Project
 * CropCategoryType.java
 * @version 3.1.0 2016-07-01
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure.building.function.farming;

import java.util.logging.Logger;
import org.mars_sim.msp.core.robot.RobotType;

public enum CropCategoryType {
	
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
	SPICES("spices"),
	
	STEMS("stems"),
	TUBERS("tubers");
	

	private String name;

	private CropCategoryType(String name) {
		this.name = name;
	}	

	public String getName() {
		// TODO change all names to i18n-keys for accessing messages.properties
		return this.name;
	}
	
	public static CropCategoryType getType(String s) {
		return valueOf(s.toUpperCase());
	}
}
