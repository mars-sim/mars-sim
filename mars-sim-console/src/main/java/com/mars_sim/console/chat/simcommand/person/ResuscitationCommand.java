/*
 * Mars Simulation Project
 * ResuscitationCommand.java
 * @date 2023-11-22
 * @author Manny Kung
 */

package com.mars_sim.console.chat.simcommand.person;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.ConversationRole;
import com.mars_sim.core.person.Person;

/** 
 * The command for resuscitating a dead person.
 */
public class ResuscitationCommand extends AbstractPersonCommand {
	public static final ChatCommand RESURRECT = new ResuscitationCommand();
	
	private ResuscitationCommand() {
		super("rn", "resuscitation", "Get resurrected");
		
		addRequiredRole(ConversationRole.EXPERT);
	}

	@Override
	public boolean execute(Conversation context, String input, Person subject) {
		
		if (!subject.isDeclaredDead()) {
			context.println("Person is not dead");
			return false;
		}
		subject.getPhysicalCondition().reviveToLife();

		context.println(subject.getName() + " was just being revived and under recovery.");
		
		return false;
	}
}
