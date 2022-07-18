/*
 * Mars Simulation Project
 * TopLevel.java
 * @date 2022-07-17
 * @author Barry Evans
 */
package org.mars.sim.console.chat.simcommand;

import java.util.Arrays;
import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.command.ExpertCommand;
import org.mars.sim.console.chat.command.HelpCommand;
import org.mars.sim.console.chat.command.InteractiveChatCommand;

public class TopLevel extends InteractiveChatCommand {

	private static final String PREAMBLE = "Welcome to MarsLink !\n\nFor help, enter '"
									+ HelpCommand.HELP_LONG + "' at the prompt\n";
	private static final List<ChatCommand> COMMON_COMMANDS = Arrays.asList(ConnectCommand.CONNECT,
																	SettlementsCommand.SETTLEMENTS,
																	ExploredCommand.EXPLORED,
																	MissionPlanCommand.MISSION_PLAN,
																	MissionSummaryCommand.MISSION_SUMMARY,
																	DateCommand.DATE,
																	DistanceCommand.DISTANCE,
																	ElevationCommand.ELEVATION,
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
	private static final String PROMPT_SEED = "MarsLink";

	public TopLevel() {
		// Toplevel does not need a keyword or short command
		super(SIMULATION_GROUP, null, null, "Top level command", PROMPT_SEED, COMMON_COMMANDS);

		setIntroduction(PREAMBLE);
	}
	
	@Override
	public String getPrompt(Conversation context) {
		StringBuilder prompt = new StringBuilder();
		prompt.append(context.getSim().getMasterClock().getMarsClock().getTrucatedDateTimeStamp());
		prompt.append(' ').append(PROMPT_SEED);
		return prompt.toString();
	}
}
