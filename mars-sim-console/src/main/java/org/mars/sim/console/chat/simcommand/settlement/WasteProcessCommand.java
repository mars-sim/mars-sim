/**
 * Mars Simulation Project
 * WasteProcessCommand.java
 * @date 2022-06-15
 * @author Manny Kung
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
import org.mars_sim.msp.core.structure.building.function.WasteProcess;
import org.mars_sim.msp.core.structure.building.function.WasteProcessing;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MarsClockFormat;

/**
 * Shows the status of WasteProcessing in a Settlement
 *
 */
public class WasteProcessCommand extends AbstractSettlementCommand {

	private static final String DUE_FORMAT = "In %d:" + MarsClockFormat.TRUNCATED_TIME_FORMAT;
	private static final String TOGGLING_FORMAT = "Toggle %.1f/%.1f";
	public static final ChatCommand WASTE = new WasteProcessCommand();

	private WasteProcessCommand() {
		super("wp", "wastes", "Settlement Waste Processing");
	}
	
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();
		response.appendTableHeading("Building", CommandHelper.BUILIDNG_WIDTH, "Waste Process", CommandHelper.PROCESS_WIDTH,
									"Active", "Level", "Toggle @ sol:msol");

		MarsClock clock = context.getSim().getMasterClock().getMarsClock();
		int missionMilliSol = (clock.getMissionSol() * 1000) + clock.getMillisolInt();
		
		// Loop buildings that do processing
		for(Building building : settlement.getBuildingManager().getBuildings(FunctionType.WASTE_PROCESSING)) {
			String bName = building.getName();
			WasteProcessing processor = building.getWasteProcessing();
		
			for(WasteProcess p : processor.getProcesses()) {
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
