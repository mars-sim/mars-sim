/*
 * Mars Simulation Project
 * ShiftCommand.java
 * @date 2022-08-24
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.person;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.task.utils.TaskSchedule;

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
		TaskSchedule ts = person.getTaskSchedule();
		
		ShiftType st0 = ts.getShiftType();
		int score = ts.getShiftChoice().get(st0);
		response.appendLabeledString("Current Work shift", st0 + " (score : " + score + ")");

		int p = 1;
		for (ShiftType shiftType : ts.getPreferredShift()) {
			score = ts.getShiftChoice().get(shiftType);
			response.appendLabeledString("Preference #" + p++, shiftType + " (score : " + score + ")");			
		}
		context.println(response.getOutput());
		
		return true;
	}
}
