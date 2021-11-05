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
import org.mars_sim.msp.core.vehicle.Vehicle;

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
		var message = new StringBuilder();
		if (source.isOutside()) {
			message.append("On the surface @ ")
				   .append(source.getCoordinates().getFormattedString());
		}
		else if (source.isInVehicle()) {
			Vehicle v = source.getVehicle();
			message.append("In ").append(v.getName());
			message.append(" @ ").append(v.getCoordinates().getFormattedString());
			if (source.isInSettlement()) {
				message.append(", warning also isIsSettlement=true");
			}
		}
		else {
			Settlement base = source.getSettlement();
			Building building = source.getBuildingLocation();
			message.append("In ");
			if (building != null) {
				message.append(building.getNickName()).append(" @ ");
			}

			message.append(base.getName());				
		}

		context.println(message.toString());
		return true;
	}

}
