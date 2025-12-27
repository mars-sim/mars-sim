/*
 * Mars Simulation Project
 * TopLevel.java
 * @date 2022-07-17
 * @author Barry Evans
 */
package com.mars_sim.console.chat.simcommand;

import java.util.Arrays;
import java.util.List;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.command.ExpertCommand;
import com.mars_sim.console.chat.command.HelpCommand;
import com.mars_sim.console.chat.command.InteractiveChatCommand;
import com.mars_sim.console.chat.simcommand.settlement.FutureEventCommand;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MasterClock;

public class TopLevel extends InteractiveChatCommand {

	private static final String PREAMBLE = "Welcome to MarsLink !\n\nFor help, enter '"
									+ HelpCommand.HELP_LONG + "' at the prompt\n";
	private static final List<ChatCommand> COMMON_COMMANDS = Arrays.asList(ConnectCommand.CONNECT,
																	SettlementsCommand.SETTLEMENTS,
																	MissionPlanCommand.MISSION_PLAN,
																	MissionSummaryCommand.MISSION_SUMMARY,
																	DateCommand.DATE,
																	DistanceCommand.DISTANCE,
																	ElevationCommand.ELEVATION,
																	SunlightCommand.SUNLIGHT,
																	ExpertCommand.EXPERT,
																	EventCommand.EVENT,
																	FutureEventCommand.FUTURE,
																	DiagnosticsCommand.DIAGNOSTICS,
																	
																	// Admin commands
																	new SaveCommand(),
																	new StopCommand(),
																	LogCommand.LOG,
																	new PauseCommand(),
																	new SpeedCommand());
	// The command group for Simulation commands
	public static final String SIMULATION_GROUP = "Simulation";
	private static final String PROMPT_SEED = "MarsLink AI";

	public TopLevel() {
		// Toplevel does not need a keyword or short command
		super(SIMULATION_GROUP, null, null, "Top level command", PROMPT_SEED, COMMON_COMMANDS);

		setIntroduction(PREAMBLE);
	}

	@Override
	public String getPrompt(Conversation context) {
		StringBuilder prompt = new StringBuilder();
		MasterClock clock = context.getSim().getMasterClock();
		MarsTime marsClock = clock.getMarsTime();
		prompt.append("[" + clock.getMarsTime().getTruncatedDateTimeStamp());
		prompt.append(" Sol " + marsClock.getMissionSol());
		prompt.append("] ").append(PROMPT_SEED);
		return prompt.toString();
	}
}
