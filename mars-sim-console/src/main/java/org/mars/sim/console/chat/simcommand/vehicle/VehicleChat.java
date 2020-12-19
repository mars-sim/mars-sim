package org.mars.sim.console.chat.simcommand.vehicle;

import java.util.Arrays;
import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.simcommand.ConnectedUnitCommand;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Represents a connection to a Vehicle.
 */
public class VehicleChat extends ConnectedUnitCommand {
	private static final List<ChatCommand> COMMANDS = Arrays.asList();

	public static final String VEHICLE_GROUP = "Vehicle";

	private Vehicle vehicle;

	public VehicleChat(Vehicle vehicle) {
		super(vehicle, COMMANDS);
		this.vehicle = vehicle;
	}

	@Override
	public String getIntroduction() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Connected to ");
		buffer.append(vehicle.getVehicleType());
		buffer.append(" called ");
		buffer.append(vehicle.getName());
		buffer.append(" based at ");
		buffer.append(vehicle.getAssociatedSettlement().getName());
		
		return buffer.toString();
	}
}
