/**
 * Mars Simulation Project
 * Indoor.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building;

import org.mars_sim.msp.core.structure.Settlement;

/**
 * This interface accounts for units that can stay indoor inside a settlement
 */
public interface Indoor {

	public Building getBuildingLocation(); 

	public Settlement getSettlement();

	public Settlement getAssociatedSettlement();

}

