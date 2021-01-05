package org.mars.sim.console.chat.simcommand;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClock;

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

		StringBuilder responseText = new StringBuilder();
		
		// Mars/Earth Date and Time
		EarthClock earthClock = context.getSim().getMasterClock().getEarthClock();
		String earthDate = earthClock.getDateStringF3();
		String earthTime = earthClock.getTimeStringF0();
		
		MarsClock marsClock = context.getSim().getMasterClock().getMarsClock();
		int missionSol = marsClock.getMissionSol();
		String marsDate = marsClock.getDateString();
		String marsTime = marsClock.getDecimalTimeString();
		
		responseText.append(System.lineSeparator());
		String s0 = "Mission Sol : ";
		int num = 20 - s0.length();
		for (int i = 0; i < num; i++) {
			responseText.append(" ");
		}
		responseText.append(s0);
		responseText.append(missionSol);
		responseText.append(System.lineSeparator());
		responseText.append(System.lineSeparator());

		String s1 = "Mars Date : ";
		num = 20 - s1.length();
		for (int i = 0; i < num; i++) {
			responseText.append(" ");
		}
		responseText.append(s1);
		responseText.append(marsDate);
		responseText.append(System.lineSeparator());

		String s2 = "Mars Time : ";
		num = 20 - s2.length();
		for (int i = 0; i < num; i++) {
			responseText.append(" ");
		}
		responseText.append(s2);
		responseText.append(marsTime);
		responseText.append(System.lineSeparator());
		responseText.append(System.lineSeparator());

		String s3 = "Earth Date : ";
		num = 20 - s3.length();
		for (int i = 0; i < num; i++) {
			responseText.append(" ");
		}
		responseText.append(s3);
		responseText.append(earthDate);
		responseText.append(System.lineSeparator());

		String s4 = "Earth Time : ";
		num = 20 - s4.length();
		for (int i = 0; i < num; i++) {
			responseText.append(" ");
		}
		responseText.append(s4);
		responseText.append(earthTime);
		responseText.append(System.lineSeparator());
		
		context.println(responseText.toString());
		
		return true;
	}

}
