/**
 * Mars Simulation Project
 * VehicleStatusCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.vehicle;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.ConversationRole;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Command to get the refill a vehicle in expert mode
 * This is a singleton.
 */
public class VehicleRefillCommand extends ChatCommand {

	public static final ChatCommand REFUEL = new VehicleRefillCommand();
	private static final double BOOST = 100D;

	private VehicleRefillCommand() {
		super(VehicleChat.VEHICLE_GROUP, "rf", "refill", "Refill a vehicle with a resource.");
		addRequiredRole(ConversationRole.ADMIN);
	}

	/** 
	 * @return 
	 */
	@Override
	public boolean execute(Conversation context, String input) {
		VehicleChat parent = (VehicleChat) context.getCurrentCommand();
		Vehicle source = parent.getVehicle();
		

		AmountResource resource = null;
		if (input != null) {
			resource = ResourceUtil.findAmountResource(input);
			if (resource == null) {
				context.println(input + " is an unknown resource.");
				return false;
			}
		}
		else {
			resource = ResourceUtil.findAmountResource(source.getFuelType());
		}
		
		source.storeAmountResource(resource.getID(), BOOST);
		context.println("Added " + BOOST + " kg of " + resource.getName());
		return true;
	}
}
