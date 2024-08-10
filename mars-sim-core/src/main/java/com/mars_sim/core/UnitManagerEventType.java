/*
 * Mars Simulation Project
 * UnitManagerEventType.java
 * @date 2024-08-10
 * @author stpa
 */

package com.mars_sim.core;

/**
 * This enum was introduced to avoid using
 * hard coded strings in {@link UnitManager}
 * and {@link UnitManagerEvent}.
 */
public enum UnitManagerEventType {

	ADD_UNIT,
	REMOVE_UNIT;
}
