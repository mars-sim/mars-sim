/**
 * Mars Simulation Project
 * TopLevel.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */
package org.mars.sim.console.chat.simcommand;

import java.util.Arrays;
import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.command.ExpertCommand;
import org.mars.sim.console.chat.command.HelpCommand;
import org.mars.sim.console.chat.command.InteractiveChatCommand;

public class TopLevel extends InteractiveChatCommand {

	private static final String PREAMBLE = "Welcome to MarsNet!\n\nFor help, enter '"
									+ HelpCommand.HELP_LONG + "' at the prompt\n";
	private static final List<ChatCommand> COMMON_COMMANDS = Arrays.asList(ConnectCommand.CONNECT,
																	SettlementsCommand.SETTLEMENTS,
																	MissionPlanCommand.MISSION_PLAN,
																	MissionSummaryCommand.MISSION_SUMMARY,
																	DateCommand.DATE,
																	DistanceCommand.DISTANCE,
																	ExpertCommand.EXPERT,
																	EventCommand.EVENT,
																	DiagnosticsCommand.DIAGNOSTICS,
																	
																	// Admin commands
																	new SaveCommand(),
																	new StopCommand(),
																	LogCommand.LOG,
																	new PauseCommand(),
																	new SpeedCommand());
	// The command group for Simulation commands
	public static final String SIMULATION_GROUP = "Simulation";

	public TopLevel() {
		// Toplevel does not need a keyword or short command
		super(SIMULATION_GROUP, null, null, "Top level command", "MarsNet", COMMON_COMMANDS);

		setIntroduction(PREAMBLE);
	}
}
