/*
 * Mars Simulation Project
 * ExitCommand.java
 * @date 2024-08-10
 * @author Barry Evans
 */

package com.mars_sim.console.chat.service;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.TopLevel;

/**
 * A stateless command that exit and leaves the current conversation.
 */
public class ExitCommand extends ChatCommand {

	public static final ChatCommand EXIT = new ExitCommand();

	public ExitCommand() {
		super(TopLevel.SIMULATION_GROUP, "ex", "exit", "Exit the current conversation");
		
		setInteractive(true);
	}

	@Override
	public boolean execute(Conversation context, String input) {
	String toExit = context.getInput("Exit the console session (Y/N)?");
        
        if ("Y".equalsIgnoreCase(toExit)) {
            context.println("Exiting...");
			context.setCompleted();
        }
        else {
        	context.println("OK, exit skipped.");
        }
        return true;

	}

}
