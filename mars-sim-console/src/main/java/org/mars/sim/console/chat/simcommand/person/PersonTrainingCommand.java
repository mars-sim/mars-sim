package org.mars.sim.console.chat.simcommand.person;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.TrainingType;

/** 
 * Reports on a Persons training
 */
public class PersonTrainingCommand extends ChatCommand {

	
	public PersonTrainingCommand() {
		super(PersonChat.PERSON_GROUP, "t", "training", "About my training");
	}

	@Override
	public void execute(Conversation context, String input) {
		PersonChat parent = (PersonChat) context.getCurrentCommand();
		Person person = parent.getPerson();
		
		StringBuffer responseText = new StringBuffer();
		
		responseText.append(String.format("%24%n", "Type of Training"));
		responseText.append(" ---------------------------- ");
		
		for (TrainingType tt : person.getTrainings()) {
			responseText.append(String.format("%22%n", tt.getName()));
		}	
		context.println(responseText.toString());
	}

}
