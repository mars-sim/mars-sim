/*
 * Mars Simulation Project
 * VehicleStatusCommand.java
 * @date 2022-08-24
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.vehicle;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;

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
		buffer.appendLabeledString("Beacon", source.isBeaconOn() ? "On" : "Off");

		buffer.appendLabeledString("Reserved",
					(source.isReservedForMission() ? "Yes" : "No"));
		Worker operator = source.getOperator();
		if (operator != null) {
			buffer.appendLabeledString("Operator", operator.getName());
		}
		
		Mission m = source.getMission();
		if (m != null) {
			buffer.appendLabeledString("Mission", m.getName());
			buffer.appendLabeledString("Mission Phase", m.getPhaseDescription());
			buffer.appendLabeledString("Mission Lead", m.getStartingPerson().getName());

			if (m instanceof VehicleMission vm) {
				double dist = vm.getTotalDistanceProposed();
				double trav = vm.getTotalDistanceTravelled();
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
