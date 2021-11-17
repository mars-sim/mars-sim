/*
 * Mars Simulation Project
 * MalfunctionRepairWork.java
 * @date 2021-11-16
 * @author Barry Evans
 */

package org.mars_sim.msp.core.malfunction;

/**
 * The different types of repair work required for a Malfunction.
 */
public enum MalfunctionRepairWork {
	INSIDE("Inside"),
	EVA("EVA");

	private String name;

	private MalfunctionRepairWork(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
