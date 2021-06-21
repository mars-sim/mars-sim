/**
 * Mars Simulation Project
 * BedCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.person;

import java.awt.geom.Point2D;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
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
		Point2D bed = person.getBed();
		if (bed == null) {
			context.println("I haven't got my own private quarters yet.");
		} 
		else {
			StringBuilder responseText = new StringBuilder();
			responseText.append("My designated quarters is at (");
			responseText.append(bed.getX());
			responseText.append(", ");
			responseText.append(bed.getY());
			responseText.append(") in ");
			responseText.append(person.getQuarters());
			responseText.append(" at ");
			responseText.append(person.getAssociatedSettlement());

			context.println(responseText.toString());
		}
		
		return true;
	}
}
