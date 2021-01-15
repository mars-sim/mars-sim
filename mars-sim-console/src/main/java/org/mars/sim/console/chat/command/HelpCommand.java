package org.mars.sim.console.chat.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.command.InteractiveChatCommand.ParseResult;

/**
 * Generic help command that will list the commands that are supported.
 */
public class HelpCommand extends ChatCommand {

	private static final String CMD =  " * "  + InteractiveChatCommand.SHORT_PREFIX + "%-2s";
	private static final String FULL = CMD + " %s - %s";
	private static final String SHORT_ONE = CMD + " %s";
	private static final String SHORT_TWO = CMD + " %-16s " + CMD + " %s";
	
	// The long command 
	public static final String HELP_LONG = "help";
	private boolean wideFormat = true;

	public HelpCommand(boolean wide) {
		super(COMMAND_GROUP, "?", HELP_LONG, "Lists the available commands");
		
		wideFormat = wide;
	}

	@Override
	public List<String> getArguments(Conversation context) {
		return context.getCurrentCommand().getSubCommands().stream()
				.map(c -> c.getLongCommand()).collect(Collectors.toList());
	}
	
	@Override
	public boolean execute(Conversation context, String input) {
		if ((input != null) && !input.isBlank()) {
			ParseResult found = context.getCurrentCommand().parseInput(input);
			if (found.command != null) {
				context.println("Format: * <short command> <long command> - <description>");
				outputFullHelp(found.command, context);
				return true;
			}
			else {
				context.println("Don't know command " + input);
			}
		}
		
		context.println("These are the control keys I understand:");
		context.println(String.format(" * %s - Auto completes the input; for commands (long & short) and arguments",
									  Conversation.AUTO_COMPLETE_KEY)); 
		context.println(String.format(" * %s - Goes back in the history", Conversation.HISTORY_BACK_KEY)); 
		context.println(String.format(" * %s - Goes forward in the history", Conversation.HISTORY_FORWARD_KEY)); 

		context.println("");
		context.println("Console commands (* <short command> <long command>)");
		InteractiveChatCommand current = context.getCurrentCommand();
		Map<String, List<ChatCommand>> commandGroups = current.getSubCommands().stream()
                .collect(Collectors.groupingBy(ChatCommand::getCommandGroup));

		List<String> groupNames = new ArrayList<>(commandGroups.keySet());
		Collections.sort(groupNames);
		
		// Display according to group
		for (String group : groupNames) {
			context.println("Group : " + group);
			List<ChatCommand> subs = commandGroups.get(group);
			Collections.sort(subs);
		
			if (wideFormat) {
				// Two column and short format
				int half = (subs.size()+1)/2;
				for(int i = 0; i < half; i++) {
					ChatCommand second = null;
					int j = i + half;
					if (j < subs.size()) {
						second = subs.get(j);
					}
					outputShortHelp(subs.get(i), second, context);
				}
			}
			else {
				for (ChatCommand chatCommand : subs) {
					outputFullHelp(chatCommand, context);
				}
			}
			context.println("");
		}
		if (wideFormat ) {
			context.println("More detailed help by using > " + HELP_LONG + " <long command>");
		}
		return true;
	}
	
	private void outputShortHelp(ChatCommand first, ChatCommand second, Conversation context) {
		if (second != null) {
			context.println(String.format(SHORT_TWO, 
					first.getShortCommand(), first.getLongCommand(),
					second.getShortCommand(), second.getLongCommand()));		
		}
		else {
			context.println(String.format(SHORT_ONE,
					first.getShortCommand(), first.getLongCommand()));			
		}
	}

	private void outputFullHelp(ChatCommand chatCommand, Conversation context) {
		context.println(String.format(FULL,
				chatCommand.getShortCommand(),
				chatCommand.getLongCommand(), chatCommand.getDescription()));
	}
}
