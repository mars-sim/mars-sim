/**
 * Mars Simulation Project
 * InsideVehicle.java
 * @version 3.08 2015-12-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.location;

import java.io.Serializable;

import org.mars_sim.msp.core.Unit;

public class InsideVehicle implements LocationState, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String name = "Inside a vehicle";
	private Unit unit;

	public String getName() {
		return name;
	}

	public InsideVehicle(Unit unit) {
		this.unit = unit;
	}

}