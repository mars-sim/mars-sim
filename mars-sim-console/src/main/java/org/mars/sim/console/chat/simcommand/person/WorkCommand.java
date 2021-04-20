package org.mars.sim.console.chat.simcommand.person;

import java.util.Map;
import java.util.Map.Entry;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.ConversationRole;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;

/** 
 * What work in terms of Tasks is available for this Person to do
 */
public class WorkCommand extends AbstractPersonCommand {
	public static final ChatCommand WORK = new WorkCommand();
	
	private WorkCommand() {
		super("wk", "work", "Work that I can perform");
		addRequiredRole(ConversationRole.EXPERT);
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {
		StructuredResponse response = new StructuredResponse();

		Map<MetaTask, Double> tasks = person.getMind().getTaskManager().getLatestTaskProbability();
		response.appendTableHeading("Task", CommandHelper.TASK_WIDTH, "Probability", 4);
		for (Entry<MetaTask, Double> item : tasks.entrySet()) {
			response.appendTableRow(item.getKey().getName(), item.getValue());
		}
		
		context.println(response.getOutput());
		return true;
	}

}
