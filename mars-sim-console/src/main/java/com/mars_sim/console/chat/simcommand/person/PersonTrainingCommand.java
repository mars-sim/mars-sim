/*
 * Mars Simulation Project
 * PersonTrainingCommand.java
 * @date 2022-08-24
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.person;

import com.mars_sim.console.chat.Conversation;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.training.TrainingType;

/** 
 * Reports on a Persons training.
 */
public class PersonTrainingCommand extends AbstractPersonCommand {

	
	public PersonTrainingCommand() {
		super("t", "training", "About my training");
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {

		StringBuffer responseText = new StringBuffer();
		
		responseText.append(String.format("%24s%n", "Type of Training"));
		responseText.append(" ---------------------------- ");
		responseText.append(System.lineSeparator());
		
		for (TrainingType tt : person.getTrainings()) {
			responseText.append(String.format("%-22s%n", tt.getName()));
		}	
		context.println(responseText.toString());
		
		return true;
	}
}
