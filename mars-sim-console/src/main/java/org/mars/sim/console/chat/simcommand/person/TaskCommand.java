package org.mars.sim.console.chat.simcommand.person;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;

/** 
 * 
 */
public class TaskCommand extends AbstractPersonCommand {
	public static final ChatCommand TASK = new TaskCommand();
	
	private TaskCommand() {
		super("ta", "task", "About my current activity");
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {
		StructuredResponse response = new StructuredResponse();
		
		response.appendHeading("Task stack");
		StringBuilder prefix = new StringBuilder();
		// Task should come off person
		Task task = person.getMind().getTaskManager().getTask();
		while(task != null) {
			TaskPhase phase = task.getPhase();
			
			StringBuilder sb = new StringBuilder();
			sb.append(prefix);
			sb.append(task.getDescription(false));
			
			if (phase != null) {
				sb.append(" (");
				sb.append(phase.getName());
				sb.append(")");
			}
			response.append(sb.toString());
			response.appendBlankLine();
				
			task = task.getSubTask();
			if ((task != null) && task.isDone()) {
				// If the Tak is done why has it not been removed ????
				task = null;
			}
			prefix.append("->");
		}
		
		context.println(response.getOutput());
		return true;
	}
}
