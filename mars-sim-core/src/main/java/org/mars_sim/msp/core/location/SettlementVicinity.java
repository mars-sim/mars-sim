/**
 * Mars Simulation Project
 * SettlementVicinity.java
 * @version 3.08 2015-12-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.location;

import java.io.Serializable;

import org.mars_sim.msp.core.Unit;

public class SettlementVicinity implements LocationState, Serializable {

	private String name = "Within a settlement's vicinity";
	private Unit unit;

	public String getName() {
		return name;
	}

	public SettlementVicinity(Unit unit) {
		this.unit = unit;
	}

}