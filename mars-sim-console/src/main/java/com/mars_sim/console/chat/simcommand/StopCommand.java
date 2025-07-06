/*
 * Mars Simulation Project
 * StopCommand.java
 * @date 2024-08-10
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.ConversationRole;
import com.mars_sim.core.Simulation;

public class StopCommand extends ChatCommand {

	public StopCommand() {
		super(TopLevel.SIMULATION_GROUP, "st", "stop", "Stop the simulation");
		setInteractive(true);
		addRequiredRole(ConversationRole.ADMIN);
	}

	@Override
	public boolean execute(Conversation context, String input) {        
        if (context.getBooleanInput("Stop the simulation")) {
            context.println("Stopping the Simulation...");
            Simulation sim = context.getSim();
        	sim.endSimulation(); 
    		sim.getMasterClock().exitProgram();

			context.setCompleted();
			context.println("Closing conversation.");
			
			// Odd that we have to force the termination
			System.exit(0);
        }
        else {
        	context.println("OK, exit skipped.");
        }
        return true;
	}

}
