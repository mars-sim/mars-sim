package org.mars.sim.console.chat.command;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.sql.CommonDataSource;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;

/**
 * Generic help command that will list the commands that are supported.
 */
public class HelpCommand extends ChatCommand {

	// The long command 
	public static final String HELP_LONG = "help";

	public HelpCommand() {
		super(COMMAND_GROUP, "?", HELP_LONG, "Lists the available commands");
	}

	@Override
	public void execute(Conversation context, String input) {
		context.println("These are the control keys I understand:");
		context.println(String.format(" * %s - Auto completes the command", Conversation.AUTO_COMPLETE_KEY)); 
		context.println(String.format(" * %s - Goes back in the history", Conversation.HISTORY_BACK_KEY)); 
		context.println(String.format(" * %s - Goes forward in the history", Conversation.HISTORY_FORWARD_KEY)); 

		context.println("");
		context.println("Here are the commands I understand:");
		InteractiveChatCommand current = context.getCurrentCommand();
		Map<String, List<ChatCommand>> commandGroups = current.getSubCommands().stream()
                .collect(Collectors.groupingBy(ChatCommand::getCommandGroup));

		// Display according to group
		for (Entry<String, List<ChatCommand>> group : commandGroups.entrySet()) {
			context.println("Group : " + group.getKey());
			List<ChatCommand> subs = group.getValue();
			Collections.sort(subs);
		
			for (ChatCommand chatCommand : subs) {
				context.println(String.format(" * %s%s %s - %s", InteractiveChatCommand.SHORT_PREFIX,
											chatCommand.getShortCommand(),
											chatCommand.getLongCommand(), chatCommand.getDescription()));
			}
			context.println("");
		}

	}
}
