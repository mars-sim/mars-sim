package org.mars.sim.console.chat.simcommand;

import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.command.InteractiveChatCommand;
import org.mars.sim.console.chat.simcommand.person.PersonChat;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;

/**
 * Connects to an entity. This is a singleton
 */
public class ConnectCommand extends ChatCommand {

	// Connect command is stateless and can be shared
	public static final ConnectCommand CONNECT = new ConnectCommand();

	private ConnectCommand() {
		super(TopLevel.SIMULATION_GROUP, "c", "connect", "Connects to an entity sepcfied by name");
	}

	/**
	 * Connects to another entity; this will change the Conversation current command to one specific to the
	 * entity.
	 */
	@Override
	public void execute(Conversation context, String input) {
		if ((input == null) || input.isBlank()) {
			context.println("Sorry, you have to tell what to connect to");
			return;
		}
		context.println("Connecting to " + input + " .....");

		UnitManager um = context.getSim().getUnitManager();
		InteractiveChatCommand newCommand = null;
		
		// Find unit by name
		final String name = input;
		List<Person> matchedPeople = um.getPeople().stream().filter(p -> p.getName().equals(name)).collect(Collectors.toList());
		if (!matchedPeople.isEmpty()) {
			newCommand = new PersonChat(matchedPeople.get(0));
		}
		
		// If the current chat is an Unit then don't remember it
		if (newCommand != null) {
			boolean alreadyConnected = (context.getCurrentCommand() instanceof ConnectedUnitCommand);
			context.setCurrentCommand(newCommand, !alreadyConnected);
		}
	}

	@Override
	/**
	 * Find any units where the name matches the inout
	 * @param context Conversation taking place
	 * @param input Partial input
	 * @return List of Unit names that match
	 */
	public List<String> getAutoComplete(Conversation context, String parameter) {
		UnitManager um = context.getSim().getUnitManager();
		List<Unit> units = new ArrayList<>();
		units.addAll(um.getPeople());
		
		// Filter the Units by name
		List<String> result = units.stream().filter(u -> u.getName().startsWith(parameter))
									.map(n -> n.getName()).collect(Collectors.toList());

		return result;
	}
}
