package org.mars.sim.console.chat.simcommand;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.command.InteractiveChatCommand;
import org.mars.sim.console.chat.simcommand.person.PersonChat;
import org.mars.sim.console.chat.simcommand.robot.RobotChat;
import org.mars.sim.console.chat.simcommand.settlement.SettlementChat;
import org.mars.sim.console.chat.simcommand.vehicle.VehicleChat;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Connects to an entity. This is a singleton
 */
public class ConnectCommand extends ChatCommand {

	// Connect command is stateless and can be shared
	public static final ChatCommand CONNECT = new ConnectCommand();

	private ConnectCommand() {
		super(TopLevel.SIMULATION_GROUP, "c", "connect", "Connects to an entity sepcfied by name");
		setInteractive(true);
	}

	/**
	 * Connects to another entity; this will change the Conversation current command to one specific to the
	 * entity.
	 * @return 
	 */
	@Override
	public boolean execute(Conversation context, String input) {
		boolean result = false;
		if ((input == null) || input.isBlank()) {
			context.println("Sorry, you have to tell what to connect to");
		}
		else {
			context.println("Connecting to " + input + " .....");
	
			UnitManager um = context.getSim().getUnitManager();
			InteractiveChatCommand newCommand = null;
			
			// Find unit by full equals match on name
			final String name = input;
			List<Unit> allUnits = getAllUnits(um);
			List<Unit> matched = allUnits.stream().filter(p -> p.getName().equalsIgnoreCase(name)).collect(Collectors.toList());
	
			if (matched.size() != 1) {
				context.println("Sorry there must be 1 match for '" + name + "'");
			}
			else {
				InteractiveChatCommand parent = null;
				if (context.getCurrentCommand() instanceof ConnectedUnitCommand) {
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
				
				// No choice but to use instanceof
				if (match instanceof Person) {
					newCommand = new PersonChat((Person) match, parent);
				}
				else if (match instanceof Robot) {
					newCommand = new RobotChat((Robot) match, parent);
				}
				else if (match instanceof Vehicle) {
					newCommand = new VehicleChat((Vehicle) match, parent);
				}
				else if (match instanceof Settlement) {
					newCommand = new SettlementChat((Settlement) match, parent);
				}
				else {
					context.println("Sorry I don't know how to connect " + name);
				}
			}
			
			// If the current chat is an Unit then don't remember it
			if (newCommand != null) {
				boolean alreadyConnected = (context.getCurrentCommand() instanceof ConnectedUnitCommand);
				context.setCurrentCommand(newCommand, !alreadyConnected);
				result = true;
			}
		}
		return result;
	}

	/**
	 * Get all the units in the simulation. Really should come directly off UnitManager.
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

	@Override
	/**
	 * Get the possible Unit names for auto complete.
	 * @param context Conversation taking place
	 * @param input Partial input
	 * @return List of Unit names that match
	 */
	public List<String> getArguments(Conversation context) {
		UnitManager um = context.getSim().getUnitManager();
		List<Unit> units = getAllUnits(um);
		
		// Filter the Units by name
		return units.stream().map(Unit::getName).collect(Collectors.toList());
	}
}
