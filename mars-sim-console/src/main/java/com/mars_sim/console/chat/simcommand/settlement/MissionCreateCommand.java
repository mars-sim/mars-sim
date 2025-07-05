/*
 * Mars Simulation Project
 * MissionCommand.java
 * @date 2022-06-11
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.settlement;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.core.mission.predefined.TestDriveMetaMission;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.meta.MetaMission;
import com.mars_sim.core.person.ai.mission.meta.MetaMissionUtil;
import com.mars_sim.core.structure.Settlement;

public class MissionCreateCommand extends AbstractSettlementCommand {
	public static final ChatCommand MISSION = new MissionCreateCommand();

	private MissionCreateCommand() {
		super("cm", "create mission", "Create a Mission");
	}

	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {

		// Get the user to select the Mission
		List<MetaMission> automissions = MetaMissionUtil.getMetaMissions().stream()
						.filter(m -> settlement.isMissionEnable(m.getType()))
						.toList();
		
		// Add the none auto missions
		List<MetaMission> missions = new ArrayList<>(automissions);
		missions.add(new TestDriveMetaMission());

		List<String> names = missions.stream().map(MetaMission::getName).toList();				
		int choice = CommandHelper.getOptionInput(context, names, "Pick a mission from above by entering a number");
		if (choice < 0) {
			return false;
		}
		MetaMission choosen = missions.get(choice);

		// Select leader
		List<Person> leaders = settlement.getAllAssociatedPeople().stream()
								.filter(p -> p.getMission() == null && p.isInSettlement())
								.toList();
		
		// Create name and the suitability
		List<String> pNames = leaders.stream()
								.map(p -> p.getName() + ", suitability "
										+ (choosen.getLeaderSuitability(p) > 0.5D ? "high" : "low"))
								.toList();
		int leaderNum = CommandHelper.getOptionInput(context, pNames, "Pick a leader from above by entering a number");
		if (leaderNum < 0) {
			return false;
		}
		Person leader = leaders.get(leaderNum);

		// Confirmation
		if (context.getBooleanInput("Create a " + choosen.getName() + " Mission with leader " + leader.getName())) {
			// Create without a review
			Mission newMission = choosen.constructInstance(leader, false);
			if (newMission.isDone()) {
				context.println("Mission failed to start " + newMission.getMissionStatus());
			}
			else {
				context.println("Create Mission " + newMission.getName());
				context.getSim().getMissionManager().addMission(newMission);
			}
		}

		return true;
	}

}
