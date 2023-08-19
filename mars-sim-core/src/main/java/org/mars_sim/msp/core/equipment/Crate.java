/*
 * Mars Simulation Project
 * Crate.java
 * @date 2023-07-12
 * @author Manny Kung
 */

package org.mars_sim.msp.core.equipment;

import org.mars_sim.msp.core.Entity;

public class Crate extends AmountResourceBin {
	
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	public Crate(Entity entity, double cap) {
		super(entity, cap);

		setBinType(BinType.CRATE);
	}
}
