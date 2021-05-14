package org.mars.sim.console.chat.simcommand;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.ConversationRole;
import org.mars_sim.msp.core.person.ai.task.utils.TaskManager;

/**
 * Controls whether Task diagnostics are available.
 */
public class TaskDiagCommand extends ChatCommand {
	public final static ChatCommand TASKDIAG = new TaskDiagCommand();
	
	private TaskDiagCommand() {
		super(TopLevel.SIMULATION_GROUP, "td", "task diag", "Change the task diagnostic logging");
		addRequiredRole(ConversationRole.EXPERT);
	}

	@Override
	public boolean execute(Conversation context, String input) {
		String outcome = TaskManager.toggleDiagnostics();
		context.println("Toggle outcome: " + outcome);
		return true;
	}
}
