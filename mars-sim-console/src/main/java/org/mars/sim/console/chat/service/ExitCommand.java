package org.mars.sim.console.chat.service;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.TopLevel;

/**
 * A stateless command that exit and leaves the corrent Conversation
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
            context.println("Exitting...");
			context.setCompleted();
        }
        else {
        	context.println("OK, exit skipped");
        }
        return true;

	}

}
