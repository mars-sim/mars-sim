/*
 * Mars Simulation Project
 * CommandHelper.java
 * @date 2022-06-27
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.Malfunction.Repairer;
import org.mars_sim.msp.core.malfunction.MalfunctionRepairWork;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionLog;
import org.mars_sim.msp.core.person.ai.mission.MissionPlanning;
import org.mars_sim.msp.core.person.ai.mission.MissionStatus;
import org.mars_sim.msp.core.person.ai.mission.NavPoint;
import org.mars_sim.msp.core.person.ai.mission.PlanType;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.LoadingController;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessor;
import org.mars_sim.msp.core.time.MarsClockFormat;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Helper class with common formatting methods.
 */
public class CommandHelper {
	// Width for names
	
	public static final int MALFUNCTION_WIDTH = 28;
	public static final int BUILIDNG_WIDTH = 26;
	public static final int PROCESS_WIDTH = 55;
	//Width of a Person value
	public static final int PERSON_WIDTH = 22;
	// Width of a Job vlaue
	public static final int JOB_WIDTH = 16;
	// Width of a Role value
	public static final int ROLE_WIDTH = 33;
	// Width of a Task value
	public static final int TASK_WIDTH = 30;
	// Width of a Bot name
	public static final int BOT_WIDTH = 19;
	// Width of a truncated timestamp 
	private static final int TIMESTAMP_TRUNCATED_WIDTH = 15;
	// Width of a Coordinate
	public static final int COORDINATE_WIDTH = 24;
    public static final int GOOD_WIDTH = 30;
	
	// Base value formats for use with String.format
	public static final String DOUBLE_FORMAT = "%.2f";
	public static final String KG_FORMAT = "%.2f kg";
	public static final String KM_FORMAT = "%.2f km";
	public static final String PERC_FORMAT = "%.0f%%";
	public static final String PERC1_FORMAT = "%.1f%%";
	public static final String MILLISOL_FORMAT = "%.1f millisol";
	public static final String KMPH_FORMAT = "%.2f km/h";
    public static final String MONEY_FORMAT = "$%,.2f";
	private static final String DUE_FORMAT = "%d:" + MarsClockFormat.TRUNCATED_TIME_FORMAT;

	
	private CommandHelper() {
		// Do nothing
	}

	/**
	 * Prompt teh user to select an option from a list of choices
	 * @param context
	 * @param names
	 * @param string
	 * @return
	 */
	public static int getOptionInput(Conversation context, List<String> names, String prompt) {
		int idx = 1;
		context.println("0 - Select none/cancel");
		for (String name : names) {
			context.println(idx++ + " - " + name);
		}
		int choice = context.getIntInput(prompt + " >");
		if ((choice < 0) || (choice >= idx)) {
			context.println("Invalid choice");
			choice = -1;
		}
		else {
			choice--;
		}
		return choice;
	}
	
	/**
	 * Display the status of a Scientific Study
	 * @param response
	 * @param study
	 */
	public static void outputStudy(StructuredResponse response, ScientificStudy study) {
		response.appendHeading(study.getName());
		response.appendLabeledString("Science", study.getScience().getName());
		response.appendLabeledString("Lead", study.getPrimaryResearcher().getName());
		response.appendLabeledString("Phase", study.getPhase());
		response.appendLabelledDigit("Level", study.getDifficultyLevel());

		
		switch(study.getPhase()) {
		case ScientificStudy.PROPOSAL_PHASE:
			response.appendLabeledString("Completed",
					String.format(PERC_FORMAT, (100D * study.getProposalWorkTimeCompleted())
							/ study.getTotalProposalWorkTimeRequired()));
			break;
			
		case ScientificStudy.INVITATION_PHASE:
			response.appendLabelledDigit("Max Collaborators", study.getMaxCollaborators());
			response.appendTableHeading("Invitee", CommandHelper.PERSON_WIDTH, "Responded", "Accepted");
			
			Set<Person> c = study.getCollaborativeResearchers();
			for (Person person :  study.getInvitedResearchers()) {
				response.appendTableRow(person.getName(),
										study.hasInvitedResearcherResponded(person),
										c.contains(person));
			}
			break;
		
		case ScientificStudy.PAPER_PHASE:
		case ScientificStudy.RESEARCH_PHASE:
			displayCollaborators(response, study);			
			break;
			
		case ScientificStudy.PEER_REVIEW_PHASE:
			// Safe to cast because value will always be 100 or less
			response.appendLabeledString("Completed", String.format(PERC_FORMAT,
											(100D * study.getPeerReviewTimeCompleted())
													/ study.getTotalPeerReviewTimeRequired()));
			break;
			
		default:
			break;
		}
	}
	
	/**
	 * Display a list of Collalorators
	 * @param response
	 * @param study
	 */
	private static void displayCollaborators(StructuredResponse response, ScientificStudy study) {
		boolean paper;
		double colabExpected;
		double primeExpected;
		
		if (study.getPhase().equals(ScientificStudy.PAPER_PHASE)) {
			paper = true;
			colabExpected = study.getTotalCollaborativePaperWorkTimeRequired();
			primeExpected = study.getTotalPrimaryPaperWorkTimeRequired();

		}
		else {
			paper = false;
			colabExpected = study.getTotalCollaborativeResearchWorkTimeRequired();
			primeExpected = study.getTotalPrimaryResearchWorkTimeRequired();
		}
		
		response.appendText("Researchers");
		response.appendTableHeading("Reseacher", CommandHelper.PERSON_WIDTH, "Contribution",
									(paper ? "Paperwork %" : "Research %"));
		response.appendTableRow(study.getPrimaryResearcher().getName(), study.getScience().getName(),
								100D * (paper ? study.getPrimaryPaperWorkTimeCompleted()
										: study.getPrimaryResearchWorkTimeCompleted())				 				
				 					/ primeExpected);

		// Details about collaborators
		Set<Person> researchers = study.getCollaborativeResearchers();		
		for (Person person : researchers) {
			response.appendTableRow(person.getName(),
									study.getContribution(person).getName(),
									100D * (paper ? study.getCollaborativePaperWorkTimeCompleted(person) 
											: study.getCollaborativeResearchWorkTimeCompleted(person))
										/ colabExpected);				
		}
	}

	/**
	 * Display the details of a list of Airlocks
	 * @param response Output for details.
	 * @param airlocks
	 */
	public static void outputAirlock(StructuredResponse response, List<Airlock> airlocks) {
		response.appendTableHeading("Name", BUILIDNG_WIDTH, "State", 14, "Active",
									"Operator", PERSON_WIDTH,
									"Use", 
									"Inner", "Outer");
		
		for (Airlock airlock : airlocks) {
			response.appendTableRow(airlock.getEntityName(), airlock.getState().name(),
									airlock.isActivated(),
									airlock.getOperatorName(),
									String.format("%d/%d", airlock.getNumOccupants(), airlock.getCapacity()),
									(airlock.isInnerDoorLocked() ? "LCK" : "ULK"),
									(airlock.isOuterDoorLocked() ? "LCK" : "ULK"));
		}
	}
	
	/**
	 * Display the details of a list of Airlocks
	 * @param response Output for details.
	 * @param airlocks
	 */
	public static void outputAirlockDetailed(StructuredResponse response, String name, Airlock airlock) {
		response.appendLabeledString("Name", name);
		response.appendLabeledString("Operator", airlock.getOperatorName());
		response.appendLabeledString("State", airlock.getState().name());
		response.appendLabeledString("Activated", (airlock.isActivated() ? "Yes" : "No"));
		response.appendLabeledString("Doors", "Inner-" + (airlock.isInnerDoorLocked() ? "LCK" : "ULK")
												+ " Outer-" + (airlock.isOuterDoorLocked() ? "LCK" : "ULK"));
		response.appendLabeledString("Waiting", "Inner-" + airlock.getNumAwaitingInnerDoor()
												+ " Outer-" + airlock.getNumAwaitingOuterDoor());
		
		response.appendTableHeading("Occupant", PERSON_WIDTH, "<->", "Has Suit");
		for (int pID : airlock.getOccupants()) {
			Person p = airlock.getPersonByID(pID);
			if (p != null) {
				String direction = null;
				
				// Not nice code. Step through the Task stack looking for a Airlock tack
				Task active = p.getTaskManager().getTask();
				while ((active != null) && (direction == null)) {
					if (active.getTaskSimpleName().equalsIgnoreCase("EnterAirlock")) {
						direction = "In";
					}
					else if (active.getTaskSimpleName().equalsIgnoreCase("Exitairlock")) {
						direction = "Out";
					}
					else {
						active = active.getSubTask();
					}
				}
				
				response.appendTableRow(p.getName(), (direction != null ? direction : "?"),
									    p.getSuit() != null ? "Yes" : "No");
			}
		}
	}
	
	/**
	 * This generates the details of a mission.
	 * @param response Output destination
	 * @param mission Mission in question
	 */
	public static void outputMissionDetails(StructuredResponse response, Mission mission) {
		List<Worker> plist = new ArrayList<>(mission.getMembers());
		Person startingPerson = mission.getStartingPerson();
		plist.remove(startingPerson);
	
		double dist = 0;
		double trav = 0;
		Vehicle v = null;
		
		if (mission instanceof VehicleMission) {
			VehicleMission vm = (VehicleMission) mission;
			v = vm.getVehicle();
			dist = vm.getDistanceProposed();
			trav = vm.getTotalDistanceTravelled();
		}
	
		if (v != null) {
			response.appendLabeledString("Vehicle", v.getName());
			response.appendLabeledString("Type", v.getVehicleTypeString());
			response.appendLabeledString("Est. Dist.", String.format(KM_FORMAT, dist));
			response.appendLabeledString("Travelled", String.format(KM_FORMAT, trav));
		}
		
		MissionPlanning mp = mission.getPlan();
		if ((mp != null) && (mp.getStatus() == PlanType.PENDING)) {
			StringBuilder planMsg = new StringBuilder();
			PlanType st = mp.getStatus();
			planMsg.append(st.getName());
			planMsg.append(' ');
			planMsg.append(String.format(PERC_FORMAT, mp.getPercentComplete()));

			response.appendLabeledString("Plan Status", planMsg.toString());
		}
		
		response.appendLabeledString("Lead", startingPerson.getName());		
		response.appendLabeledString("Status:",
									mission.getMissionStatus().stream()
									.map(MissionStatus::getName)
									.collect(Collectors.joining(", ")));
		
		if (!mission.isDone()) {
			response.appendLabeledString("Phase", mission.getPhaseDescription());
			response.appendLabeledString("Phase Started", mission.getPhaseStartTime().getTrucatedDateTimeStamp());
		
			List<String> names = plist.stream().map(p -> p.getName()).sorted().collect(Collectors.toList());
			response.appendNumberedList("Members", names);
		
			// Travel mission has a route
			if (mission instanceof VehicleMission) {
				VehicleMission tm = (VehicleMission) mission;
				int navPoints = tm.getNumberOfNavpoints();
				if ((navPoints > 0) && (tm.getNextNavpointIndex() >= 0)) {
					response.appendText("Itinerary:");
					response.appendTableHeading("Way Point", COORDINATE_WIDTH, "Distance", 10,
										"Description");
					for(int i = tm.getNextNavpointIndex(); i < navPoints; i++) {
						NavPoint nv = tm.getNavpoint(i);
						String distance = String.format(KM_FORMAT, nv.getDistance());
						if (nv.isSettlementAtNavpoint()) {
							response.appendTableRow(nv.getSettlement().getName(), distance, "");
						}
						else {
							response.appendTableRow(nv.getLocation().getCoordinateString(),
									distance,
									nv.getDescription());
						}
					}
					response.appendBlankLine();				
				}
		
				// Vehicle mission has a loading
				LoadingController lp = tm.getLoadingPlan();
				if ((lp != null) && !lp.isCompleted()) {
					response.appendText("Loading from " + lp.getSettlement().getName() + " :");
					outputResources("Resources", response, lp.getResourcesManifest());	
					outputResources("Optional Resources", response, lp.getOptionalResourcesManifest());	
					outputEquipment("Equipment", response, lp.getEquipmentManifest());	
					outputEquipment("Optional Equipment", response, lp.getOptionalEquipmentManifest());	
				}
			}
		}
		// Mission log
		response.appendText("Log:");
		response.appendTableHeading("Time", TIMESTAMP_TRUNCATED_WIDTH, "Phase");
		for (MissionLog.MissionLogEntry entry : mission.getLog().getEntries()) {
			response.appendTableRow(entry.getTime(), entry.getEntry());
		}
	}

	private static void outputEquipment(String title, StructuredResponse response,
			Map<Integer, Integer> manifest) {
		if (!manifest.isEmpty()) {
			response.appendText(title);
			response.appendTableHeading("Item", 22, "Quantity");
			
			for(Entry<Integer, Integer> item : manifest.entrySet()) {
				response.appendTableRow(EquipmentType.convertID2Type(item.getKey()).getName(),
						Integer.toString(item.getValue().intValue()));
			}
			response.appendBlankLine();
		}
	}

	private static void outputResources(String title, StructuredResponse response,
			Map<Integer, Number> resourcesManifest) {
		if (!resourcesManifest.isEmpty()) {
			response.appendText(title);
			response.appendTableHeading("Item", 30, "Quantity");
			
			for(Entry<Integer, Number> item : resourcesManifest.entrySet()) {
				int id = item.getKey();
				String quantity;
				String name;
				if (id < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
					name = ResourceUtil.findAmountResourceName(id);
					quantity = String.format(KG_FORMAT, item.getValue().doubleValue());
				}
				else {
					name = ItemResourceUtil.findItemResource(id).getName();
					quantity = Integer.toString(item.getValue().intValue());
				}	
				response.appendTableRow(name, quantity);
			}
			response.appendBlankLine();

		}
	}

	/**
	 * Output the details of Malfunction
	 * @param response Destination for output
	 * @param m Malfunction to describe
	 */
	public static void outputMalfunction(StructuredResponse response, Malfunction m) {
		response.appendLabelledDigit("Severity", m.getSeverity());
		response.appendLabeledString("Fixed ", String.format(PERC_FORMAT, m.getPercentageFixed()));
		
		// Parts
		Map<Integer, Integer> parts = m.getRepairParts();
		if (!parts.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (Entry<Integer, Integer> p : parts.entrySet()) {
				Part part = ItemResourceUtil.findItemResource(p.getKey());
				sb.append(part.getName()).append("@").append(p.getValue()).append(' ');
			}
			response.appendLabeledString("Parts", sb.toString());
		}
		
		// Work
		for (MalfunctionRepairWork rw : MalfunctionRepairWork.values()) {
			double workTime = m.getWorkTime(rw);
			if (workTime > 0) {
				response.appendBlankLine();
				response.appendHeading(rw.getName() + " Repair Work");
				double completedTime = m.getCompletedWorkTime(rw);
				response.appendLabeledString("Work (millisol)", 
											 String.format("%.1f/%.1f", completedTime, workTime));
				
				String chief = m.getChiefRepairer(rw);
				String deputy = m.getDeputyRepairer(rw);
				
				StringBuilder sb = new StringBuilder();
				sb.append(chief != null ? chief : "none")
				  .append(", ")
				  .append(deputy != null ? deputy : "none");
				response.appendLabeledString("Chief/Deputy", sb.toString());
				response.appendLabeledString("Slots available",
						String.format("%d/%d", m.numRepairerSlotsEmpty(rw),
											   m.getDesiredRepairers(rw)));
				
				// Workers
				response.appendTableHeading("Repairer", PERSON_WIDTH,
											"Active", "Work (millisol)");
				for (Repairer effort : m.getRepairersEffort(rw)) {
					response.appendTableRow(effort.getWorker(), effort.isActive(), effort.getWorkTime());
				}
			}
		}
	}

	/**
	 * Get the a Coordinates from user input. 
	 * @param desc A prompt for the user explaining the purpose
	 * @param context COntext of the conversation
	 */
	public static Coordinates getCoordinates(String desc, Conversation context) {
		double lat1 = 0;
		double lon1 = 0;
		boolean good = false;
		
		//Get lat
		do {
			try {
				String latitudeStr1 = context.getInput("What is the latitude (e.g. 10.03 N, 5.01 S) of the " 
						+ desc.toLowerCase() + " coordinate ?");
				if (latitudeStr1.equalsIgnoreCase("quit") || latitudeStr1.equalsIgnoreCase("/q")
						|| latitudeStr1.isBlank())
					return null;
				else {
					lat1 = Coordinates.parseLatitude2Phi(latitudeStr1);
					good = true;
				}
			} catch(IllegalStateException e) {
				context.println("Not a valid format");
				good = false;
			}
		} while (!good);
		
		do {
			try {
				String longitudeStr = context.getInput("What is the longitude (e.g. 5.09 E, 18.04 W) of the "
						+ desc.toLowerCase() + " coordinate ?");
				if (longitudeStr.equalsIgnoreCase("quit") || longitudeStr.equalsIgnoreCase("/q")
						|| longitudeStr.isBlank())
					return null;
				else {
					lon1 = Coordinates.parseLongitude2Theta(longitudeStr);
					good = true;
				}
			} catch(IllegalStateException e) {
				context.println("Not a valid format");
				good = false;
			}
		} while (!good);
		
		return new Coordinates(lat1, lon1);
	}

	/**
	 * Output the processes that a Resource Processor is running. Put these in a table.
	 * @param response Output destination
	 * @param processType The name of the process type column
	 * @param currentMSol The current mars time
	 * @param bName The hosting Building name
	 * @param processor Host of the processes
	 */
	public static void outputProcesses(StructuredResponse response, String processType, int currentMSol, String bName,
										ResourceProcessor processor) {

		// Build table label by placing the building name before the process type
		// Works as PROCESS_WIDTh is very large
		int width = PROCESS_WIDTH - processType.length();
		StringBuilder firstColumn = new StringBuilder();
		firstColumn.append(String.format("%-" + width + "s", bName));
		firstColumn.append(processType);

	    List<ResourceProcess> processes = new ArrayList<>(processor.getProcesses());
		processes.sort((ResourceProcess h1, ResourceProcess h2) -> h1.getProcessName().compareTo(h2.getProcessName()));

		response.appendTableHeading(firstColumn.toString(), PROCESS_WIDTH,
					"Active", "Level", "Toggle");
		for(ResourceProcess p : processes) {
			String nextToggle = null;
			if (p.isToggleAvailable()) {
				// Toggling is available
				double[] toggleTime = p.getToggleSwitchDuration();
				if (toggleTime[0] == 0D) {
					nextToggle = "Available ";
				}
				else {
					nextToggle = String.format(PERC_FORMAT, (100D * toggleTime[0])/toggleTime[1]);
				}
				
				// Flag if it is being currently toggled by someone
				if (p.isFlagged()) {
					nextToggle = "Active " + nextToggle;
				}
			}
			else {
				int[] remainingTime = p.getTimeLimit();
				nextToggle = "Due @ " + String.format(DUE_FORMAT, remainingTime[0], remainingTime[1]);
			}

			response.appendTableRow(p.getProcessName(), p.isProcessRunning(),
									p.getCurrentProductionLevel(), nextToggle);
		}
	}
}
