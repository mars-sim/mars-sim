/**
 * Mars Simulation Project
 * VehicleStatusCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.vehicle;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
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
		buffer.appendLabeledString("Speed", String.format(CommandHelper.KMPH_FORMAT, source.getSpeed()));
		buffer.appendLabeledString("Reserved",
					(source.isReservedForMission() ? "Yes" : "No"));
		VehicleOperator operator = source.getOperator();
		if (operator != null) {
			buffer.appendLabeledString("Operator", operator.getOperatorName());
		}
		
		// TODO Why it this not on the Vehicle ????
		Mission m = context.getSim().getMissionManager().getMissionForVehicle(source);
		if (m != null) {
			buffer.appendLabeledString("Mission", m.getTypeID());
			buffer.appendLabeledString("Mission Phase", m.getPhaseDescription());
			buffer.appendLabeledString("Mission Lead", m.getStartingPerson().getName());

			if (m instanceof VehicleMission) {
				double dist = ((VehicleMission) m).getEstimatedTotalDistance();
				double trav = ((VehicleMission) m).getActualTotalDistanceTravelled();
				buffer.appendLabeledString("Proposed Dist.", String.format(CommandHelper.KM_FORMAT, dist));
				buffer.appendLabeledString("Travelled", String.format(CommandHelper.KM_FORMAT, trav));
			}
		} 

		if (source.isBeingTowed()) {
			buffer.appendLabeledString("Being Towed", "Yes by " + source.getTowingVehicle().getName());
		}
		else if (isRover && ((Rover) source).isTowingAVehicle()) {
			buffer.appendLabeledString("Towing", ((Rover) source).getTowedVehicle().getName());
		}

		// Maintenance details
		MalfunctionManager mm = source.getMalfunctionManager();
		buffer.appendLabeledString("Active since maint.", String.format(CommandHelper.MILLISOL_FORMAT, mm.getEffectiveTimeSinceLastMaintenance()));
		buffer.appendLabeledString("Odometer", String.format(CommandHelper.KM_FORMAT, source.getOdometerMileage()));
		
		context.println(buffer.getOutput());
		return true;
	}
}
