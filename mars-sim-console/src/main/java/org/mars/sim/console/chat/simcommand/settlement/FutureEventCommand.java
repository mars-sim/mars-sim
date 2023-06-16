/**
 * Mars Simulation Project
 * FutureEventCommand.java
 * @version 3.5.0 023-01-01
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.settlement;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.events.ScheduledEventManager.ScheduledEvent;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Command to display settlement future scheduled events.
 * Notes: this is a singleton.
 */
public class FutureEventCommand extends AbstractSettlementCommand {

	public static final ChatCommand FUTURE = new FutureEventCommand();

	private FutureEventCommand() {
		super("fe", "future", "Future scheduled events");
	}

	/** 
	 * @return 
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();
		response.appendTableHeading("When", 24, "Event");
							
		// Display each farm separately
		for (ScheduledEvent event : settlement.getFutureManager().getEvents()) {			
			response.appendTableRow(event.getWhen().getTrucatedDateTimeStamp(), event.getDescription());
		}
		context.println(response.getOutput());
		return true;
	}
}
