package org.mars.sim.console.chat.simcommand.person;

import java.util.Collections;
import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobUtil;

/** 
 * 
 */
public class JobProspectCommand extends AbstractPersonCommand {
	public static final ChatCommand JOB_PROSPECT = new JobProspectCommand();
	private JobProspectCommand() {
		super("jp", "job prospect", "About my job prospects");
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {
		StructuredResponse response = new StructuredResponse();
		List<String> jobList = JobUtil.getJobList();
		Collections.sort(jobList);

		response.appendTableHeading("Job", CommandHelper.JOB_WIDTH, "Capability Score", "Prospect Score");

		for (String jobStr : jobList) {

			Job job = JobUtil.getJob(jobStr);

			double capScore = Math.round(job.getCapability(person) * 10.0) / 10.0;
			double prospectScore = Math.round(
					JobUtil.getJobProspect(person, job, person.getAssociatedSettlement(), true) * 10.0)
					/ 10.0;

			response.appendTableRow(jobStr, capScore, prospectScore);
		}
		
		context.println(response.getOutput());
		
		return true;
	}
}
