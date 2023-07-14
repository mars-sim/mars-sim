/*
 * Mars Simulation Project
 * WorkerActivityCommand.java
 * @date 2022-06-24
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.unit;

import java.util.List;

import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.data.History.HistoryItem;
import org.mars_sim.msp.core.person.ai.task.util.TaskManager;
import org.mars_sim.msp.core.person.ai.task.util.TaskManager.OneActivity;
import org.mars_sim.msp.core.person.ai.task.util.Worker;

/** 
 * 
 */
public class WorkerActivityCommand extends AbstractUnitCommand {
	
	public WorkerActivityCommand(String group) {
		super(group, "ac", "activities", "Activites done by the Worker");
	}

	/** 
	 * Output the current immediate location of the Unit
	 */
	@Override
	protected boolean execute(Conversation context, String input, Unit target) {

		TaskManager tManager = null;

		if (target instanceof Worker w) {
			tManager = w.getTaskManager();
		}
		else {
			context.println("Sorry I am not a Worker.");
			return false;
		}
		
		List<HistoryItem<OneActivity>> tasks = tManager.getAllActivities().getChanges();
		
		StructuredResponse response = new StructuredResponse();
		response.appendTableHeading("When", 8,
						"Task", 20,
									"Activity", -32,
									"Phase");
		for (HistoryItem<OneActivity> attr : tasks) {
			OneActivity act = attr.getWhat();
			response.appendTableRow(attr.getWhen().getDateTimeStamp(),
									act.getTaskName(),
									act.getDescription(),
									act.getPhase());
		}
		context.println(response.getOutput());
		
		return true;
	}
}
