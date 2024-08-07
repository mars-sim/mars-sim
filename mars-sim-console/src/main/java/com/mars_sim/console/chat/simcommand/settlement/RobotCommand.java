/**
 * Mars Simulation Project
 * RobotCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.settlement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;

/**
 * Command to display bed allocation in a Settlement
 * This is a singleton.
 */
public class RobotCommand extends AbstractSettlementCommand {

	public static final ChatCommand ROBOT = new RobotCommand();

	private RobotCommand() {
		super("rb", "robots", "Robots status");
	}

	/** 
	 * Output the details of the robots
	 * @return 
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {

		StructuredResponse response = new StructuredResponse();

		response.appendLabelledDigit("Robots #", settlement.getNumBots());
		response.appendBlankLine();
		Collection<Robot> list = settlement.getAllAssociatedRobots();
		List<Robot> namelist = new ArrayList<>(list);
		Collections.sort(namelist);
		
		response.appendTableHeading("Name", CommandHelper.BOT_WIDTH, "Activity");
		for (Robot robot : namelist) {
			response.appendTableRow(robot.getName(), robot.getTaskDescription());
		}
		
		context.println(response.getOutput());
		
		return true;
	}

}
