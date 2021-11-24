/**
 * Mars Simulation Project
 * MissionPlanCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand;

import java.util.Map;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.person.ai.mission.PlanType;

/**
 * Command to display mission stats
 * This is a singleton.
 */
public class MissionPlanCommand extends ChatCommand {

	public static final ChatCommand MISSION_PLAN = new MissionPlanCommand();

	private MissionPlanCommand() {
		super(TopLevel.SIMULATION_GROUP, "mp", "mission plan", "Planned mission counts");
		
		setInteractive(true);
	}

	@Override
	public boolean execute(Conversation context, String input) {

		String prompt2 = "Show me the statistics on the mission plans submitted."
				+ System.lineSeparator() + System.lineSeparator() + " 1. Today" + System.lineSeparator()
				+ " 2. last 3 sols (Each)" + System.lineSeparator() + " 3. Last 7 sols (Each)"
				+ System.lineSeparator() + " 4. last 3 sols (Combined)" + System.lineSeparator()
				+ " 5. Last 7 sols (Combined)" + System.lineSeparator() + " 6. Last 14 sols (Combined)"
				+ System.lineSeparator() + " 7. Last 28 sols (Combined)" + System.lineSeparator()
				+ " 8. Since the beginning (Combined)" + System.lineSeparator() + System.lineSeparator()
				+ "Enter your choice (1-8)";

		context.println(prompt2);
		int newObj = context.getIntInput("Select an option ");
		int max = 0;
		boolean totals = false;
		int today = context.getSim().getMasterClock().getMarsClock().getMissionSol();

		switch (newObj) {
		case 1:
			max = 1;
			break;

		case 2:
			max = 3;
			break;

		case 3:
			max = 7;
			break;
		
		case 4:			
			totals = true;
			max = 3;
			break;
			
		case 5:
			max = 7;
			totals = true;
			break;
			
		case 6:
			max = 14;
			totals = true;
			break;
			
		case 7:
			max = 28;
			totals = true;
			break;
			
		case 8:
			max = today;
			totals = true;
			break;
			
		default:
			context.println("Please enter a valid input.");
			return false;
		}	

		StructuredResponse response = new StructuredResponse();

		response.appendHeading("On Sol " + today + ", the " + (totals ? "combined " : "")
								+ "data for the last " + max + " sols shows");

		Map<Integer, Map<String, Double>> plannings = context.getSim().getMissionManager().getHistoricalMissions();

		int totalApproved = 0;
		int totalNotApproved = 0;
		int firstSol = today;
		int lastSol = Math.max(0, today - max); // Don't go negative
		for (int i = firstSol; i > lastSol; i--) {
			Map<String, Double> plans = plannings.get(i);
			int approved = (int) plans.getOrDefault(PlanType.APPROVED.name(), 0D).doubleValue();
			int notApproved = (int) plans.getOrDefault(PlanType.NOT_APPROVED.name(), 0D).doubleValue();

			if (totals) {
				totalApproved += approved;
				totalNotApproved += notApproved;
			}
			else {
				response.appendText("Stats for sol " + i);
				response.appendLabelledDigit("# of plans approved", approved);
				response.appendLabelledDigit("# of plans not approved", notApproved);
			}
		}
		
		if (totals) {
			response.appendLabelledDigit("# of plans approved", totalApproved);
			response.appendLabelledDigit("# of plans not approved", totalNotApproved);
		}
		
		context.println(response.getOutput());
		return true;
	}
}
