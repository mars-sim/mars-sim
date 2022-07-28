/*
 * Mars Simulation Project
 * SaveCommand.java
 * @date 2022-07-28
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.ConversationRole;
import org.mars_sim.msp.core.SimulationListener;

public class SaveCommand extends ChatCommand {

	public SaveCommand() {
		super(TopLevel.SIMULATION_GROUP, "sv", "save", "Save the simulation");
		setInteractive(true);
		addRequiredRole(ConversationRole.ADMIN);
	}


	@Override
	public boolean execute(Conversation context, String input) {
		String toSave = context.getInput("Save simulation (Y/N)?");
	
        if ("Y".equalsIgnoreCase(toSave)) {
            context.println("Saving Simulation...");

			CompletableFuture<Boolean> lock = new CompletableFuture<>();
				context.getSim().requestSave(null, action -> {
					if (SimulationListener.SAVE_COMPLETED.equals(action)) {
						lock.complete(true);
					}
				});

			// Print the size of all serialized objects
			context.println("");
			context.println("Method 1 - Using Outputstream as a Counter");
			context.println("");
			context.println(context.getSim().printObjectSize(0).toString());
			context.println("");
			context.println("Method 2 - Using Byte Arrays");
			context.println("");
			context.println(context.getSim().printObjectSize(1).toString());
			
			// Wait for the save to complete
			try {
				lock.get(20, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				context.println("Problem completing the save wait: " + e);
				Thread.currentThread().interrupt();
				return false;
			} catch (ExecutionException | TimeoutException e) {
				context.println("Problem executing the save wait: " + e);
				return false;
			}
			
        }

		context.println("Done Saving.");
		context.println("");
		
		return true;
	}
}
