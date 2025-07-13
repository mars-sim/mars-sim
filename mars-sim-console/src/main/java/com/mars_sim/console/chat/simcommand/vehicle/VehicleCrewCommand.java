/*
 * Mars Simulation Project
 * VehicleCrewCommand.java
 * @date 2022-08-24
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.vehicle;

import java.util.Collection;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.Vehicle;

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
							.filter(Person.class::isInstance)
							.map(p -> (Person) p)
							.toList();
			robots = members.stream()
					.filter(Robot.class::isInstance)
					.map(r -> (Robot) r)
					.toList();
		}
		else if (source instanceof Crewable bus) {
			people = bus.getCrew();
			robots = bus.getRobotCrew();
		}
		
		if (people == null && robots == null) {
			context.println("Sorry this has no crew");
		}
		else {
			StructuredResponse response = new StructuredResponse();
	
			if (people != null) {
				outputTasks(response, "People", people);
			}
			
			if (robots != null && !robots.isEmpty()) {
				response.appendBlankLine();
				outputTasks(response, "Robots", robots);
			}
			
			context.println(response.getOutput());
		}
		return true;
	}
	
	private static void outputTasks(StructuredResponse response, String name, Collection<? extends Worker> workers) {
		response.appendTableHeading(name, CommandHelper.PERSON_WIDTH, "OnBoard", "Task", -CommandHelper.TASK_WIDTH);
		for (var p : workers) {
			String taskDesc = "";
			var task = p.getTaskManager().getTask();
			if (task != null) {
				taskDesc = task.getStatus();
			}

			response.appendTableRow(p.getName(), p.isInVehicle(), taskDesc);
		}
	}

}
