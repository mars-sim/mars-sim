/*
 * Mars Simulation Project
 * WorkerWorkCommand.java
 * @date 2022-06-24
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.unit;

import java.util.Map.Entry;

import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.ConversationRole;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.TaskManager;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.person.ai.task.util.TaskCache;
import org.mars_sim.msp.core.person.ai.task.util.TaskJob;

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
		
		TaskCache tasks = tm.getLatestTaskProbability();
		double sum = tasks.getTotal();
		response.appendTableHeading("Task", 45, 
									"P Score", 9,
									"P %", 6);
		for (TaskJob item : tasks.getTasks()) {
			response.appendTableRow(item.getDescription(), 
									item.getScore(),
									String.format(CommandHelper.PERC1_FORMAT, (100D * item.getScore())/sum)
									);
		}
		
		context.println(response.getOutput());
		return true;
	}

}
