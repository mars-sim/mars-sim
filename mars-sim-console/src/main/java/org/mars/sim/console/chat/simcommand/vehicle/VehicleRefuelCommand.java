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
 * Command to get the refuel a vehicle in expert mode
 * This is a singleton.
 */
public class VehicleRefuelCommand extends ChatCommand {

	public static final ChatCommand REFUEL = new VehicleRefuelCommand();
	private static final double FUEL_BOOST = 100D;

	private VehicleRefuelCommand() {
		super(VehicleChat.VEHICLE_GROUP, "rf", "refuel", "Refuel a vehicle.");
		addRequiredRole(ConversationRole.ADMIN);
	}

	/** 
	 * @return 
	 */
	@Override
	public boolean execute(Conversation context, String input) {
		VehicleChat parent = (VehicleChat) context.getCurrentCommand();
		Vehicle source = parent.getVehicle();
		
		int fuel =  source.getFuelType();
		source.storeAmountResource(fuel, FUEL_BOOST);
		
		AmountResource fuelType = ResourceUtil.findAmountResource(fuel);
		context.println("Added " + FUEL_BOOST + " of " + fuelType.getName());
		return true;
	}
}
