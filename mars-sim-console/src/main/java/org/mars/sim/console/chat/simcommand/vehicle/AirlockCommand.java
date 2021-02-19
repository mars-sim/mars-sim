package org.mars.sim.console.chat.simcommand.vehicle;

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

			response.appendTableHeading("State", 14, "Active",
									"Operator", CommandHelper.PERSON_WIDTH,
									"Inner Door", "Outer Door");
			Airlock airlock = ((Rover)source).getAirlock();
			response.appendTableRow(airlock.getState().name(),
									(airlock.isActivated() ? "Yes" : "No"),
									airlock.getOperatorName(),
									(airlock.isInnerDoorLocked() ? "Locked" : "Unlocked"),
									(airlock.isOuterDoorLocked() ? "Locked" : "Unlocked"));
			context.println(response.getOutput());
		}
		else {
			context.println("No airlocks");
		}

		return true;
	}
}
