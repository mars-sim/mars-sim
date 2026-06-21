/*
 * Mars Simulation Project
 * MissionPlanCommand.java
 * @date 2022-06-26
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.settlement;

import java.util.HashMap;
import java.util.Map;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.structure.Settlement;

/**
 * Command to display mission stats
 * This is a singleton.
 */
public class MissionPlanCommand extends AbstractSettlementCommand {

	public static final ChatCommand MISSION_PLAN = new MissionPlanCommand();

	private MissionPlanCommand() {
		super("mp", "planning", "Planned mission counts");
		
		setInteractive(true);
	}

	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
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
		Map<String, Double> totals = null;
		int today = context.getSim().getMasterClock().getMarsTime().getMissionSol();

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
			totals = new HashMap<>();
			max = 3;
			break;
			
		case 5:
			max = 7;
			totals = new HashMap<>();
			break;
			
		case 6:
			max = 14;
			totals = new HashMap<>();
			break;
			
		case 7:
			max = 28;
			totals = new HashMap<>();
			break;
			
		case 8:
			max = today;
			totals = new HashMap<>();
			break;
			
		default:
			context.println("Please enter a valid input.");
			return false;
		}	

		StructuredResponse response = new StructuredResponse();

		response.appendHeading("On Sol " + today + ", the data for the last N sols");

		Map<Integer, Map<String, Double>> plannings = settlement.getMissionControl().getHistoricalMissions();

		int firstSol = today;
		int lastSol = Math.max(0, today - max); // Don't go negative
		for (int i = firstSol; i > lastSol; i--) {
			var sol = plannings.get(i);
			if (sol != null) {
				response.appendText("Stats for sol " + i);
				outputStats(response, sol);
				if (totals != null) {
					for(var p : sol.entrySet()) {
						totals.merge(p.getKey(), p.getValue(), Double::sum);
					}
				}
			}
			else {
				response.appendText("No data for sol " + i);
			}
		}
		
		if (totals != null) {
			response.appendText("Stats for total:");
			outputStats(response, totals);
		}
		
		context.println(response.getOutput());
		return true;
	}

	private static void outputStats(StructuredResponse response, Map<String, Double> stats) {
		stats.entrySet().forEach(p -> response.appendLabelledDigit(p.getKey(), p.getValue().intValue()));
	}
}
