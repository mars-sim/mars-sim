/*
 * Mars Simulation Project
 * RoleCommand.java
 * @date 2024-08-10
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.settlement;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.Settlement;

public class RoleCommand extends AbstractSettlementCommand {
	public static final ChatCommand ROLE = new RoleCommand();

	private RoleCommand() {
		super("rl", "role", "Settlement allocation of roles");
	}

	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();
		
		response.appendTableHeading("Role", CommandHelper.ROLE_WIDTH, "Name");
		
		List<Person> list = settlement.getAllAssociatedPeople().stream()
				.sorted(Comparator.comparing(o -> o.getRole().getType().ordinal())).collect(Collectors.toList());

		for (Person p : list) {
			String role = p.getRole().getType().getName();
			response.appendTableRow(role, p.getName());
		}
		
		context.println(response.getOutput());
		return true;
	}

}
