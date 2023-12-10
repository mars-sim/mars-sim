/*
 * Mars Simulation Project
 * BedCommand.java
 * @date 2022-08-24
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.person;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.building.function.ActivitySpot;

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
		ActivitySpot bed = person.getBed();
		if (bed == null) {
			context.println("I haven't got my own private quarters yet.");
		} 
		else {
			StringBuilder responseText = new StringBuilder();
			responseText.append("My designated quarters is ");
			responseText.append(bed.getName() + " at ");
			responseText.append(bed.getPos().getShortFormat());
			responseText.append(" in ");
			responseText.append(person.getQuarters());
			responseText.append(" at ");
			responseText.append(person.getAssociatedSettlement());

			context.println(responseText.toString());
		}
		
		return true;
	}
}
