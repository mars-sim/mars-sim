package org.mars.sim.console.chat.command;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.CancellableCommand;
import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.command.InteractiveChatCommand.ParseResult;

/**
 * A command to repeate other commands periodically. 
 * This command is NOT stateless.
 */
public class RepeatCommand extends ChatCommand implements CancellableCommand {

	private static final int MIN_DELAY = 1;
	private static final int MAX_DELAY = 60;

	private boolean stopRun;

	public RepeatCommand() {
		super(COMMAND_GROUP, "rp", "repeat", "Repeatedly call a command; > repeat [delay sec] [repeat] {command}");
		setInteractive(true);
	}

	@Override
	public boolean execute(Conversation context, String input) {
		InteractiveChatCommand parent = context.getCurrentCommand();
		
		// Extract seconds delay
		String[] parts = input.split(" ", 3);
		int delaySec = Integer.parseInt(parts[0]);
		if ((delaySec < MIN_DELAY) || (delaySec > MAX_DELAY)) {
			context.println("Delay must be between " + MIN_DELAY + " and " + MAX_DELAY + " secs");
			return false;
		}
		int repeatCount = Integer.parseInt(parts[1]);
		
		// Check the command is not interactive
		String commandStr = parts[2].trim();
		ParseResult parsedCommand = parent.parseInput(commandStr);
		if (parsedCommand.command == null) {
			context.println("Can not understand " + commandStr);
			return false;
		}
		else if (parsedCommand.command.isInteractive()) {
			context.println("Called command cannot be interactive");
			return false;
		}
		
		// Execute the requested command first time including the description
		context.println("Going to execute '" + commandStr + "' every " + delaySec + " secs");
		context.println("To stop press " + Conversation.CANCEL_KEY);
		
		
		boolean result = parsedCommand.command.execute(context, parsedCommand.parameter);
		int count = 1;
		context.setActiveCommand(this);
		stopRun = false;
		
		while (result && !stopRun && (count != repeatCount)) {
			context.println("Waiting..........");
			try {
				Thread.sleep(delaySec * 1000L);
			} catch (InterruptedException e) {
				context.println("Abort repeat");
				Thread.currentThread().interrupt();
			}
			
			if (!stopRun) {
				result = parsedCommand.command.execute(context, parsedCommand.parameter);
				count++;
			}
		}
		context.setActiveCommand(null);
		
		return result;
	}

	/**
	 * This delegates to the parent Interactive Command
	 */
	@Override
	public List<String> getAutoComplete(Conversation context, String parameter) {
		InteractiveChatCommand parent = context.getCurrentCommand();

		// Parameter should be in the format <delay> <repeat> <command>
		String[] parts = parameter.split(" ", 3);
		List<String> commandOptions;
		if (parts.length == 3) {
			commandOptions = parent.getAutoComplete(context, parts[2]);
		}
		else {
			commandOptions = new ArrayList<>();
		}
		
		StringBuilder prefix = new StringBuilder();
		prefix.append(parts[0]);
		prefix.append(" ");
		prefix.append(parts[1]);
		prefix.append(' ');
		
		return commandOptions.stream().map(o -> prefix + o ).collect(Collectors.toList());
	}

	@Override
	public void cancel() {
		stopRun = true;
	}
}
