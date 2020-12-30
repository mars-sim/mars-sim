package org.mars.sim.console.chat.simcommand.settlement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Command to display Job Roster. This is a composite command
 * This is a singleton.
 */
public class JobRosterCommand extends AbstractSettlementCommand {

	public static final ChatCommand ROSTER = new JobRosterCommand();
	
	private static final String DESC = "Job roster details";

	private static final String PERSON_ROSTER = "%" + PERSON_WIDTH + "s - %s%n";
	private static final String JOB_ROSTER = "%" + JOB_WIDTH + "s - %s%n";

	
	private JobRosterCommand() {
		super("jr", "job roster", DESC);
	}


	/** 
	 * Output the current immediate location of the Unit
	 * @return 
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {

		StructuredResponse response = new StructuredResponse();
		response.appendHeading("Job Roster by Person");
		List<Person> list = settlement.getAllAssociatedPeople().stream()
				.sorted((p1, p2) -> p1.getName().compareTo(p2.getName())).collect(Collectors.toList());

		for (Person p : list) {
			String job = p.getMind().getJob().getName(p.getGender());
			response.append(String.format(PERSON_ROSTER, p.getName(), job));
		}

		response.append(System.lineSeparator());
		response.appendHeading("Job Roster by Job");
		Map<String, List<Person>> map = JobUtil.getJobMap(settlement);

		List<String> jobList = new ArrayList<>(map.keySet());
		Collections.sort(jobList);
		for (String jobStr : jobList) {
			List<Person> plist = map.get(jobStr);
			Collections.sort(plist);

			for (Person p : plist) {
				response.append(String.format(JOB_ROSTER, jobStr, p.getName()));
				jobStr = "";
			}
		}
		
		context.println(response.getOutput());
		return true;
	}
}
