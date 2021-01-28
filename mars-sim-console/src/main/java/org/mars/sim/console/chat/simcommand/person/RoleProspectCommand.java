package org.mars.sim.console.chat.simcommand.person;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.role.RoleUtil;

/** 
 * 
 */
public class RoleProspectCommand extends AbstractPersonCommand {
	public static final ChatCommand ROLE_PROSPECT = new RoleProspectCommand();
	private RoleProspectCommand() {
		super("rp", "role prospect", "About my role prospects");
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {
		StructuredResponse response = new StructuredResponse();

		List<RoleType> list = Arrays.asList(RoleUtil.specialistRoles);
		Collections.sort(list);

		response.appendTableHeading("Role", CommandHelper.ROLE_WIDTH, "Job Score", "Training Score", "Total");

		Job job = person.getMind().getJob();
		int id = job.getJobID();
		double[] weights = RoleUtil.getRoleWeights().get(id);
		
		for (RoleType roleType : list) {
			double jScore = Math.round(
					RoleUtil.getJobScore(person, roleType, weights) * 10.0)
					/ 10.0;
			double tScore = Math.round(
					RoleUtil.getTrainingScore(person, roleType, weights) * 10.0)
					/ 10.0;
			double total = Math.round((jScore + tScore) * 10.0) / 10.0;

			response.appendTableRow(roleType.getName(), jScore, tScore, total); 
		}
		context.println(response.getOutput());
		
		return true;
	}
}
