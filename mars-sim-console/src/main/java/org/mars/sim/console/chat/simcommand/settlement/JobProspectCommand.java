package org.mars.sim.console.chat.simcommand.settlement;

import java.util.List;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.Conversion;

/**
 * Command to display Job prospect. This is a composite command
 * This is a singleton.
 */
public class JobProspectCommand extends AbstractSettlementCommand {

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
		
		List<Person> list = settlement.getAllAssociatedPeople().stream()
				.sorted((p1, p2) -> p1.getName().compareTo(p2.getName())).collect(Collectors.toList());
		
		Job job = null;
		if (input != null && !input.isBlank()) {
			job = JobUtil.getJob(input);
		}
		
		boolean result = true;
		if (job != null) {
			response.appendTableHeading(Conversion.capitalize(input) + " Job Prospect", CommandHelper.PERSON_WIDTH, "Scores");
			for (Person p : list) {
				double jobProspect = Math.round(JobUtil.getJobProspect(p, job, settlement, true) * 10.0) / 10.0;
				response.appendTableRow(p.getName(), jobProspect);
			}
		}
		else {
			response.append("Sorry I don't know a job called '" + input + "'" + System.lineSeparator());
			result = false;
		}
		
		context.println(response.getOutput());
		return result;
	}
}
