/*
 * Mars Simulation Project
 * SleepCommand.java
 * @date 2022-08-24
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
		var response = new StructuredResponse();

		CircadianClock cc = person.getCircadianClock();
		int[] preferences = cc.getPreferredSleepHours();
		for(int i = 0; i < preferences.length; i++) {
			response.appendLabeledString("Preferred Sleep hours #" + (i+1), preferences[i] + " millisols.");
		}
	
		Map<Integer, Double> history = cc.getSleepHistory();
		response.appendTableHeading("Sol", 3, "Sleep duration");
		for(Entry<Integer, Double> i : history.entrySet()) {
			response.appendTableRow(i.getKey().toString(), i.getValue());
		}
		
		context.println(response.getOutput());
		return true;
	}
}
