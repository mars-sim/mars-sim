package org.mars.sim.console.chat.simcommand;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.Simulation;

public class QuitCommand extends ChatCommand {

	public QuitCommand() {
		super(TopLevel.SIMULATION_GROUP, "x", "exit", "Exit the simulation");
	}

	@Override
	public void execute(Conversation context, String input) {
		String toSave = context.getInput("Exit not (Y/N)?");
        
        if ("Y".equals(toSave.toUpperCase())) {
            context.println("Exiting the Simulation...");
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
        	
	}

}
