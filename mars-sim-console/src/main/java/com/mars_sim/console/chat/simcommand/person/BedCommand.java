/*
 * Mars Simulation Project
 * BedCommand.java
 * @date 2022-08-24
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.person;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.core.building.function.ActivitySpot;
import com.mars_sim.core.person.Person;

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
		var bed = person.getBed();
		if (bed == null) {
			context.println("I haven't got my own private quarters yet.");
		} 
		else {
			ActivitySpot bedSpot = bed.getAllocated();
			StringBuilder responseText = new StringBuilder();
			responseText.append("My designated quarters is ");
			responseText.append(bed.getSpotDescription() + " (");
			responseText.append(bedSpot.getPos().getShortFormat());
			responseText.append(") at ");
			responseText.append(person.getAssociatedSettlement());

			context.println(responseText.toString());
		}
		
		return true;
	}
}
