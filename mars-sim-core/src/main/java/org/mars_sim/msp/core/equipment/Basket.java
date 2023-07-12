/*
 * Mars Simulation Project
 * Basket.java
 * @date 2023-07-12
 * @author Manny Kung
 */

package org.mars_sim.msp.core.equipment;

import org.mars_sim.msp.core.Unit;

public class Basket extends AmountResourceContainer {
	
	public Basket(Unit unit, double cap) {
		super(unit, cap);

		setContainerType(ContainerType.BASKET);
	}
}
