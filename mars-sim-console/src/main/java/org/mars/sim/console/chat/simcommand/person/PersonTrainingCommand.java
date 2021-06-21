/**
 * Mars Simulation Project
 * PersonTrainingCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.person;

import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.TrainingType;

/** 
 * Reports on a Persons training
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
