package org.mars.sim.console.chat.simcommand.settlement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.Settlement;

public class ResearcherCommand extends AbstractSettlementCommand {

	public static final ChatCommand RESEARCHER = new ResearcherCommand();

	private ResearcherCommand() {
		super("re", "research", "Settlement researchers");
	}
	
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();
		
		List<Person> people = new ArrayList<>(settlement.getAllAssociatedPeople());

		List<ScienceType> sciences = Arrays.asList(ScienceType.values());			

		for (Person p : people) {
			response.appendBlankLine();
			response.appendHeading(p.getName() + " - " + p.getMind().getJob().getName());

			ScientificStudy ss = p.getStudy();
			String priName = "None";
			String priPhase = "";
			if (ss != null) {
				priName = ss.getScience().getName();
				priPhase = ss.getPhase();
			}
			response.appendLabeledString("Ongoing Primary Study", priName + " - " + priPhase);

			Set<ScientificStudy> cols = p.getCollabStudies();
			if (!cols.isEmpty()) {
				response.appendBlankLine();
				response.appendTableHeading("Collaborative Study", 22, "Phase");
				for (ScientificStudy item : cols) {
					String secName = item.getScience().getName();
					String secPhase = item.getPhase();
					response.appendTableRow(secName, secPhase);
				}
			}
			
			response.appendBlankLine();
			response.appendTableHeading("Subject", 15, "Achievement Score");

			for (ScienceType t : sciences) {
				double score = p.getScientificAchievement(t);
				response.appendTableRow(t.getName(), score);
			}
		}
		context.println(response.getOutput());
		return true;
	}

}
