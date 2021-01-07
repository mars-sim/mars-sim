package org.mars.sim.console.chat.simcommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.ChatCommand;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Helper class with common formatting methods.
 */
public class CommandHelper {
	private CommandHelper() {
		// Do nothing
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
			response.appendTableHeading("Invitee", ChatCommand.PERSON_WIDTH, "Responded", "Accepted");
			
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
		
		response.append("Researchers");
		response.append(System.lineSeparator());
		response.appendTableHeading("Reseacher", ChatCommand.PERSON_WIDTH, "Contribution",
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
	 * This generates the details of a mission.
	 * @param response Output destination
	 * @param mission Mission in question
	 */
	public static void outputMissionDetails(StructuredResponse response, Mission mission) {
		List<MissionMember> plist = new ArrayList<>(mission.getMembers());
		Person startingPerson = mission.getStartingMember();
		plist.remove(startingPerson);
	
		double dist = 0;
		double trav = 0;
		Vehicle v = null;
		
		// Ohhh instanceof ???
		if (mission instanceof VehicleMission) {
			v = ((VehicleMission) mission).getVehicle();
			dist = Math.round(((VehicleMission) mission).getProposedRouteTotalDistance() * 10.0) / 10.0;
			trav = Math.round(((VehicleMission) mission).getActualTotalDistanceTravelled() * 10.0) / 10.0;
		}
	
		if (v != null) {
			response.appendLabeledString("Vehicle", v.getName());
			response.appendLabeledString("Type", v.getVehicleType());
			response.appendLabeledString("Est. Dist.", dist + " km");
			response.appendLabeledString("Travelled", trav + " km");
		}
		response.appendLabeledString("Phase", mission.getPhaseDescription());
		response.appendLabeledString("Lead", startingPerson.getName());
		response.append("Members:");
		response.append(System.lineSeparator());
		List<String> names = plist.stream().map(p -> p.getName()).sorted().collect(Collectors.toList());
		response.appendNumberedList(names);
	}
}
