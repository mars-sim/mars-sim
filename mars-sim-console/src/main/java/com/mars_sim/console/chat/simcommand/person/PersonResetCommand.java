/*
 * Mars Simulation Project
 * PersonResetCommand.java
 * @date 2022-08-24
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.person;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.ConversationRole;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;

/** 
 * Expert command to reset a person's 5 performance factors back to zero
 */
public class PersonResetCommand extends AbstractPersonCommand {
	public static final ChatCommand RESET = new PersonResetCommand();
	
	private PersonResetCommand() {
		super("res", "reset", "Reset a person's 5 performance factors");
		
		addRequiredRole(ConversationRole.EXPERT);
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {
		context.println("Restore a person's 5 performance factors");
		
		if (person.isDeclaredDead() || person.isBuried()) {
			context.println("This person is dead. Use Settlement's Resuscitation command instead !");
			return false;
		}
		else {
			PhysicalCondition pc = person.getPhysicalCondition();
			pc.setFatigue(0D);
			pc.setHunger(0D);
			pc.setStress(0D);
			pc.setThirst(0D);
			pc.setPerformanceFactor(1D);
		}
		return true;
	}
}
