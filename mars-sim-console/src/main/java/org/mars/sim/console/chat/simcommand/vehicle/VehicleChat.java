package org.mars.sim.console.chat.simcommand.vehicle;

import java.util.Arrays;
import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.simcommand.ConnectedUnitCommand;
import org.mars.sim.console.chat.simcommand.MissionCommand;
import org.mars.sim.console.chat.simcommand.UnitLocationCommand;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Represents a connection to a Vehicle.
 */
public class VehicleChat extends ConnectedUnitCommand {
	public static final String VEHICLE_GROUP = "Vehicle";

	private static final List<ChatCommand> COMMANDS = Arrays.asList(VehicleSpecCommand.SPEC,
																	new MissionCommand(VEHICLE_GROUP),
																	new UnitLocationCommand(VEHICLE_GROUP),
																	VehicleStatusCommand.STATUS);


	public VehicleChat(Vehicle vehicle) {
		super(vehicle, COMMANDS);
	}

	@Override
	public String getIntroduction() {
		Vehicle vehicle = getVehicle();
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("Connected to ");
		buffer.append(vehicle.getVehicleType());
		buffer.append(" called ");
		buffer.append(vehicle.getName());
		buffer.append(" based at ");
		buffer.append(vehicle.getAssociatedSettlement().getName());
		
		return buffer.toString();
	}

	public Vehicle getVehicle() {
		return (Vehicle) getUnit();
	}
}
