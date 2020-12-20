package org.mars.sim.console.chat.simcommand.vehicle;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Command to get the specs of a vehicle
 * This is a singleton.
 */
public class VehicleStatusCommand extends ChatCommand {

	public static final ChatCommand STATUS = new VehicleStatusCommand();
	private static final String ONE_COLUMN = "%28s : %s%n";

	private VehicleStatusCommand() {
		super(VehicleChat.VEHICLE_GROUP, "st", "status", "What are the vehicle specs.");
	}

	/** 
	 * Output the current immediate location of the Unit
	 */
	@Override
	public void execute(Conversation context, String input) {
		VehicleChat parent = (VehicleChat) context.getCurrentCommand();
		Vehicle source = parent.getVehicle();
		
		// Rovers has more capabilities.
		boolean isRover = (source instanceof Rover);
		StringBuffer buffer = new StringBuffer();

		buffer.append(String.format(ONE_COLUMN, "Status", source.printStatusTypes()));
		buffer.append(String.format(ONE_COLUMN, "Settlement", source.getAssociatedSettlement().getName()));
		buffer.append(String.format(ONE_COLUMN, "Location", source.getImmediateLocation()));
		buffer.append(String.format(ONE_COLUMN, "Locale", source.getLocale()));
		buffer.append(String.format(ONE_COLUMN, "Reserved",
					(source.isReservedForMission() ? "Yes" : "No")));

		// TODO Why it this not on the Vehicle ????
		Mission m = context.getSim().getMissionManager().getMissionForVehicle(source);
		if (m != null) {
			buffer.append(String.format(ONE_COLUMN, "Mission", m.getName()));
			buffer.append(String.format(ONE_COLUMN, "Mission Lead", m.getStartingMember().getName()));

			if (m instanceof VehicleMission) {
				double dist = Math.round(((VehicleMission) m).getProposedRouteTotalDistance() * 10.0) / 10.0;
				double trav = Math.round(((VehicleMission) m).getActualTotalDistanceTravelled() * 10.0) / 10.0;
				buffer.append(String.format(ONE_COLUMN, "Proposed Dist.", (dist + " km")));
				buffer.append(String.format(ONE_COLUMN, "Travelled", (trav + " km")));
			}
		} 

		if (source.isBeingTowed()) {
			buffer.append(String.format(ONE_COLUMN, "Being Towed", "Yes by " + source.getTowingVehicle().getName()));
		}
		else if (isRover && ((Rover) source).isTowingAVehicle()) {
			buffer.append(String.format(ONE_COLUMN, "Towing", ((Rover) source).getTowedVehicle().getName()));
		}

		context.println(buffer.toString());
	}
}
