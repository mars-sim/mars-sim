/*
 * Mars Simulation Project
 * ResourceProcessCommand.java
 * @date 2022-06-15
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.settlement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.time.MarsTime;

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


		MarsTime clock = context.getSim().getMasterClock().getMarsTime();
		int missionMilliSol = (clock.getMissionSol() * 1000) + clock.getMillisolInt();
		
		// Loop buildings that do processing
		List<Building> processors = new ArrayList<>(
					settlement.getBuildingManager().getBuildings(FunctionType.RESOURCE_PROCESSING));
		Collections.sort(processors);
		for(Building building : processors) {
			CommandHelper.outputProcesses(response, "Resource process", missionMilliSol, building.getName(),
							building.getResourceProcessing());
		}
		
		context.println(response.getOutput());
		return true;
	}


}
