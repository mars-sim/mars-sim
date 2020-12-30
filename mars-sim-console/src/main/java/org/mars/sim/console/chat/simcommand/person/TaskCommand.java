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
	public void execute(Conversation context, String input, Person person) {
		StructuredResponse response = new StructuredResponse();
		
		response.appendTableHeading("Task", TASK_WIDTH, "Phase");
		
		StringBuilder prefix = new StringBuilder();
		// Task should come off person
		Task task = person.getMind().getTaskManager().getTask();
		while(task != null) {
			TaskPhase phase = task.getPhase();
			
			// Why does a Task get method return details of the SubTask ?????
			response.appendTableRow(prefix.toString() + task.getName(false), (phase != null ? phase.getName() : ""));
			
			task = task.getSubTask();
			if ((task != null) && task.isDone()) {
				// If the Tak is done why has it not been removed ????
				task = null;
			}
			prefix.append("-");
		}
		
		context.println(response.getOutput());
	}
}
