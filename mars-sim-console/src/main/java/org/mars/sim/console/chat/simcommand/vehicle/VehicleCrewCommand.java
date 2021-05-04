package org.mars.sim.console.chat.simcommand.vehicle;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Command to get the specs of a vehicle
 * This is a singleton.
 */
public class VehicleCrewCommand extends ChatCommand {

	public static final ChatCommand CREW = new VehicleCrewCommand();

	private VehicleCrewCommand() {
		super(VehicleChat.VEHICLE_GROUP, "cr", "crew", "What is the crew of the vehicle");
	}

	/** 
	 * Output the current immediate location of the Unit
	 * @return 
	 */
	@Override
	public boolean execute(Conversation context, String input) {
		VehicleChat parent = (VehicleChat) context.getCurrentCommand();
		Vehicle source = parent.getVehicle();
		StructuredResponse response = new StructuredResponse();

		Map<String, List<String>> pmap = source.getAffectedPeople().stream()
				.collect(Collectors.groupingBy(Person::getTaskDescription, Collectors.mapping(Person::getName,
					                             Collectors.toList())));
		outputTasks(response, "People", pmap);
		
		Map<String, List<String>> rmap = source.getAffectedRobots().stream()
				.collect(Collectors.groupingBy(Robot::getTaskDescription, Collectors.mapping(Robot::getName,
					                             Collectors.toList())));
		if (!rmap.isEmpty()) {
			response.appendBlankLine();
			outputTasks(response, "Robots", rmap);
		}
		
		context.println(response.getOutput());
		return true;
	}
	
	private static void outputTasks(StructuredResponse response, String name, Map<String, List<String>> tasks) {
		response.appendTableHeading("Task", CommandHelper.TASK_WIDTH, name, CommandHelper.PERSON_WIDTH);
		for (Map.Entry<String, List<String>> entry : tasks.entrySet()) {
			String task = entry.getKey();
			List<String> plist = entry.getValue();
			String tableGroup = null;
			if (task != null) {
				tableGroup = task;
			} else {
				tableGroup = "None";
			}

			// Add the rows for each person
			for (String p : plist) {
				response.appendTableRow(tableGroup, p);
				tableGroup = ""; // Reset table subgroup
			}
		}
	}

}
