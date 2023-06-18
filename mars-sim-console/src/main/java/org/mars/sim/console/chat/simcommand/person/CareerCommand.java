/*
 * Mars Simulation Project
 * CareerCommand.java
 * @date 2023-06-18
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.person;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.data.History.HistoryItem;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.role.Role;
import org.mars_sim.msp.core.person.ai.role.RoleType;

/** 
 * Display details about Person's job & roles
 */
public class CareerCommand extends AbstractPersonCommand {
	public static final ChatCommand FRIEND = new CareerCommand();
	
	private CareerCommand() {
		super("cr", "career", "About career");
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {

		StructuredResponse response = new StructuredResponse();

		Role r = person.getRole();
		response.appendLabeledString("Current Role", r.getType().getName());
		response.appendLabeledString("Current Job", person.getMind().getJob().getName());

		response.appendHeading("Role History");
		response.appendTableHeading("When", CommandHelper.TIMESTAMP_TRUNCATED_WIDTH,
									"Role", 20);
		for (HistoryItem<RoleType> rh : r.getChanges()) {
			response.appendTableRow(rh.getWhen().getTruncatedDateTimeStamp(), rh.getWhat().getName());
		}
		context.println(response.getOutput());

		return true;
	}
}
