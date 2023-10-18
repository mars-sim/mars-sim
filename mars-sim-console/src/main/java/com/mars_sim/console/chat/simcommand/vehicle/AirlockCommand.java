/*
 * Mars Simulation Project
 * AirlockCommand.java
 * @date 2022-08-24
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.vehicle;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.structure.Airlock;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;

public class AirlockCommand extends ChatCommand {

	public static final ChatCommand AIRLOCK = new AirlockCommand();
	
	private AirlockCommand() {
		super(VehicleChat.VEHICLE_GROUP, "ai", "airlocks", "Status of all airlocks");
	}

	@Override
	public boolean execute(Conversation context, String input) {
		
		VehicleChat parent = (VehicleChat) context.getCurrentCommand();
		Vehicle source = parent.getVehicle();
		
		// Rovers has more capabilities.
		if (source instanceof Rover) {
			StructuredResponse response = new StructuredResponse();
			Airlock airlock = ((Rover)source).getAirlock();
			
			CommandHelper.outputAirlockDetailed(response, "Internal", airlock);

			context.println(response.getOutput());
		}
		else {
			context.println("No airlocks");
		}

		return true;
	}
}
