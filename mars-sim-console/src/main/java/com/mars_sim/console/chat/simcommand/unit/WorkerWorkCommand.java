/*
 * Mars Simulation Project
 * WorkerWorkCommand.java
 * @date 2022-06-24
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.unit;

import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.ConversationRole;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.Unit;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.PendingTask;
import com.mars_sim.core.person.ai.task.util.TaskCache;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskManager;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.time.MarsTime;

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
		
		// Extra info for Person
		if (source instanceof Person) {
			Person p = (Person)source;
			response.appendLabeledString("Job", p.getMind().getJob().getName());
			response.appendLabeledString("Favourite", p.getFavorite().getFavoriteActivity().getName());
		}

		// List pending tasks first
		var pending = tm.getPendingTasks();
		if (!pending.isEmpty()) {
			response.appendTableHeading("Pending Task", 45, 
							"When", 9);
			for (PendingTask item : pending) {
				response.appendTableRow(item.job().getName(), item.when().getTruncatedDateTimeStamp());
			}
			response.appendBlankLine();
		}

		TaskCache tasks = tm.getLatestTaskProbability();
		if (tasks == null) {
			response.append("No Tasks planned yet");
		}
		else {
			response.appendLabeledString("Context", tasks.getContext());

			MarsTime cacheCreated = tasks.getCreatedOn();
			if (cacheCreated != null) {
				response.appendLabeledString("Created On", cacheCreated.getDateTimeStamp());
			}

			double sum = tasks.getTotal();
			response.appendTableHeading("Potential Task", CommandHelper.TASK_WIDTH, "P %", 6,
										"P Score", 20);

			// Display the last selected as 1st entry
			TaskJob lastSelected = tasks.getLastSelected();
			if (lastSelected != null) {
				response.appendTableRow(lastSelected.getName(), "active",
										lastSelected.getScore().getOutput());
			}
			// Jobs in the cache
			for (TaskJob item : tasks.getTasks()) {
				response.appendTableRow(item.getName(), 
										String.format(CommandHelper.PERC1_FORMAT,
													(100D * item.getScore().getScore())/sum),
										item.getScore().getOutput()
										);
			}
		}		
		context.println(response.getOutput());
		return true;
	}

}
