package org.mars.sim.console.chat.simcommand.settlement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.Conversion;

/**
 * Command to display Job display. This is a composite command
 * This is a singleton.
 */
public class JobCommand extends ChatCommand {

	public static final ChatCommand JOB = new JobCommand();
	
	// Su commands
	private static final String ROSTER = "roster";
	private static final String PROSPECT = "prospect";
	private static final String DEMAND = "demand";
	private static final String DESC = "Job details: takes argument (" + ROSTER + " | " 
										+ DEMAND + " | " + PROSPECT + " <job>)";

	private static final String PERSON_ROSTER = "%24s - %s%n";
	private static final String DEMAND_FORMAT = "%14s | %6s | %6s | %s%n";
	private static final String PROSPECT_FORMAT = "%24s %f%n";
	private static final String JOB_ROSTER = "%14s - %s%n";

	
	private JobCommand() {
		super(SettlementChat.SETTLEMENT_GROUP, "j", "job", DESC);
	}


	@Override
	public List<String> getAutoComplete(Conversation context, String parameter) {
		List<String> results = new ArrayList<>();

		if (parameter.isBlank()) {
			results.add(PROSPECT);
			results.add(ROSTER);
			results.add(DEMAND);
		}
		else if (ROSTER.startsWith(parameter)) {
			results.add(ROSTER);
		}
		else if (DEMAND.startsWith(parameter)) {
			// Partial
			results.add(DEMAND);
		}
		else if (parameter.startsWith(PROSPECT)) {
			// Full so should add in the possible jobs
			List<Job> jobs = JobUtil.getJobs();
			for (Job job : jobs) {
				results.add(PROSPECT + " " + job.getName(GenderType.MALE));
			}
		}
		else if (PROSPECT.startsWith(parameter)) {
			// Partial
			results.add(PROSPECT);
		}

		return results ;
	}

	/** 
	 * Output the current immediate location of the Unit
	 */
	@Override
	public void execute(Conversation context, String input) {
		SettlementChat parent = (SettlementChat) context.getCurrentCommand();		
		Settlement settlement = parent.getSettlement();
		StructuredResponse response = new StructuredResponse();

		if (input.startsWith(ROSTER)) {
			executeRoster(settlement, response);
		}
		else if (input.startsWith(PROSPECT)) {
			String job = input.substring(PROSPECT.length() + 1);
			executeProspect(settlement, job, response);
		}
		else if (input.startsWith(DEMAND)) {
			executeDemand(settlement, response);
		}
		else {
			response.append("Sorry I don't know about " + input + " for jobs");
		}
		
		context.println(response.getOutput());
	}

	/**
	 * Describe the job demand
	 * @param settlement
	 * @param response
	 */
	private void executeDemand(Settlement settlement, StructuredResponse response) {
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

	}

	/**
	 * Display the job prospect
	 * @param settlement
	 * @param jobName
	 * @param response
	 */
	private void executeProspect(Settlement settlement, String jobName, StructuredResponse response) {
		List<Person> list = settlement.getAllAssociatedPeople().stream()
				.sorted((p1, p2) -> p1.getName().compareTo(p2.getName())).collect(Collectors.toList());

		Job job = JobUtil.getJob(jobName);
		if (job != null) {
			response.appendHeading(Conversion.capitalize(jobName) + " Job Prospect Scores");
			for (Person p : list) {
				double jobProspect = Math.round(JobUtil.getJobProspect(p, job, settlement, true) * 10.0) / 10.0;
				response.append(String.format(PROSPECT_FORMAT, p, jobProspect));
			}
		}
		else {
			response.append("Sorry I don't know a job called '" + jobName + "'");
		}
	}


	private void executeRoster(Settlement settlement, StructuredResponse response) {
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
		
	}

}
