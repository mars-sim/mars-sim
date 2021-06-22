/**
 * Mars Simulation Project
 * MissionSummaryCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand;

import java.util.Collection;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
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

		Collection<Settlement> settlements = context.getSim().getUnitManager().getSettlements();
		
		StructuredResponse response = new StructuredResponse();
		response.appendTableHeading("Name",  CommandHelper.PERSON_WIDTH, "Sponsor", 
									"Population");
		for(Settlement s : settlements) {
			response.appendTableRow(s.getName(),
					                s.getSponsor(),
									s.getNumCitizens());
		}

		context.println(response.getOutput());
		return true;
	}
}
