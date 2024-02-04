/*
 * Mars Simulation Project
 * ConnectCommand.java
 * @date 2023-06-14
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.command.InteractiveChatCommand;
import com.mars_sim.console.chat.simcommand.person.PersonChat;
import com.mars_sim.console.chat.simcommand.robot.RobotChat;
import com.mars_sim.console.chat.simcommand.settlement.SettlementChat;
import com.mars_sim.console.chat.simcommand.vehicle.VehicleChat;
import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * Connects to an entity. This is a singleton.
 */
public class ConnectCommand extends ChatCommand {

	// Connect command is stateless and can be shared
	public static final ChatCommand CONNECT = new ConnectCommand();

	private ConnectCommand() {
		super(TopLevel.SIMULATION_GROUP, "c", "connect", "Connects to an entity specified by name");
		setInteractive(true);
	}

	/**
	 * Connects to another entity. 
	 * This will change the conversation current command to one specific to the entity.
	 * 
	 * @return 
	 */
	@Override
	public boolean execute(Conversation context, String input) {
		boolean result = false;
		if ((input == null) || input.isBlank()) {
			context.println("Sorry! You have to tell me clearly what you would like to connect with.");
			context.println("");
		}
		else {
			context.println("Connecting to " + input + "...");
	
			UnitManager um = context.getSim().getUnitManager();
			
			// Find unit by full equals match on name
			final String name = input;
			List<Unit> allUnits = getAllUnits(um);
			List<Unit> matched = allUnits.stream().filter(p -> p.getName().equalsIgnoreCase(name)).collect(Collectors.toList());
	
			if (matched.size() != 1) {
				context.println("Sorry there must be 1 match for '" + name + "'");
			}
			else {
				InteractiveChatCommand parent = null;
				boolean alreadyConnected = context.getCurrentCommand() instanceof ConnectedUnitCommand;
				if (alreadyConnected) {
					// So user is reconnecting without disconnecting via bye command.
					// Find the next leve up
					List<InteractiveChatCommand> layers = context.getCommandStack();
					if (layers.isEmpty()) {
						// Hmm what to do. Doesn;t work
						context.println("Seem to be no parent top level command");
					}
					else {
						parent = layers.get(0);
					}
				}
				else {
					// Parent is an non-connected interactive command
					parent = context.getCurrentCommand();
				}

				Unit match = matched.get(0);
				InteractiveChatCommand newCommand = switch(match.getUnitType()) {
					case PERSON -> new PersonChat((Person) match, parent);
					case ROBOT -> new RobotChat((Robot) match, parent);
					case VEHICLE -> new VehicleChat((Vehicle) match, parent);
					case SETTLEMENT -> new SettlementChat((Settlement) match, parent);
					default -> null;	
				};

				// If the current chat is an Unit then don't remember it
				if (newCommand != null) {
					context.setCurrentCommand(newCommand, !alreadyConnected);
					result = true;
				}
				else {
					context.println("Sorry I don't know how to connect " + name);
				}
			}
		}
		return result;
	}

	/**
	 * Gets all the units in the simulation. Really should come directly off UnitManager.
	 * 
	 * @param um
	 * @return
	 */
	private List<Unit> getAllUnits(UnitManager um) {
		List<Unit> units  = new ArrayList<>();
		units.addAll(um.getPeople());
		units.addAll(um.getVehicles());
		units.addAll(um.getRobots());
		units.addAll(um.getSettlements());
				
		return units;
	}

	/**
	 * Gets the possible Unit names for auto complete.
	 * 
	 * @param context The conversation taking place
	 * @param input Partial input
	 * @return List of Unit names that match
	 */
	@Override
	public List<String> getArguments(Conversation context) {
		UnitManager um = context.getSim().getUnitManager();
		List<Unit> units = getAllUnits(um);
		
		// Filter the Units by name
		return units.stream().map(Unit::getName).collect(Collectors.toList());
	}
}
