/*
 * Mars Simulation Project
 * BedCommand.java
 * @date 2022-08-24
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.person;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.person.Person;

/** 
 * 
 */
public class BedCommand extends AbstractPersonCommand {
	public static final ChatCommand BED = new BedCommand();
	
	private BedCommand() {
		super("be", "bed", "About my bed assignment");
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {
		LocalPosition bed = person.getBed();
		if (bed == null) {
			context.println("I haven't got my own private quarters yet.");
		} 
		else {
			StringBuilder responseText = new StringBuilder();
			responseText.append("My designated quarters is at ");
			responseText.append(bed.getShortFormat());
			responseText.append(" in ");
			responseText.append(person.getQuarters());
			responseText.append(" at ");
			responseText.append(person.getAssociatedSettlement());

			context.println(responseText.toString());
		}
		
		return true;
	}
}
