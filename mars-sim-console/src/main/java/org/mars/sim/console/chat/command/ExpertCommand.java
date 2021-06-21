/**
 * Mars Simulation Project
 * ExpertCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.command;

import java.util.HashSet;
import java.util.Set;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.ConversationRole;

/**
 * Generic help command that will list the commands that are supported.
 */
public class ExpertCommand extends ChatCommand {
	
	// The long command 
	public static final ChatCommand EXPERT = new ExpertCommand();

	public ExpertCommand() {
		super(COMMAND_GROUP, "xp", "expert", "Toggles Expert mode; optional <On|Off> argument");
	}
	
	@Override
	public boolean execute(Conversation context, String input) {
		Set<ConversationRole> roles = new HashSet<>(context.getRoles());
		boolean dropExpert = roles.contains(ConversationRole.EXPERT);
		
		if (input != null) {
			if ("on".equalsIgnoreCase(input)) {
				dropExpert = false;
			}
			else if ("off".equalsIgnoreCase(input)) {
				dropExpert = true;
			}
			else {
				context.println("Do not understand the argument " + input);
				return false;
			}
		}
		
		if (dropExpert) {
			context.println("Switching off expert mode");
			roles.remove(ConversationRole.EXPERT);
		}
		else {
			context.println("Switching on expert mode");
			roles.add(ConversationRole.EXPERT);			
		}

		context.setRoles(roles);
		return true;
	}
}
