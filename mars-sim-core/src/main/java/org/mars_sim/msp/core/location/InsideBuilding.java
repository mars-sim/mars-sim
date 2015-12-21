/**
 * Mars Simulation Project
 * InsideBuilding.java
 * @version 3.08 2015-12-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.location;

import java.io.Serializable;

import org.mars_sim.msp.core.Unit;

public class InsideBuilding implements LocationState, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String name = "Inside a building";
	private Unit unit;

	public String getName() {
		return name;
	}

	public InsideBuilding(Unit unit) {
		this.unit = unit;
	}

}