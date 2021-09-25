/**
 * Mars Simulation Project
 * CommandHelper.java
 * @version 3.1.2 2020-12-30
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
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.Malfunction.Repairer;
import org.mars_sim.msp.core.malfunction.MalfunctionRepairWork;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.core.person.ai.mission.MissionPlanning;
import org.mars_sim.msp.core.person.ai.mission.NavPoint;
import org.mars_sim.msp.core.person.ai.mission.TravelMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.LoadingController;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Helper class with common formatting methods.
 */
public class CommandHelper {
	// Width for names
	
	public static final int MALFUNCTION_WIDTH = 28;
	public static final int BUILIDNG_WIDTH = 26;
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
	public static final String KG_FORMAT = "%.2f kg";
	public static final String MILLISOL_FORMAT = "%.1f millisol";

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
			// Safe to cast because value will always be 100 or less
			String done = (int) Math.round((100D * study.getProposalWorkTimeCompleted())
								/ study.getTotalProposalWorkTimeRequired()) + "%";
			response.appendLabeledString("Completed", done);
			break;
			
		case ScientificStudy.INVITATION_PHASE:
			response.appendLabelledDigit("Max Collaborators", study.getMaxCollaborators());
			response.appendTableHeading("Invitee", CommandHelper.PERSON_WIDTH, "Responded", "Accepted");
			
			Set<Person> c = study.getCollaborativeResearchers();
			for (Person person :  study.getInvitedResearchers()) {
				response.appendTableRow(person.getName(),
										(study.hasInvitedResearcherResponded(person) ? "Yes" : "No"),
										(c.contains(person) ? "Yes" : "No"));
			}
			break;
		
		case ScientificStudy.PAPER_PHASE:
		case ScientificStudy.RESEARCH_PHASE:
			displayCollaborators(response, study);			
			break;
			
		case ScientificStudy.PEER_REVIEW_PHASE:
			// Safe to cast because value will always be 100 or less
			String reviewDone = (int) Math.round((100D * study.getPeerReviewTimeCompleted())
								/ study.getTotalPeerReviewTimeRequired()) + "%";
			response.appendLabeledString("Completed", reviewDone);
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
									(airlock.isActivated() ? "Yes" : "No"),
									airlock.getOperatorName(),
									String.format("%d/%d", airlock.getNumOccupants(), airlock.getCapacity()),
									(airlock.isInnerDoorLocked() ? "LCK" : "ULK"),
									(airlock.isOuterDoorLocked() ? "LCK" : "ULK"));
		}
	}
	
	/**
	 * This generates the details of a mission.
	 * @param response Output destination
	 * @param mission Mission in question
	 */
	public static void outputMissionDetails(StructuredResponse response, Mission mission) {
		List<MissionMember> plist = new ArrayList<>(mission.getMembers());
		Person startingPerson = mission.getStartingPerson();
		plist.remove(startingPerson);
	
		double dist = 0;
		double trav = 0;
		Vehicle v = null;
		
		// Ohhh instanceof ???
		if (mission instanceof VehicleMission) {
			v = ((VehicleMission) mission).getVehicle();
			dist = Math.round(((VehicleMission) mission).getEstimatedTotalDistance() * 10.0) / 10.0;
			trav = Math.round(((VehicleMission) mission).getActualTotalDistanceTravelled() * 10.0) / 10.0;
		}
	
		if (v != null) {
			response.appendLabeledString("Vehicle", v.getName());
			response.appendLabeledString("Type", v.getVehicleType());
			response.appendLabeledString("Est. Dist.", dist + " km");
			response.appendLabeledString("Travelled", trav + " km");
		}
		response.appendLabeledString("Phase", mission.getPhaseDescription());
		MissionPlanning mp = mission.getPlan();
		if (mp != null) {
			response.appendLabeledString("Plan Status", mp.getStatus().getName());
		}
		response.appendLabeledString("Lead", startingPerson.getName());

		List<String> names = plist.stream().map(p -> p.getName()).sorted().collect(Collectors.toList());
		response.appendNumberedList("Members", names);
	
		// Travel mission has aroute
		if (mission instanceof TravelMission) {
			TravelMission tm = (TravelMission) mission;
			int navPoints = tm.getNumberOfNavpoints();
			if (navPoints > 0) {
				response.appendText("Itinerary:");
				response.appendTableHeading("Way Point", 19, "Description");
				for(int i = tm.getNextNavpointIndex(); i < navPoints; i++) {
					NavPoint nv = tm.getNavpoint(i);
					if (nv.isSettlementAtNavpoint()) {
						response.appendTableRow(nv.getSettlement().getName(), "");
					}
					else {
						response.appendTableRow(nv.getLocation().getCoordinateString(),
								nv.getDescription());
					}
				}
				response.appendBlankLine();				
			}
		}
		
		// Vehicle mission has a loading
		if (mission instanceof VehicleMission) {
			LoadingController lp = ((VehicleMission) mission).getLoadingPlan();
			if ((lp != null) && !lp.isCompleted()) {
				response.appendText("Loading from " + lp.getSettlement().getName() + " :");
				outputResources("Resources", response, lp.getResourcesManifest());	
				outputResources("Optional Resources", response, lp.getOptionalResourcesManifest());	
				outputEquipment("Equipment", response, lp.getEquipmentManifest());	
				outputEquipment("Optional Equipment", response, lp.getOptionalEquipmentManifest());	
			}
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
		response.appendLabelledDigit("Fixed %", (int)m.getPercentageFixed());
		
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
}
