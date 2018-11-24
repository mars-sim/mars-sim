/**
 * Mars Simulation Project
 * MarsSurface.java
 * @version 3.1.0 2018-11-17
 * @author Manny Kung
 */

package org.mars_sim.msp.core.mars;

import java.io.Serializable;

import org.mars_sim.msp.core.Unit;

/**
 * MarsSurface is the top most container unit that houses all other units
 * (Settlement, Building, Person, Robot, Vehicle, and Equipment)
 *
 */
public class MarsSurface extends Unit implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 123L;

	public MarsSurface() {
		super("Mars Surface", null);
		getInventory().addGeneralCapacity(Double.MAX_VALUE);
//		System.out.println("MarsSurface : " + this.getIdentifier());
	}
}
