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
		super(COMMAND_GROUP, "xp", "expert", "Toggles Expert commands");
	}
	
	@Override
	public boolean execute(Conversation context, String input) {
		Set<ConversationRole> roles = new HashSet<>(context.getRoles());
		
		if (roles.contains(ConversationRole.EXPERT)) {
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
