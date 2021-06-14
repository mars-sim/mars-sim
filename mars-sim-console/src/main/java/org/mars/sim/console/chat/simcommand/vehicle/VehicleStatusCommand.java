package org.mars.sim.console.chat.simcommand.vehicle;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleOperator;

/**
 * Command to get the specs of a vehicle
 * This is a singleton.
 */
public class VehicleStatusCommand extends ChatCommand {

	public static final ChatCommand STATUS = new VehicleStatusCommand();

	private VehicleStatusCommand() {
		super(VehicleChat.VEHICLE_GROUP, "sts", "status", "What's the status of the vehicle.");
	}

	/** 
	 * Output the current immediate location of the Unit
	 * @return 
	 */
	@Override
	public boolean execute(Conversation context, String input) {
		VehicleChat parent = (VehicleChat) context.getCurrentCommand();
		Vehicle source = parent.getVehicle();
		
		// Rovers has more capabilities.
		boolean isRover = (source instanceof Rover);
		StructuredResponse buffer = new StructuredResponse();

		buffer.appendLabeledString("Status", source.printStatusTypes());
		buffer.appendLabeledString("Settlement", source.getAssociatedSettlement().getName());
		buffer.appendLabeledString("Location", source.getCoordinates().getFormattedString());
		buffer.appendLabeledString("Speed", String.format("%.2f km/h", source.getSpeed()));
		buffer.appendLabeledString("Reserved",
					(source.isReservedForMission() ? "Yes" : "No"));
		VehicleOperator operator = source.getOperator();
		if (operator != null) {
			buffer.appendLabeledString("Operator", operator.getOperatorName());
		}
		
		// TODO Why it this not on the Vehicle ????
		Mission m = context.getSim().getMissionManager().getMissionForVehicle(source);
		if (m != null) {
			buffer.appendLabeledString("Mission", m.getName());
			buffer.appendLabeledString("Mission Phase", m.getPhaseDescription());
			buffer.appendLabeledString("Mission Lead", m.getStartingMember().getName());

			if (m instanceof VehicleMission) {
				double dist = Math.round(((VehicleMission) m).getProposedRouteTotalDistance() * 10.0) / 10.0;
				double trav = Math.round(((VehicleMission) m).getActualTotalDistanceTravelled() * 10.0) / 10.0;
				buffer.appendLabeledString("Proposed Dist.", (dist + " km"));
				buffer.appendLabeledString("Travelled", (trav + " km"));
			}
		} 

		if (source.isBeingTowed()) {
			buffer.appendLabeledString("Being Towed", "Yes by " + source.getTowingVehicle().getName());
		}
		else if (isRover && ((Rover) source).isTowingAVehicle()) {
			buffer.appendLabeledString("Towing", ((Rover) source).getTowedVehicle().getName());
		}

		context.println(buffer.getOutput());
		return true;
	}
}
