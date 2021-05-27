package org.mars.sim.console.chat.simcommand.settlement;

import java.util.List;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Command to display Job prospect. This is a composite command
 * This is a singleton.
 */
public class JobProspectCommand extends AbstractSettlementCommand {

	private static final class JobProspect implements Comparable<JobProspect> {
		Person person;
		double prospect;
		
		public JobProspect(Person person, double prospect) {
			super();
			this.person = person;
			this.prospect = prospect;
		}

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
				.map(j -> j.getName(GenderType.MALE))
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
			response.append("Sorry I don't know a job called '" + input + "'" + System.lineSeparator());
			result = false;
		}
		else {
			final JobType job = JobType.getJobTypeByName(input); 

			List<JobProspect> prospects = settlement.getAllAssociatedPeople().stream()
					.map(p -> new JobProspect(p, JobUtil.getJobProspect(p, job, settlement, true)))
					.sorted((p1, p2) -> p1.compareTo(p2))
					.collect(Collectors.toList());
			
			response.appendTableHeading(job.getName() + " Job Prospect", CommandHelper.PERSON_WIDTH,
										"Current", CommandHelper.JOB_WIDTH, "Scores");
			for (JobProspect p : prospects) {
				response.appendTableRow(p.person.getName(), p.person.getMind().getJob(), p.prospect);
			}
		}

		context.println(response.getOutput());
		return result;
	}
}
