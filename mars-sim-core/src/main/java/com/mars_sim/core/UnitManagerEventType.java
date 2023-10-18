/**
 * Mars Simulation Project
 * UnitManagerEventType.java
 * @version 3.2.0 2021-06-20
 * @author stpa
 */

package com.mars_sim.core;

/**
 * this enum was introduced to avoid using
 * hard coded strings in {@link UnitManager}
 * and {@link UnitManagerEvent}.
 */
public enum UnitManagerEventType {

	ADD_UNIT,
	REMOVE_UNIT;
}
