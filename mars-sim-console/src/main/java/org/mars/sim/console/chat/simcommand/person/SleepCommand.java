package org.mars.sim.console.chat.simcommand.person;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
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
	public void execute(Conversation context, String input, Person person) {
		int[] twos = person.getCircadianClock().getPreferredSleepHours();
		int small = Math.min(twos[0], twos[1]);
		int large = Math.max(twos[0], twos[1]);

		context.println("My preferred sleep hours are at either " + small + " or " + large + " millisols.");
	}

}
