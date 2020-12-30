package org.mars.sim.console.chat.simcommand;

import java.util.List;
import java.util.Map;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.person.ai.mission.MissionPlanning;
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
		int newObj = context.getIntInput("Select an option");
		int max = 0;
		boolean totals = false;
		if (newObj == 1) {
			max = 1;
		}
		else if (newObj == 2) {
			max = 3;
		}
		else if (newObj == 3) {
			max = 7;
		}
		else if (newObj == 4) {
			totals = true;
			max = 3;
		}
		else if (newObj == 5) {
			max = 7;
			totals = true;
		}
		else if (newObj == 6) {
			max = 14;
			totals = true;
		}
		else if (newObj == 7) {
			max = 28;
			totals = true;
		}
		else if (newObj == 8) {
			max = Integer.MAX_VALUE;
			totals = true;
		}
		else {
			context.println("Sorry wrong option");
			return false;
		}

		StructuredResponse response = new StructuredResponse();
		int today = context.getSim().getMasterClock().getMarsClock().getMissionSol();
		response.appendHeading("On Sol " + today + ", the " + (totals ? "combined " : "")
								+ "data for the last " + max + " sols shows");

		Map<Integer, List<MissionPlanning>> plannings = context.getSim().getMissionManager().getHistoricalMissions();

		int totalApproved = 0;
		int totalNotApproved = 0;
		int totalPending = 0;
		int firstSol = today;
		int lastSol = Math.max(0, today - max); // Don't go negative
		for (int i = firstSol; i > lastSol; i--) {
			List<MissionPlanning> plans = plannings.get(i);
			int approved = 0;
			int notApproved = 0;
			int pending = 0;

			if (plans != null) {
				for (MissionPlanning mp : plans) {
					if (PlanType.PENDING == mp.getStatus())
						pending++;
					else if (PlanType.NOT_APPROVED == mp.getStatus())
						notApproved++;
					else if (PlanType.APPROVED == mp.getStatus())
						approved++;
				}
			}

			if (totals) {
				totalApproved += approved;
				totalNotApproved += notApproved;
				totalPending += pending;
			}
			else {
				response.append("Stats for sol " + i + "\n");
				response.appendLabelledDigit("# of plans approved", approved);
				response.appendLabelledDigit("# of plans not approved", notApproved);
				response.appendLabelledDigit("# of plans pending", pending);
			}
		}
		
		if (totals) {
			response.appendLabelledDigit("# of plans approved", totalApproved);
			response.appendLabelledDigit("# of plans not approved", totalNotApproved);
			response.appendLabelledDigit("# of plans pending", totalPending);
		}
		
		context.println(response.getOutput());
		return true;
	}
}
