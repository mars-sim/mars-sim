/*
 * Mars Simulation Project
 * Crate.java
 * @date 2023-07-12
 * @author Manny Kung
 */

package org.mars_sim.msp.core.equipment;

import org.mars_sim.msp.core.Unit;

public class Crate extends AmountResourceContainer {
	
	public Crate(Unit unit, double cap) {
		super(unit, cap);

		setContainerType(ContainerType.CRATE);
	}
}
