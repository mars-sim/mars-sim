/**
 * Mars Simulation Project
 * ComplaintType.java
 * @date 2023-11-15
 * @author Manny Kung
 *
 */

package com.mars_sim.core.person.health;

import com.mars_sim.core.tool.Msg;

/**
 * 	Medical Illnesses.
 */
public enum ComplaintType {

	APPENDICITIS,BROKEN_BONE,BURNS,COLD,

	// Environmentally Induced
	DECOMPRESSION,
	
	DEHYDRATION,DEPRESSION,FEVER,FLU,FOOD_POISONING,

	// Environmentally Induced
	FREEZING,FROSTBITE,FROSTNIP,

	GANGRENE,HEART_ATTACK,HEARTBURN,

	// Environmentally Induced
	HEAT_STROKE,

	HIGH_FATIGUE_COLLAPSE,HYPOXEMIA,LACERATION,MAJOR_BURNS,
	MENINGITIS,MINOR_BURNS,PANIC_ATTACK,PULLED_MUSCLE_TENDON,
	RADIATION_SICKNESS,RUPTURED_APPENDIX,

	// Environmentally Induced
	STARVATION,SUFFOCATION,
	SUICIDE;

	private String name;

	private ComplaintType() {
        this.name = Msg.getStringOptional("ComplaintType", name());
	}

	public String getName() {
		return this.name;
	}
}
