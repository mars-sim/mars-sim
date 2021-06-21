/**
 * Mars Simulation Project
 * AirlockCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.vehicle;

import java.util.ArrayList;
import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

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
			
			List<Airlock> i = new ArrayList<>();
			i.add(airlock);
			CommandHelper.outputAirlock(response, i);

			context.println(response.getOutput());
		}
		else {
			context.println("No airlocks");
		}

		return true;
	}
}
