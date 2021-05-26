package org.mars.sim.console.chat.simcommand.unit;

import java.util.Map;
import java.util.Map.Entry;

import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.ConversationRole;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.TaskManager;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;

/** 
 * What work in terms of Tasks is available for this Person to do
 */
public class WorkerWorkCommand extends AbstractUnitCommand {
	
	public WorkerWorkCommand(String group) {
		super(group, "wk", "work", "Work that I can perform");
		addRequiredRole(ConversationRole.EXPERT);
	}

	@Override
	public boolean execute(Conversation context, String input, Unit source) {
		TaskManager tm = null;
		if (source instanceof Worker) {
			tm = ((Worker) source).getTaskManager();
		}
		else {
			context.println("Unit is not a Worker");
			return false;
		}
		
		StructuredResponse response = new StructuredResponse();
		
		Map<MetaTask, Double> tasks = tm.getLatestTaskProbability();
		response.appendTableHeading("Task", CommandHelper.TASK_WIDTH, "Prob", 6,
									"Trait", 14, "Favourite");
		for (Entry<MetaTask, Double> item : tasks.entrySet()) {
			MetaTask mt = item.getKey();
			response.appendTableRow(mt.getName(), item.getValue(),
									mt.getTraits(),
									mt.getFavourites()
									);
		}
		
		context.println(response.getOutput());
		return true;
	}

}
