package org.mars.sim.console.chat;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ChatCommand implements Comparable<ChatCommand> {

	public static final String COMMAND_GROUP = "Common";

	//Width of a Person value
	public static final int PERSON_WIDTH = 22;

	// Width of a Job vlaue
	public static final int JOB_WIDTH = 16;

	// Width of a Role value
	public static final int ROLE_WIDTH = 33;

	// Width of a Task value
	public static final int TASK_WIDTH = 30;

	// Width of a Bot name
	public static final int BOT_WIDTH = 19;
	
	private String shortCommand;
	private String longCommand;
	private String commandGroup;
	private String description;
	private String introduction = null;
	private List<String> arguments = null;

	private boolean interactive = false;

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
	 * @return Has the input been accepted and understood
	 */
	public abstract boolean execute(Conversation context, String input);

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

	/**
	 * A description abouve the command; used for help
	 * @return
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * The command group this is held in.
	 * @return
	 */
	public String getCommandGroup() {
		return commandGroup;
	}

	/**
	 * Return any introduction to executing this command when working as interactive.
	 * Assume that the contents may be dynamic as a subclass may override this method.
	 * @return
	 */
	public String getIntroduction() {
		return introduction ;
	}

	/**
	 * Set a default introduction.
	 * @param introduction
	 */
	protected void setIntroduction(String introduction) {
		this.introduction = introduction;
	}

	/**
	 * Any fix arguments for the command. These are used in the AutoComplete operation.
	 * This should be Overridden by subclasses if they what to return a dynamic list of options. 
	 * A static list of optinos can be specified in {@link #setArguments(List)}
	 * @param context Current context
	 * @return
	 */
	protected List<String> getArguments(Conversation context) {
		return arguments;
	}

	/**
	 * Set and fixed arguments to this command. Dynamic arguments the {@link #getAutoComplete(Conversation, String)}
	 * method should be overriden.
	 * @param arguments
	 */
	protected void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}

	/**
	 * Get the list of options that match the partial input. 
	 * @param partialInput
	 * @return List of potential full commands or maybe null.
	 */
	public List<String> getAutoComplete(Conversation context, String parameter) {
		List<String> result;
		List<String> activeArgs = getArguments(context);
		if (activeArgs != null) {
			String match = parameter.toLowerCase();
			result = activeArgs.stream()
					.filter(n -> n.toLowerCase().startsWith(match))
					.collect(Collectors.toList());
		}
		else {
			result = Collections.emptyList();
		}
		
		return result;
	}
	
	/**
	 * Is this command interactive and requestes user input?
	 * @return
	 */
	public boolean isInteractive() {
		return interactive;
	}
	
	/**
	 * Is this command interactive and prompt the user?
	 * @param b
	 */
	protected void setInteractive(boolean b) {
		this.interactive  = b;
		
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
}