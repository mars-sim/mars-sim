package org.mars.sim.console.chat.simcommand.settlement;

import java.util.List;
import java.util.Map;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Command to display Job display. This is a composite command
 * This is a singleton.
 */
public class JobDemandCommand extends AbstractSettlementCommand {

	public static final ChatCommand DEMAND = new JobDemandCommand();
	
	private static final String DESC = "Job demand details";

	
	private JobDemandCommand() {
		super("jd", "job demand", DESC);
	}

	/** 
	 * Output the current immediate location of the Unit
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {

		StructuredResponse response = new StructuredResponse();
		response.appendHeading("Job Demand");
		response.appendTableHeading("Job", CommandHelper.JOB_WIDTH, "Demand", "Filled", "Deficit");

		Map<JobType, List<Person>> map = JobRosterCommand.getJobMap(settlement);

		for (JobType job : JobType.values()) {
			Job jobSpec = JobUtil.getJobSpec(job);

			String demand = "" + Math.round(jobSpec.getSettlementNeed(settlement) * 10.0) / 10.0;

			String positions = "0";
			if (map.get(job) != null)
				positions = "" + map.get(job).size();

			String deficit = ""
					+ Math.round(JobUtil.getRemainingSettlementNeed(settlement, job) * 10.0) / 10.0;			

			response.appendTableRow(job.getName(), demand, positions, deficit);
		}

		context.println(response.getOutput());
		return true;
	}
}
