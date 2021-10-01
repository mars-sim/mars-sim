/**
 * Mars Simulation Project
 * MissionRadiusCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.settlement;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
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
		StructuredResponse status = new StructuredResponse();
		
		status.appendTableHeading("Id", 4, "Type of Mission", 24, "Mission Radius");
		for (MissionType  type : MissionType.values()) {
			status.appendTableRow("" + type.ordinal(), type.getName(), settlement.getMissionRadius(type));
		}
		context.println(status.getOutput());
		 
		int selected = context.getIntInput("Which one would you like to change ?");
		if ((selected < 1) || (selected > MissionType.values().length)) {
			context.println("Value not valid.");
		}
		else {
			selected--; // Index is zero based
			String rangeText = context.getInput("Enter the new mission radius in exact km");
			try {
				int newRange = Integer.parseInt(rangeText);
		
				MissionType choosen = MissionType.values()[selected];
				int oldRange = settlement.getMissionRadius(choosen);
				settlement.setMissionRadius(choosen, newRange);
				
				context.println("Old Mission Radius :  " + oldRange + " km");
				context.println("New Mission Radius :  " + newRange + " km");
			}
			catch (NumberFormatException e) {
				context.println("The radies is not a valid number");
			}	
		}
		
		return true;
	}
}
