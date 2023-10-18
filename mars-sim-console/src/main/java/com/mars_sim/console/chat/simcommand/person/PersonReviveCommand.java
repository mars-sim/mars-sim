/*
 * Mars Simulation Project
 * PersonReviveCommand.java
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
 * Expert command to revive a Person 
 */
public class PersonReviveCommand extends AbstractPersonCommand {
	public static final ChatCommand REVIVE = new PersonReviveCommand();
	
	private PersonReviveCommand() {
		super("rv", "revive", "Revive a Person");
		
		addRequiredRole(ConversationRole.EXPERT);
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {
		context.println("Revive the Person's health");
		
		PhysicalCondition pc = person.getPhysicalCondition();
		pc.setFatigue(0D);
		pc.setHunger(0D);
		pc.setStress(0D);
		pc.setThirst(0D);
		pc.setPerformanceFactor(1D);
		return true;
	}
}
