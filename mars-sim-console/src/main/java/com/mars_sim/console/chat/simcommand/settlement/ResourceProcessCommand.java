/*
 * Mars Simulation Project
 * ResourceProcessCommand.java
 * @date 2022-06-15
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.settlement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.FunctionType;

/**
 * Shows the status of ResourceProcessing in a Settlement
 *
 */
public class ResourceProcessCommand extends AbstractSettlementCommand {

	public static final ChatCommand PROCESS = new ResourceProcessCommand();

	private ResourceProcessCommand() {
		super("rp", "processes", "Settlement Resource Processing");
	}
	
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();
		
		// Loop buildings that do processing
		List<Building> processors = new ArrayList<>(
					settlement.getBuildingManager().getBuildings(FunctionType.RESOURCE_PROCESSING));
		Collections.sort(processors);
		for(Building building : processors) {
			CommandHelper.outputProcesses(response, "Resource process", building.getName(),
							building.getResourceProcessing());
		}
		
		context.println(response.getOutput());
		return true;
	}


}
