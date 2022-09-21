/*
 * Mars Simulation Project
 * WorkerTaskCommand.java
 * @date 2022-06-24
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.unit;

import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskManager;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.person.ai.task.util.Worker;

/** 
 * 
 */
public class WorkerTaskCommand extends AbstractUnitCommand {
	
	public WorkerTaskCommand(String group) {
		super(group, "ta", "task", "About my current activity");
	}

	@Override
	public boolean execute(Conversation context, String input, Unit source) {
		TaskManager mgr = null;
		if (source instanceof Worker) {
			mgr = ((Worker) source).getTaskManager();
		}
		else {
			context.println("Unit " + source.getName() + " is not a Worker.");
			return false;
		}
		StructuredResponse response = new StructuredResponse();
		response.appendBlankLine();
		response.appendHeading("Task stack");
		StringBuilder prefix = new StringBuilder();
		// Task should come off person
		Task task = mgr.getTask();
		while(task != null) {
			TaskPhase phase = task.getPhase();
			
			StringBuilder sb = new StringBuilder();
			sb.append(prefix + " ");
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
