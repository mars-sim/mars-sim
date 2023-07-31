/*
 * Mars Simulation Project
 * Pot.java
 * @date 2023-07-30
 * @author Manny Kung
 */

package org.mars_sim.msp.core.equipment;

import org.mars_sim.msp.core.structure.building.function.farming.UnitEntity;

public class Pot extends AmountResourceBin {
	
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	public static double CAP = 100;
	
	public Pot(UnitEntity unitEntity, double cap) {
		super(unitEntity, cap);

		setBinType(BinType.POT);
	}
}
