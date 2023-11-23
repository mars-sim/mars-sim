/*
 * Mars Simulation Project
 * ResuscitationCommand.java
 * @date 2023-11-22
 * @author Manny Kung
 */

package com.mars_sim.console.chat.simcommand.person;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.ConversationRole;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.person.GenderType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.health.BodyRegionType;
import com.mars_sim.core.person.health.ComplaintType;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.person.health.RadiationExposure;
import com.mars_sim.core.person.health.RadiationType;
import com.mars_sim.tools.util.RandomUtil;

/** 
 * The command for resuscitating a dead person.
 */
public class ResuscitationCommand extends AbstractPersonCommand {
	public static final ChatCommand RESURRECT = new ResuscitationCommand();
	
	private ResuscitationCommand() {
		super("re", "resuscitation", "Get resurrected");
		
		setInteractive(true);
		addRequiredRole(ConversationRole.EXPERT);
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {
		
		Collection<Person> persons = Simulation.instance().getUnitManager().getPeople();

		List<Person> dead = new ArrayList<>();
		for (Person p: persons) {
			if (p.isDeclaredDead())
				dead.add(p);
		}
		Collections.sort(dead);
	
		List<String> deadNames = new ArrayList<>();
		for (Person p: dead) {
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
