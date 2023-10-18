/*
 * Mars Simulation Project
 * Basket.java
 * @date 2023-07-12
 * @author Manny Kung
 */

package com.mars_sim.core.equipment;

import com.mars_sim.core.Entity;

public class Basket extends AmountResourceBin {
	
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	public Basket(Entity entity, double cap) {
		super(entity, cap);

		setBinType(BinType.BASKET);
	}
}
