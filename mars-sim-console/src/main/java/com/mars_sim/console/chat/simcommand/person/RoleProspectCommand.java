/*
 * Mars Simulation Project
 * RoleProspectCommand.java
 * @date 2022-08-24
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.person;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.role.RoleUtil;

public class RoleProspectCommand extends AbstractPersonCommand {
	public static final ChatCommand ROLE_PROSPECT = new RoleProspectCommand();
	private RoleProspectCommand() {
		super("rp", "role prospect", "About my role prospects");
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {
		StructuredResponse response = new StructuredResponse();

		List<RoleType> list = new ArrayList<>(RoleUtil.getSpecialists());
		Collections.sort(list);

		response.appendTableHeading("Role", CommandHelper.ROLE_WIDTH, "Job Score", "Training Score", "Total");

		JobType job = person.getMind().getJob();
		Map<RoleType, Double> weights = RoleUtil.getRoleWeights().get(job);
		
		for (RoleType roleType : list) {
			double jScore = Math.round(
					weights.get(roleType) * 10.0)
					/ 10.0;
			double tScore = Math.round(
					RoleUtil.getTrainingScore(person, roleType) * 10.0)
					/ 10.0;
			double total = Math.round((jScore + tScore) * 10.0) / 10.0;

			response.appendTableRow(roleType.getName(), jScore, tScore, total); 
		}
		context.println(response.getOutput());
		
		return true;
	}
}
