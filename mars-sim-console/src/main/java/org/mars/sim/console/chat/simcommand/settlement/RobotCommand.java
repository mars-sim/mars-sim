package org.mars.sim.console.chat.simcommand.settlement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Command to display bed allocation in a Settlement
 * This is a singleton.
 */
public class RobotCommand extends ChatCommand {

	public static final ChatCommand ROBOT = new RobotCommand();

	private RobotCommand() {
		super(SettlementChat.SETTLEMENT_GROUP, "r", "robots", "Robots status");
	}

	/** 
	 * Output the details of the robots
	 */
	@Override
	public void execute(Conversation context, String input) {
		SettlementChat parent = (SettlementChat) context.getCurrentCommand();		
		Settlement settlement = parent.getSettlement();
		StructuredResponse response = new StructuredResponse();

		response.appendLabelledDigit("Robots #", settlement.getNumBots());
		response.append("\n");
		Collection<Robot> list = settlement.getRobots();
		List<Robot> namelist = new ArrayList<>(list);
		Collections.sort(namelist);
		
		response.appendTableHeading("Name", "Activity");
		for (Robot robot : namelist) {
			response.appendTableString(robot.getName(), robot.getTaskDescription());
		}
		
		context.println(response.getOutput());
	}

}
