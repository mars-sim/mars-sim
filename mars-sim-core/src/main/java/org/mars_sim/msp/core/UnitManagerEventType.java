/**
 * Mars Simulation Project
 * UnitManagerEventType.java
 * @version 3.1.2 2020-09-02
 * @author stpa
 */

package org.mars_sim.msp.core;

/**
 * this enum was introduced to avoid using
 * hard coded strings in {@link UnitManager}
 * and {@link UnitManagerEvent}.
 */
public enum UnitManagerEventType {

	ADD_UNIT,
	REMOVE_UNIT;
}
