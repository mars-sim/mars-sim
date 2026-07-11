/*
 * Mars Simulation Project
 * MissionCommand.java
 * @date 2022-06-11
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.settlement;

import java.util.Comparator;
import java.util.List;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.core.mission.MetaMission;
import com.mars_sim.core.mission.MetaMissionRegistry;
import com.mars_sim.core.mission.MissionBuilder;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.PlanType;
import com.mars_sim.core.structure.Settlement;

public class MissionCreateCommand extends AbstractSettlementCommand {
	public static final ChatCommand MISSION = new MissionCreateCommand();

	private MissionCreateCommand() {
		super("cm", "create mission", "Create a Mission");
	}

	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {

		// Get the user to select the Mission
		List<MetaMission> automissions = MetaMissionRegistry.getAutomaticMetaMissions();

		List<String> names = automissions.stream().map(MetaMission::getName).toList();				
		int choice = CommandHelper.getOptionInput(context, names, "Pick a mission from above by entering a number");
		if (choice < 0) {
			return false;
		}
		var choosen = automissions.get(choice);

		// Select leader
		var leader = selectLeader(choosen, settlement, context);
		if (leader == null) {
			context.println("No leader selected");
			return false;
		}
		int reviewChoice = CommandHelper.getYesNoInput(context, "Request a review");
		if (reviewChoice != CommandHelper.CANCEL) {
			// Create mission
			var doReview = (reviewChoice == CommandHelper.YES);

			var builder = new MissionBuilder(choosen, leader);
			Mission newMission = builder.buildMission(doReview);
			if (newMission == null) {
				context.println("Mission failed to start:");
				builder.getMessages().forEach(m -> context.println(m.getMessage()));
			}
			else if (newMission.isDone()) {
				context.println("Mission failed to start " + newMission.getMissionStatus());
			}
			else {
				context.println("Create Mission " + newMission.getName());
				settlement.getMissionControl().addMission(newMission);

				if (doReview) {
					var plan = newMission.getPlan();
					if (plan == null) {
						context.println("Mission does not have a plan to review");
					}
					else {
						// Must still skip Preparing state
						context.println("Mission plan requires review");
						plan.setStatus(PlanType.PENDING);
					}
				}
			}
		}

		return true;
	}

	private record LeaderScore(Person person, double score) {}

	private Person selectLeader(MetaMission choosen, Settlement settlement, Conversation context	) {
		List<LeaderScore> leaders = settlement.getAllAssociatedPeople().stream()
								.filter(p -> p.getMission() == null && p.isInSettlement())
								.map(p -> new LeaderScore(p, choosen.getLeaderSuitability(p)))
								.filter(v -> v.score() > 0)
								.sorted(Comparator.comparingDouble(LeaderScore::score))
								.toList();

		List<String> pNames = leaders.stream()
								.map(s -> s.person().getName() + " (Score: " + String.format("%.2f", s.score()) + ")")
								.toList();
		int leaderNum = CommandHelper.getOptionInput(context, pNames, "Pick a leader from above by entering a number");
		if (leaderNum < 0) {
			return null;
		}
		return leaders.get(leaderNum).person();
	}
}
