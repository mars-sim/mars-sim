/**
 * Mars Simulation Project
 * SaveCommand.java
 * @version 3.1.2 2020-12-30
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

			// Wait for the save to complete
			try {
				lock.get(20, TimeUnit.SECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				System.err.println("Problem completing save wait" + e);
			}
        }

		context.println("Done");
		return true;
	}
}
