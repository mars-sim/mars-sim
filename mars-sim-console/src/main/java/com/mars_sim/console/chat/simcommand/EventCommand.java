/*
 * Mars Simulation Project
 * EventCommand.java
 * @date 2024-08-10
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand;

import java.util.List;
import java.util.Objects;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.events.HistoricalEventManager;

/**
 * Command to stop speaking with an entity.
 * This is a singleton.
 */
public class EventCommand extends ChatCommand {

	public static final EventCommand EVENT = new EventCommand();

	private static final int EVENT_SIZE = 7;

	private EventCommand() {
		super(TopLevel.SIMULATION_GROUP, "ev", "events", "Display recent events");
	}

	@Override
	public boolean execute(Conversation context, String input) {
		HistoricalEventManager mgr = context.getSim().getEventManager();
		List<HistoricalEvent> events = mgr.getEvents();
		
		if (events.isEmpty()) {
			context.println("None to display");
		}
		else {
			StructuredResponse response = new StructuredResponse();
			
			int latest = events.size() - 1;
			int lastId = Math.max(latest - EVENT_SIZE, 0);
			for(int idx = latest; idx >= lastId; idx--) {
				HistoricalEvent e = events.get(idx);
				String source = Objects.requireNonNullElse(e.getSource(), "").toString();
				
				response.appendHeading(e.getCategory().getName() + " @ " + e.getTimestamp().getDateTimeStamp());
				response.appendLabeledString("Type", e.getType().getName());
				response.appendLabeledString("Source", source);
				response.appendLabeledString("Cause", e.getWhatCause());
				response.appendLabeledString("Entity", (e.getEntity() != null ?
								e.getEntity().getName() : ""));
				response.appendLabeledString("Coords", e.getCoordinates().getFormattedString());
				var homeTown = e.getHomeTown();
				response.appendLabeledString("Settlement", homeTown != null ? homeTown.getName() : "");

				response.appendBlankLine();
			}
			
			context.println(response.getOutput());
		}
		return true;
	}

}
