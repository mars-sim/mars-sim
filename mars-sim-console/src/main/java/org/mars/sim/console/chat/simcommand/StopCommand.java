/**
 * Mars Simulation Project
 * StopCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.ConversationRole;
import org.mars_sim.msp.core.Simulation;

public class StopCommand extends ChatCommand {

	public StopCommand() {
		super(TopLevel.SIMULATION_GROUP, "st", "stop", "Stop the simulation");
		setInteractive(true);
		addRequiredRole(ConversationRole.ADMIN);
	}

	@Override
	public boolean execute(Conversation context, String input) {
		String toSave = context.getInput("Stop the simulation (Y/N)?");
        
        if ("Y".equalsIgnoreCase(toSave)) {
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
        	context.println("OK, exit skipped");
        }
        return true;
	}

}
