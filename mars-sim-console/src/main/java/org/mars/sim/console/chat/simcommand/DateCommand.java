/**
 * Mars Simulation Project
 * DateCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.ConversationRole;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClockFormat;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;

/**
 * Command to stop speaking with an entity.
 * This is a singleton.
 */
public class DateCommand extends ChatCommand {

	public static final ChatCommand DATE = new DateCommand();

	private DateCommand() {
		super(TopLevel.SIMULATION_GROUP, "d", "date", "What is the date?");
	}

	/**
	 * Output the current simulation date time.
	 */
	@Override
	public boolean execute(Conversation context, String input) {

		StructuredResponse responseText = new StructuredResponse();
		MasterClock clock = context.getSim().getMasterClock();
		EarthClock earthClock = clock.getEarthClock();
		MarsClock marsClock = clock.getMarsClock();

		responseText.appendLabelledDigit("Mission Sol", marsClock.getMissionSol());
		responseText.appendLabeledString("Mars Date", MarsClockFormat.getDateString(marsClock));
		responseText.appendLabeledString("Mars Time", MarsClockFormat.getDecimalTimeString(marsClock));
		responseText.appendLabeledString("Earth Date", earthClock.getDateStringF4());
		responseText.appendLabeledString("Earth Time", earthClock.getTimeStringF0());
		responseText.appendLabeledString("Uptime", clock.getUpTimer().getUptime());

		if (context.getRoles().contains(ConversationRole.ADMIN)) {
			// For Admin user display details about the simulation engine
			responseText.appendBlankLine();
			responseText.appendLabelledDigit("Last Pulse execution (msec)", (int) clock.getExecutionTime());
			responseText.appendLabelledDigit("Last sleep time (msec)", (int) clock.getSleepTime());
			responseText.appendLabelledDigit("Pulse count", (int) clock.getTotalPulses());
		}

		context.println(responseText.getOutput());

		return true;
	}

}
