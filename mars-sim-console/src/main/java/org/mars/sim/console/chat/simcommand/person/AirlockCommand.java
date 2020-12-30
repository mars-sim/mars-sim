package org.mars.sim.console.chat.simcommand.person;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;

/** 
 * 
 */
public class AirlockCommand extends AbstractPersonCommand {
	public static final ChatCommand AIRLOCK = new AirlockCommand();
	
	private AirlockCommand() {
		super("al", "airlock", "Airlock times");
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {
		StructuredResponse response = new StructuredResponse();
		
		response.appendTableHeading("Sol", 5, "Millisols");


		int size = context.getSim().getMasterClock().getMarsClock().getMissionSol();

		for (int i = 1; i <= size; i++) {
			double milliSol = person.getTaskSchedule().getAirlockTasksTime(i);
			response.appendTableRow("" + i, milliSol);
		}
		
		context.println(response.getOutput());
		
		return true;
	}
}
