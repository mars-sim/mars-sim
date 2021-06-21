/**
 * Mars Simulation Project
 * SleepCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.person;

import java.util.Map;
import java.util.Map.Entry;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.CircadianClock;
import org.mars_sim.msp.core.person.Person;

/** 
 * 
 */
public class SleepCommand extends AbstractPersonCommand {
	public static final ChatCommand SLEEP = new SleepCommand();
	
	private SleepCommand() {
		super("sl", "sleep", "Sleep hour");
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {
		CircadianClock cc = person.getCircadianClock();
		int[] twos = cc.getPreferredSleepHours();
		int small = Math.min(twos[0], twos[1]);
		int large = Math.max(twos[0], twos[1]);

		var response = new StructuredResponse();
		response.appendLabeledString("Preferred Sleep hours", small + " or " + large + " millisols.");
		
		Map<Integer, Double> history = cc.getSleepTime();
		response.appendTableHeading("Sol", 3, "Sleep duration");
		for(Entry<Integer, Double> i : history.entrySet()) {
			response.appendTableRow(i.getKey().toString(), i.getValue());
		}
		
		context.println(response.getOutput());
		return true;
	}
}
