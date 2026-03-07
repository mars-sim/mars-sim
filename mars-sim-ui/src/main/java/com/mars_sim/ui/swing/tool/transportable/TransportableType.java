/*
 * Mars Simulation Project
 * TransportableType.java
 * @date 2026-03-03
 * @author Barry Evans
 */

package com.mars_sim.ui.swing.tool.transportable;

import com.mars_sim.core.Named;

/**
 * Types of transportables that can be created by the wizard.
 */
enum TransportableType implements Named {
	RESUPPLY("Resupply Mission"),
	ARRIVING_SETTLEMENT("Arriving Settlement");

	private final String name;

	TransportableType(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
}
