/*
 * Mars Simulation Project
 * FutureEventCommand.java
 * @date 2024-08-10
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.settlement;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.events.ScheduledEventManager;
import com.mars_sim.core.events.ScheduledEventManager.ScheduledEvent;
import com.mars_sim.core.structure.Settlement;

/**
 * Command to display settlement future scheduled events for either a Settlement or global Simulation.
 * Notes: this is a singleton.
 */
public class FutureEventCommand extends ChatCommand {

	public static final ChatCommand FUTURE = new FutureEventCommand();

	private FutureEventCommand() {
		super(COMMAND_GROUP, "fe", "future", "Future scheduled events");
	}

	/** 
	 * @return 
	 */
	@Override
	public boolean execute(Conversation context, String input) {
		ScheduledEventManager futures = null;
		StructuredResponse response = new StructuredResponse();

		// Identify the context to get the correct ScheduledEventManager
		if (context.getCurrentCommand() instanceof SettlementChat sc) {
			Settlement settlement = sc.getSettlement();
			futures = settlement.getFutureManager();
			response.appendHeading("Events in " + settlement.getName());
		}
		else {
			futures = context.getSim().getScheduleManager();
			response.appendHeading("Global Scheduled Events");
		}

		response.appendTableHeading("When", 24, "Event");
							
		// Display each farm separately
		for (ScheduledEvent event : futures.getEvents()) {			
			response.appendTableRow(event.getWhen().getTruncatedDateTimeStamp(), event.getDescription());
		}
		context.println(response.getOutput());
		return true;
	}
}
