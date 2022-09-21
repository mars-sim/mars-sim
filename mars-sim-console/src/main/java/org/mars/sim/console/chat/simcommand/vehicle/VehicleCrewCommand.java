/*
 * Mars Simulation Project
 * VehicleCrewCommand.java
 * @date 2022-08-24
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.vehicle;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.vehicle.Crewable;
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
		Collection<Person> people = null;
		Collection<Robot> robots = null;
		
		// Find the crew off the Mission if one assigned
		// and outside the Settlement
		Mission m = source.getMission();
		if (m != null) {
			Collection<Worker> members = m.getMembers();
			people = members.stream()
							.filter(mp -> mp instanceof Person)
							.map(p -> (Person) p)
							.collect(Collectors.toList());
			robots = members.stream()
					.filter(mr -> mr instanceof Robot)
					.map(r -> (Robot) r)
					.collect(Collectors.toList());
		}
		else if (source instanceof Crewable) {
			Crewable bus = (Crewable) source;

			people = bus.getCrew();
			robots = bus.getRobotCrew();
		}
		
		if (people == null && robots == null) {
			context.println("Sorry this has no crew");
		}
		else {
			StructuredResponse response = new StructuredResponse();
	
			if (people != null) {
				Map<String, List<Worker>> pmap = people.stream()
						.collect(Collectors.groupingBy(Person::getTaskDescription, Collectors.toList()));
				outputTasks(response, "People", pmap);
			}
			
			if (robots != null) {
				Map<String, List<Worker>> rmap = robots.stream()
						.collect(Collectors.groupingBy(Robot::getTaskDescription, Collectors.toList()));
				if (!rmap.isEmpty()) {
					response.appendBlankLine();
					outputTasks(response, "Robots", rmap);
				}
			}
			
			context.println(response.getOutput());
		}
		return true;
	}
	
	private static void outputTasks(StructuredResponse response, String name, Map<String, List<Worker>> tasks) {
		response.appendTableHeading("Task", CommandHelper.TASK_WIDTH, "OnBoard", name, CommandHelper.PERSON_WIDTH);
		for (Map.Entry<String, List<Worker>> entry : tasks.entrySet()) {
			String task = entry.getKey();
			List<Worker> plist = entry.getValue();
			String tableGroup = null;
			if (task != null) {
				tableGroup = task;
			} else {
				tableGroup = "None";
			}

			// Add the rows for each person
			for (Worker p : plist) {
				response.appendTableRow(tableGroup, p.isInVehicle(), p.getName());
				tableGroup = ""; // Reset table subgroup
			}
		}
	}

}
