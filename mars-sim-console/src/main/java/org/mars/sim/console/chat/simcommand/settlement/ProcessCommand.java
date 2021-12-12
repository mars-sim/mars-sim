/**
 * Mars Simulation Project
 * ProcessCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.settlement;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MarsClockFormat;

/**
 * Shows the status of ResourceProcessing in a Settlement
 *
 */
public class ProcessCommand extends AbstractSettlementCommand {

	private static final String DUE_FORMAT = "In %d:" + MarsClockFormat.TRUNCATED_TIME_FORMAT;
	private static final String TOGGLING_FORMAT = "Toggle %.1f/%.1f";
	public static final ChatCommand PROCESS = new ProcessCommand();

	private ProcessCommand() {
		super("pr", "processes", "Settlement Resource processing");
	}
	
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();
		response.appendTableHeading("Building", CommandHelper.BUILIDNG_WIDTH, "Process", CommandHelper.BUILIDNG_WIDTH,
									"Active", "Level", "Toggle @ sol:msol");

		MarsClock clock = context.getSim().getMasterClock().getMarsClock();
		int missionMilliSol = (clock.getMissionSol() * 1000) + clock.getMillisolInt();
		
		// Loop building that do processing
		for(Building building : settlement.getBuildingManager().getBuildings(FunctionType.RESOURCE_PROCESSING)) {
			String bName = building.getNickName();
			ResourceProcessing processor = building.getResourceProcessing();
		
			for(ResourceProcess p : processor.getProcesses()) {
				int[] remainingTime = p.getTimeLimit();
				int remainingMilliSol = ((remainingTime[0] * 1000) + remainingTime[1]) - missionMilliSol;

				String nextToggle = null;
				if (remainingMilliSol <= 0) {
					// Toggling is active
					double[] toggleTime = p.getToggleSwitchDuration();
					nextToggle = String.format(TOGGLING_FORMAT, toggleTime[0], toggleTime[1]);
				}
				else {
					int remainingSol = (remainingMilliSol/1000);
					remainingMilliSol = remainingMilliSol % 1000;
					nextToggle = String.format(DUE_FORMAT, remainingSol, remainingMilliSol);
				}

				
				response.appendTableRow(bName, p.getProcessName(), p.isProcessRunning(),
										p.getCurrentProductionLevel(), nextToggle);
			}
		}

		context.println(response.getOutput());
		return true;
	}

}
