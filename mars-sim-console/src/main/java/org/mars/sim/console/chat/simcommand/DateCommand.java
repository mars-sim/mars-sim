/**
 * Mars Simulation Project
 * DateCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.ConversationRole;
import org.mars_sim.msp.core.time.MarsClockFormat;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;

/**
 * Command to stop speaking with an entity.
 * This is a singleton.
 */
public class DateCommand extends ChatCommand {

	public static final ChatCommand DATE = new DateCommand();
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

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
		LocalDateTime earthClock = clock.getEarthTime();
		MarsClock marsClock = clock.getMarsClock();

		responseText.appendLabelledDigit("Mission Sol", marsClock.getMissionSol());
		responseText.appendLabeledString("Mars Date", MarsClockFormat.getDateString(marsClock));
		responseText.appendLabeledString("Mars Time", MarsClockFormat.getDecimalTimeString(marsClock));
		responseText.appendLabeledString("Earth Date", earthClock.format(DATE_FORMAT));
		responseText.appendLabeledString("Earth Time", earthClock.format(TIME_FORMAT));
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
