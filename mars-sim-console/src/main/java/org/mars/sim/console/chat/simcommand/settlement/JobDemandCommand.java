package org.mars.sim.console.chat.simcommand.settlement;

import java.util.List;
import java.util.Map;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Command to display Job display. This is a composite command
 * This is a singleton.
 */
public class JobDemandCommand extends AbstractSettlementCommand {

	public static final ChatCommand DEMAND = new JobDemandCommand();
	
	private static final String DESC = "Job demand details";

	private static final String DEMAND_FORMAT = "%14s | %6s | %6s | %s%n";

	
	private JobDemandCommand() {
		super("jd", "job demand", DESC);
	}

	/** 
	 * Output the current immediate location of the Unit
	 */
	@Override
	protected void execute(Conversation context, String input, Settlement settlement) {

		StructuredResponse response = new StructuredResponse();
		response.appendHeading("Job Demand");
		response.append(String.format(DEMAND_FORMAT, "Job", "Demand", "Filled", "Deficit"));
		response.appendSeperator();

		Map<String, List<Person>> map = JobUtil.getJobMap(settlement);

		List<Job> jobs = JobUtil.getJobs();
		for (Job job : jobs) {
			String jobName = job.getName(GenderType.MALE);

			String demand = "" + Math.round(job.getSettlementNeed(settlement) * 10.0) / 10.0;

			String positions = "0";
			if (map.get(jobName) != null)
				positions = "" + map.get(jobName).size();

			String deficit = ""
					+ Math.round(JobUtil.getRemainingSettlementNeed(settlement, job) * 10.0) / 10.0;			

			response.append(String.format(DEMAND_FORMAT, jobName, demand, positions, deficit));
		}

		context.println(response.getOutput());
	}
}
