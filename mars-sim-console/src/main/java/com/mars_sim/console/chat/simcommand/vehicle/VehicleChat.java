/*
 * Mars Simulation Project
 * VehicleChat.java
 * @date 2022-06-27
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.vehicle;

import java.util.Arrays;
import java.util.List;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.command.InteractiveChatCommand;
import com.mars_sim.console.chat.simcommand.ConnectedUnitCommand;
import com.mars_sim.console.chat.simcommand.unit.EquipmentCommand;
import com.mars_sim.console.chat.simcommand.unit.InventoryCommand;
import com.mars_sim.console.chat.simcommand.unit.MalfunctionCreateCommand;
import com.mars_sim.console.chat.simcommand.unit.MissionCommand;
import com.mars_sim.console.chat.simcommand.unit.ResourceHolderRefillCommand;
import com.mars_sim.console.chat.simcommand.unit.UnitLocationCommand;
import com.mars_sim.console.chat.simcommand.unit.UnitMalfunctionCommand;
import com.mars_sim.console.chat.simcommand.unit.UnitSunlightCommand;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * Represents a connection to a Vehicle.
 */
public class VehicleChat extends ConnectedUnitCommand {
	public static final String VEHICLE_GROUP = "Vehicle";

	private static final List<ChatCommand> COMMANDS = Arrays.asList(VehicleSpecCommand.SPEC,
																	AirlockCommand.AIRLOCK,
																	VehicleCrewCommand.CREW,
																	new ResourceHolderRefillCommand(VEHICLE_GROUP),
																	new EquipmentCommand(VEHICLE_GROUP),
																	new InventoryCommand(VEHICLE_GROUP),
																	new MissionCommand(VEHICLE_GROUP),
																	new UnitLocationCommand(VEHICLE_GROUP),
																	new UnitSunlightCommand(VEHICLE_GROUP),
																	new UnitMalfunctionCommand(VEHICLE_GROUP),
																	new MalfunctionCreateCommand(VEHICLE_GROUP),
																	VehicleStatusCommand.STATUS);


	public VehicleChat(Vehicle vehicle, InteractiveChatCommand parent) {
		super(vehicle, COMMANDS, parent);
	}

	@Override
	public String getIntroduction() {
		Vehicle vehicle = getVehicle();
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("Connected to ");
		buffer.append(vehicle.getVehicleType().getName());
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
