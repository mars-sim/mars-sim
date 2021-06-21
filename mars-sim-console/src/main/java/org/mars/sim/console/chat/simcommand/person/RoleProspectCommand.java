/**
 * Mars Simulation Project
 * RoleProspectCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.person;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.role.RoleUtil;

public class RoleProspectCommand extends AbstractPersonCommand {
	public static final ChatCommand ROLE_PROSPECT = new RoleProspectCommand();
	private RoleProspectCommand() {
		super("rp", "role prospect", "About my role prospects");
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {
		StructuredResponse response = new StructuredResponse();

		List<RoleType> list = Arrays.asList(RoleUtil.SPECIALISTS);
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
