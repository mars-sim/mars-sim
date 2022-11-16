/**
 * Mars Simulation Project
 * SettelemntsCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Command to display Settlement stats
 * This is a singleton.
 */
public class SettlementsCommand extends ChatCommand {

	public static final ChatCommand SETTLEMENTS = new SettlementsCommand();

	private SettlementsCommand() {
		super(TopLevel.SIMULATION_GROUP, "se", "settlements", "Summary of all settlements");
	}

	@Override
	public boolean execute(Conversation context, String input) {

		List<Settlement> settlements = new ArrayList<>(context.getSim().getUnitManager().getSettlements());
		Collections.sort(settlements);

		StructuredResponse response = new StructuredResponse();
		response.appendTableHeading("Name",  CommandHelper.PERSON_WIDTH, "Sponsor", 
									"Template", 12, "Pop", "Location");
		for(Settlement s : settlements) {
			response.appendTableRow(s.getName(),
					                s.getSponsor().getName(),
					                s.getTemplate(),
									s.getNumCitizens(),
									s.getCoordinates().getFormattedString());
		}

		context.println(response.getOutput());
		return true;
	}
}
