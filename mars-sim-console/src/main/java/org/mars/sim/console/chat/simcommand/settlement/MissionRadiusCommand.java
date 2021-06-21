/**
 * Mars Simulation Project
 * MissionRadiusCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.settlement;

import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Command to display bed allocation in a Settlement
 * This is a singleton.
 */
public class MissionRadiusCommand extends AbstractSettlementCommand {

	public static final ChatCommand RADIUS = new MissionRadiusCommand();

	private MissionRadiusCommand() {
		super("mr", "mission radius", "Maximum radiius of Missions from this Settlement");
		
		setInteractive(true);
	}

	/** 
	 * Output the current immediate location of the Unit
	 * @return 
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		List<String> missionNames = Settlement.getTravelMissionNames();

		StructuredResponse status = new StructuredResponse();
		
		int i = 0;
		status.appendTableHeading("Id", 4, "Type of Mission", 24, "Mission Radius");
		for (String string : missionNames) {
			status.appendTableRow("" + (i + 1), string, settlement.getMissionRadius(i++));
		}
		context.println(status.getOutput());
		
		int selected = context.getIntInput("Which one would you like to change ?");
		if ((selected < 1) || (selected > missionNames.size())) {
			context.println("Value not valid.");
		}
		else {
			selected--; // Index is zero based
			String rangeText = context.getInput("Enter the new mission radius (a number between 50.0 and 2200.0 [in km])");
			double newRange = 0;
			try {
				newRange = Double.parseDouble(rangeText);
				newRange = Math.round(newRange*10.0)/10.0;
		
				double oldRange = Math.round(settlement.getMissionRadius(selected)*10.0)/10.0;
		
				if (newRange >= 50.0 && newRange <= 2200.0) {
					settlement.setMissionRadius(selected, newRange);
					//settlement.setMaxMssionRange(newRange);
					
					context.println("Old Mission Radius :  " + oldRange + " km");
					context.println("New Mission Radius :  " + newRange + " km");
				}
				else {
					context.println("Radius has to be between 50.0 & 2200.0");
				}
			}
			catch (NumberFormatException e) {
				context.println("The radies is not a valid number");
			}	
		}
		
		return true;
	}
}
