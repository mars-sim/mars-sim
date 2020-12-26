package org.mars.sim.console.chat.simcommand.person;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.person.Person;

/** 
 * 
 */
public class FriendCommand extends AbstractPersonCommand {
	public static final ChatCommand FRIEND = new FriendCommand();
	
	private FriendCommand() {
		super("f", "friend", "About my friends");
	}

	@Override
	public void execute(Conversation context, String input, Person person) {

		context.println("Not implemented");
	}

}
