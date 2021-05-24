package org.mars.sim.console.chat.simcommand.robot;

import java.util.Arrays;
import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.command.InteractiveChatCommand;
import org.mars.sim.console.chat.simcommand.ConnectedUnitCommand;
import org.mars.sim.console.chat.simcommand.unit.MalfunctionCreateCommand;
import org.mars.sim.console.chat.simcommand.unit.MissionCommand;
import org.mars.sim.console.chat.simcommand.unit.UnitLocationCommand;
import org.mars.sim.console.chat.simcommand.unit.UnitMalfunctionCommand;
import org.mars.sim.console.chat.simcommand.unit.WorkerActivityCommand;
import org.mars.sim.console.chat.simcommand.unit.WorkerAttributeCommand;
import org.mars.sim.console.chat.simcommand.unit.WorkerSkillsCommand;
import org.mars.sim.console.chat.simcommand.unit.WorkerTaskCommand;
import org.mars.sim.console.chat.simcommand.unit.WorkerWorkCommand;
import org.mars_sim.msp.core.robot.Robot;

/**
 * Represents a connection to a Robot.
 */
public class RobotChat extends ConnectedUnitCommand {
	public static final String ROBOT_GROUP = "Robot";

	private static final List<ChatCommand> COMMANDS = Arrays.asList(new WorkerAttributeCommand(ROBOT_GROUP),
																	new MissionCommand(ROBOT_GROUP),
																	new WorkerSkillsCommand(ROBOT_GROUP),
																	new UnitMalfunctionCommand(ROBOT_GROUP),
																	new MalfunctionCreateCommand(ROBOT_GROUP),
																	new UnitLocationCommand(ROBOT_GROUP),
																    new WorkerActivityCommand(ROBOT_GROUP),
																    new WorkerTaskCommand(ROBOT_GROUP),
																    new WorkerWorkCommand(ROBOT_GROUP)
																		);

	private Robot robot;

	public RobotChat(Robot robot, InteractiveChatCommand parent) {
		super(robot, COMMANDS, parent);
		this.robot = robot;
	}

	@Override
	public String getIntroduction() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Hi, I am a ");
		buffer.append(robot.getRobotType());
		buffer.append(" called ");
		buffer.append(robot.getName());
		buffer.append(" based in ");
		buffer.append(robot.getSettlement().getName());
		
		return buffer.toString();
	}
}
