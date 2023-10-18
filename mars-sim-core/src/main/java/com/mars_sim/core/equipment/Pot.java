/*
 * Mars Simulation Project
 * Pot.java
 * @date 2023-07-30
 * @author Manny Kung
 */

package com.mars_sim.core.equipment;

import com.mars_sim.core.Entity;

public class Pot extends AmountResourceBin {
	
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	public Pot(Entity entity, double cap) {
		super(entity, cap);

		setBinType(BinType.POT);
	}
}
