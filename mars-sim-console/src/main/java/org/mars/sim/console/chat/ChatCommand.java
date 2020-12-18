package org.mars.sim.console.chat;

import java.util.Collections;
import java.util.List;

public abstract class ChatCommand implements Comparable<ChatCommand> {

	public static final String COMMAND_GROUP = "Common";
	
	private String shortCommand;
	private String longCommand;
	private String commandGroup;
	private String description;

	public ChatCommand(String commandGroup, String shortCommand, String longCommand, String description) {
		super();
		this.commandGroup = commandGroup;
		this.shortCommand = shortCommand;
		this.longCommand = longCommand;
		this.description = description;
	}

	/**
	 * This processes input from a user.
	 * @param context
	 * @param input 
	 * @return Has the input been accepted
	 */
	public abstract void execute(Conversation context, String input);

	/**
	 * What is the short command?
	 * @return
	 */
	public String getShortCommand() {
		return shortCommand;
	}
	
	/**
	 * The keyword that triggers the execution of this command
	 * @return
	 */
	public String getLongCommand() {
		return longCommand;
	}

	public String getDescription() {
		return description;
	}
	
	public String getCommandGroup() {
		return commandGroup;
	}

	/**
	 * Sort on keyword.
	 */
	@Override
	public int compareTo(ChatCommand other) {
		return longCommand.compareTo(other.getLongCommand());
	}

	@Override
	public String toString() {
		return "ChatCommand [keyword=" + longCommand + "]";
	}

	/**
	 * Get the list of options that match the partial input. 
	 * @param partialInput
	 * @return List of potential full commands or maybe null.
	 */
	public List<String> getAutoComplete(Conversation context, String parameter) {
		return Collections.emptyList();
	}
}