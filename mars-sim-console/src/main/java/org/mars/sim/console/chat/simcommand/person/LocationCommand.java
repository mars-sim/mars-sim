package org.mars.sim.console.chat.simcommand.person;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.person.Person;

/** 
 * 
 */
public class LocationCommand extends AbstractPersonCommand {
	public static final ChatCommand LOCATION = new LocationCommand();
	
	private LocationCommand() {
		super("l", "location", "About my location");
	}

	@Override
	public void execute(Conversation context, String input, Person person) {

		context.println("Not implemented");
	}

}
