/*
 * Mars Simulation Project
 * JobProspectCommand.java
 * @date 2022-07-06
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.settlement;

import java.util.List;
import java.util.stream.Collectors;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.job.util.JobUtil;
import com.mars_sim.core.structure.Settlement;

/**
 * Command to display Job prospect. This is a composite command
 * This is a singleton.
 */
public class JobProspectCommand extends AbstractSettlementCommand {

	private static record JobProspect (Person person, double prospect)
			implements Comparable<JobProspect> {

		@Override
		public int compareTo(JobProspect o) {
			int diff = Double.compare(o.prospect, prospect);
			if (diff == 0) {
				diff = person.getName().compareTo(o.person.getName());
			}
			return diff;
		}
	}
	
	public static final ChatCommand PROSPECT = new JobProspectCommand();

	private static final String DESC = "Job prospect takes an argument <job>";
	
	private JobProspectCommand() {
		super("jp", "job prospect", DESC);
		
		setIntroduction("Display the Job prospected for a job");
		
		// Setup  
		// Setup the fixed arguments
		setArguments(JobUtil.getJobs().stream()
				.map(j -> j.getType().getName())
				.collect(Collectors.toList()));
	}

	/** 
	 * Output the current immediate location of the Unit
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();
		boolean result = true;
		
		if (input == null || input.isBlank()) {
			response.append("Must specify a job as an argument. (e.g. /jp psychologist)" + System.lineSeparator());
			result = false;
		}
		else {
			final JobType job = JobType.getJobTypeByName(input); 
			if (job == null) {
				response.append("Invalid job. Please try again." + System.lineSeparator());
				return false;
			}
			
			List<JobProspect> prospects = settlement.getAllAssociatedPeople().stream()
					.map(p -> new JobProspect(p, JobUtil.getJobProspect(p, job, settlement, true)))
					.sorted(Comparable::compareTo)
					.collect(Collectors.toList());
			
			response.appendTableHeading(job.getName() + " Job Prospect", CommandHelper.PERSON_WIDTH,
										"Current", CommandHelper.JOB_WIDTH, "Scores");
			for (JobProspect p : prospects) {
				response.appendTableRow(p.person.getName(), p.person.getMind().getJobType(), p.prospect);
			}
		}

		context.println(response.getOutput());
		return result;
	}
}
