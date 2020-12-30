package org.mars.sim.console.chat.simcommand.person;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ShiftType;

/** 
 * 
 */
public class ShiftCommand extends AbstractPersonCommand {
	public static final ChatCommand SHIFT = new ShiftCommand();
	
	private ShiftCommand() {
		super("sh", "shift", "Shift patterns");
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {
		StructuredResponse response = new StructuredResponse();
		
		ShiftType st0 = person.getTaskSchedule().getShiftType();
		int score = person.getTaskSchedule().getShiftChoice().get(st0);
		response.appendLabeledString("Current Work shift", st0 + " (score : " + score + ")");

		int p = 1;
		ShiftType[] st = person.getTaskSchedule().getPreferredShift();
		for (ShiftType shiftType : st) {
			score = person.getTaskSchedule().getShiftChoice().get(shiftType);
			response.appendLabeledString("Preference #" + p++, shiftType + " (score : " + score + ")");			
		}
		context.println(response.getOutput());
		
		return true;
	}
}
