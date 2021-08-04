/**
 * Mars Simulation Project
 * DiagnosticsCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.ConversationRole;
import org.mars_sim.msp.core.logging.DiagnosticsManager;

/**
 * Controls which diagnostics are enabled.
 */
public class DiagnosticsCommand extends ChatCommand {
	public final static ChatCommand DIAGNOSTICS = new DiagnosticsCommand();
	

	private DiagnosticsCommand() {
		super(TopLevel.SIMULATION_GROUP, "dg", "diagnostics", "Change a module diagnostic logging");
		addRequiredRole(ConversationRole.EXPERT);
	}

	@Override
	public boolean execute(Conversation context, String input) {
		String module = null;
		boolean enabled = true;
		if (input != null) {
			String []parts = input.split(" ");
			if (parts.length == 2) {
				module = parts[0];
				if (parts[1].equalsIgnoreCase("Y")) {
					enabled = true;
				}
				else if (parts[1].equalsIgnoreCase("N")) {
					enabled = false;
				}
				else {
					module = null;
				}
			}
		}
				
		boolean result = false;
		if (module != null) {
			// Apply the change
			context.println((enabled ? "Enabling" : "Disabling")
					+ " diagnostics on module " + module);
			try {
				result = DiagnosticsManager.setDiagnostics(module.trim(), enabled);
			} catch (FileNotFoundException e) {
				context.println("Problem with diagnostics file "
								+ e.getMessage());	
			}
		}
		if (result == false) {
			printHelp(context);
		}
		return result;
	}
	
	private void printHelp(Conversation context) {
		String modules = Arrays.stream(DiagnosticsManager.MODULE_NAMES).collect(
						Collectors.joining(",", "'", "'"));
		context.println("Sorry wrong format. Must have arguments of <module name> <Y|N>");
		context.println("Module names are " + modules);
	}
}
