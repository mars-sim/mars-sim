package org.mars.sim.console.chat.simcommand.settlement;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Command to display task allocation in a Settlement
 * This is a singleton.
 */
public class TaskCommand extends AbstractSettlementCommand {

	public static final ChatCommand TASK = new TaskCommand();
	private TaskCommand() {
		super("t", "task", "Task Roster");
	}

	/** 
	 * Output the current immediate location of the Unit
	 * @return 
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();
		
		response.appendText("(A). Settlers");
		response.appendTableHeading("Task", CommandHelper.TASK_WIDTH, "People", -CommandHelper.PERSON_WIDTH);
		
		Map<String, List<Person>> map = settlement.getAllAssociatedPeople().stream()
				.collect(Collectors.groupingBy(Person::getTaskDescription));

		for (Map.Entry<String, List<Person>> entry : map.entrySet()) {
			String task = entry.getKey();
			List<Person> plist = entry.getValue();
			String tableGroup = null;
			if (task != null) {
				tableGroup = task;
			} else {
				tableGroup = "None";
			}

			// Add the rows for each person
			for (Person p : plist) {
				response.appendTableRow(tableGroup, p.getName());
				tableGroup = ""; // Reset table subgroup
			}
		}

		response.appendBlankLine();
		response.appendText("(B). Bots");
		response.appendTableHeading("Task", CommandHelper.TASK_WIDTH, "Bots", -CommandHelper.BOT_WIDTH);

		Map<String, List<Robot>> botMap = settlement.getAllAssociatedRobots().stream()
				.collect(Collectors.groupingBy(Robot::getTaskDescription));

		for (Map.Entry<String, List<Robot>> entry : botMap.entrySet()) {
			String task = entry.getKey();
			List<Robot> plist = entry.getValue();
			String tableGroup = task;
			if (task != null) {
				tableGroup = task;
			} else {
				tableGroup = "None";
			}

			// Add the rows for each person
			for (Robot p : plist) {
				response.appendTableRow(tableGroup, p.getName());
				tableGroup = ""; // Reset table subgroup
			}
		}
		
		context.println(response.getOutput());
		
		return true;
	}

}
