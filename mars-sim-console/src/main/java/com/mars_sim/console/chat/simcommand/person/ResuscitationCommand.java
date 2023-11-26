/*
 * Mars Simulation Project
 * ResuscitationCommand.java
 * @date 2023-11-22
 * @author Manny Kung
 */

package com.mars_sim.console.chat.simcommand.person;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.ConversationRole;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.settlement.AbstractSettlementCommand;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.Settlement;

/** 
 * The command for resuscitating a dead person.
 */
public class ResuscitationCommand extends AbstractSettlementCommand {
	public static final ChatCommand RESURRECT = new ResuscitationCommand();
	
	private ResuscitationCommand() {
		super("re", "resuscitation", "Get resurrected");
		
		setInteractive(true);
		addRequiredRole(ConversationRole.EXPERT);
	}

	@Override
	public boolean execute(Conversation context, String input, Settlement settlement) {
		
		Collection<Person> persons = settlement.getAllAssociatedPeople();

		List<Person> deadPersons = new ArrayList<>();
		for (Person p: persons) {
			if (p.isDeclaredDead())
				deadPersons.add(p);
		}
		Collections.sort(deadPersons);
	
		List<String> deadNames = new ArrayList<>();
		for (Person p: deadPersons) {
			deadNames.add(p.getName());
		}
		Collections.sort(deadNames);
		
		// Choose one
		int choice = CommandHelper.getOptionInput(context, deadNames, 
				"Pick a person to revive by entering a number");
		
		if (choice < 0) {
			context.println("No one was chosen. Try again.");
			return false;
		}

		String theName = deadNames.get(choice);
		Person theOne = null;
		
		for (Person p: persons) {
			if (p.getName().equalsIgnoreCase(theName)) {
				theOne = p;
				break;
			}
		}
		
		theOne.getPhysicalCondition().reviveToLife();
		
		context.println(theName + " was just being revived and under recovery.");
		
		return true;
	}
}
