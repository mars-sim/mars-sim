/*
 * Mars Simulation Project
 * LogCommand.java
 * @date 2021-09-05
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.ConversationRole;

public class LogCommand extends ChatCommand {
	private static final String ALL_CHOICE = "All";
	public final static ChatCommand LOG = new LogCommand();
	
	private LogCommand() {
		super(TopLevel.SIMULATION_GROUP, "lo", "log", "Change the simulation logging");
		setInteractive(true);
		addRequiredRole(ConversationRole.ADMIN);
	}

	@Override
	public boolean execute(Conversation context, String input) {
		String loggerPattern = null;
		List<String> selected = null;

		String level = null;
		if ((input != null) && !input.isEmpty()) {
			String[] args = input.split(" ");
			if (args.length != 2) {
				context.println("Arguments must is a <logger name> <level>");
				return false;
			}
			selected = new ArrayList<>();
			selected.add(args[0]);
			level = args[1];
		}
		else {
			// Get required pattern
			loggerPattern = context.getInput("What is the logger name (partial name supported)");
			
			// Find any matching loggers
			List<String> matches = new ArrayList<>();
			Enumeration<String> loggers =  LogManager.getLogManager().getLoggerNames();
			while (loggers.hasMoreElements()) {
				String logger = loggers.nextElement();
				if (logger.contains(loggerPattern)) {
					matches.add(logger);
				}
			}
			
			// Add All as the last choice
			matches.add(ALL_CHOICE);
			int choice = CommandHelper.getOptionInput(context, matches, "Choose 1 logger or all");
			if (choice < 0) {
				context.println("Cancelled");
				return false;
			}
			if (ALL_CHOICE.equals(matches.get(choice))) {
				selected = matches.subList(0, matches.size()-1);
			}
			else {
				selected = new ArrayList<>();
				selected.add(matches.get(choice));
			}

			level = context.getInput("What is the new level?");

		}

		
		applyLevel(context, selected, level);
		return true;
	}

	private void applyLevel(Conversation context, List<String> logNames, String level) {
		Level newLevel = Level.parse(level.toUpperCase());

		for (String loggerName : logNames) {
			Logger logger = Logger.getLogger(loggerName);
			logger.setLevel(newLevel);
			
			context.println("Setting level " + newLevel + " to " + logger.getName());
		}
	}
}
