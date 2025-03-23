/*
 * Mars Simulation Project
 * AirlockCommand.java
 * @date 2024-08-10
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.settlement;

import java.util.List;
import java.util.stream.Collectors;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.structure.Airlock;
import com.mars_sim.core.structure.Settlement;

public class AirlockCommand extends AbstractSettlementCommand {

	public static final ChatCommand AIRLOCK = new AirlockCommand();
	
	private AirlockCommand() {
		super("ai", "airlocks", "Status of all airlocks");

	}

	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();
		
		BuildingManager bm = settlement.getBuildingManager();
		if ((input == null) || input.isEmpty()) {
			// Display summary for all airlocks
			List<Airlock> i = bm.getBuildings(FunctionType.EVA).stream()
									.map(b -> b.getEVA().getAirlock())
									.collect(Collectors.toList());
			CommandHelper.outputAirlock(response, i);
		}
		else {
			// Display details
			for(Building b : bm.getBuildings(FunctionType.EVA)) {
				if (b.getName().contains(input)) {
					CommandHelper.outputAirlockDetailed(response, b.getName(),
														b.getEVA().getAirlock());
					response.appendBlankLine();
				}
			}
		}
		context.println(response.getOutput());
		return true;
	}
}
