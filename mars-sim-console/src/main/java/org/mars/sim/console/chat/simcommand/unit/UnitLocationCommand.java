/**
 * Mars Simulation Project
 * UnitLocationCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.unit;

import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Command to stop speaking with an entity.
 * This is a singleton.
 */
public class UnitLocationCommand extends AbstractUnitCommand {


	public UnitLocationCommand(String groupName) {
		super(groupName, "l", "location", "What is the location.");
	}

	/** 
	 * Output the current immediate location of the Unit
	 */
	@Override
	protected boolean execute(Conversation context, String input, Unit source) {
		
		if (source.isOutside()) {
			context.println("On the surface @ " + source.getCoordinates().getFormattedString());
		}
		else if (source.isInVehicle()) {
			context.println("In " + source.getVehicle().getName());
		}
		else {
			Settlement base = source.getSettlement();
			Building building = source.getBuildingLocation();
			if (building != null) {
				context.println("In " + building.getNickName() + " @ " + base.getName());
			}
			else {
				context.println("In " + base.getName());				
			}
		}

		return true;
	}

}
