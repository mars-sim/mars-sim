/*
 * Mars Simulation Project
 * MissionCommand.java
 * @date 2022-06-11
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.settlement;

import java.util.List;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.meta.MetaMission;
import org.mars_sim.msp.core.person.ai.mission.meta.MetaMissionUtil;
import org.mars_sim.msp.core.structure.Settlement;

public class MissionCreateCommand extends AbstractSettlementCommand {
	public static final ChatCommand MISSION = new MissionCreateCommand();

	private MissionCreateCommand() {
		super("cm", "create mission", "Create a Mission");
	}

	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {

		// Get the user to select the Mission
		List<MetaMission> missions = MetaMissionUtil.getMetaMissions().stream()
						.filter(m -> settlement.isMissionEnable(m.getType()))
						.collect(Collectors.toList());
		List<String> names = missions.stream().map(MetaMission::getName).collect(Collectors.toList());				
		int choice = CommandHelper.getOptionInput(context, names, "Pick a mission from above by entering a number");
		if (choice < 0) {
			return false;
		}
		MetaMission choosen = missions.get(choice);

		// Select leader
		List<Person> leaders = settlement.getAllAssociatedPeople().stream()
								.filter(p -> p.getMission() == null && p.isInSettlement())
								.collect(Collectors.toList());
		
		// Create name and the suitability
		List<String> pNames = leaders.stream()
								.map(p -> p.getName() + ", suitability "
										+ (choosen.getLeaderSuitability(p) > 0.5D ? "high" : "low"))
								.collect(Collectors.toList());
		int leaderNum = CommandHelper.getOptionInput(context, pNames, "Pick a leader from above by entering a number");
		if (leaderNum < 0) {
			return false;
		}
		Person leader = leaders.get(leaderNum);

		// Confirmation
		context.println("Create a " + choosen.getName() + " Mission with leader " + leader.getName());
		String confirmation = context.getInput("Y/N");
		if ("Y".equals(confirmation)) {
			// Create without a review
			Mission newMission = choosen.constructInstance(leader, false);
			if (newMission.isDone()) {
				context.println("Mission failed to start " + newMission.getMissionStatus());
			}
			else {
				context.println("Create Mission " + newMission.getTypeID());
				context.getSim().getMissionManager().addMission(newMission);
			}
		}

		return true;
	}

}
