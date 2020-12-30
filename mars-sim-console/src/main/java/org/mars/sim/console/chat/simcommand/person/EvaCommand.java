package org.mars.sim.console.chat.simcommand.person;

import java.util.Map;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;

/** 
 * 
 */
public class EvaCommand extends AbstractPersonCommand {
	public static final ChatCommand EVA = new EvaCommand();
	
	private EvaCommand() {
		super("e", "eva", "EVA time");
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {
		StructuredResponse response = new StructuredResponse();
		
		response.appendTableHeading("Sol", 5, "Millisols");

		Map<Integer, Double> eVATime = person.getTotalEVATaskTimeBySol();
		int size = context.getSim().getMasterClock().getMarsClock().getMissionSol();
		for (int i = 0; i < size; i++) {
			if (eVATime.containsKey(i)) {
				double milliSol = eVATime.get(i);
				milliSol = Math.round(milliSol * 10.0) / 10.0;
				response.appendTableRow("" + i, milliSol);
			}
		}
		
		context.println(response.getOutput());
		return true;
	}

}
