/**
 * Mars Simulation Project
 * OutsideOnMars.java
 * @version 3.08 2015-12-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.location;

import java.io.Serializable;

import org.mars_sim.msp.core.Unit;

public class OutsideOnMars implements LocationState, Serializable {

	private String name = "Outside on the surface of Mars";
	private Unit unit;

	public String getName() {
		return name;
	}

	public OutsideOnMars(Unit unit) {
		this.unit = unit;
	}

}