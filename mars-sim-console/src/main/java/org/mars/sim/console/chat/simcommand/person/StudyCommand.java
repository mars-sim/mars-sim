package org.mars.sim.console.chat.simcommand.person;

import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyManager;

/** 
 * Displays any active Scientific Studies for this Person
 */
public class StudyCommand extends AbstractPersonCommand {
	public static final ChatCommand STUDY = new StudyCommand();
	
	private StudyCommand() {
		super("st", "study", "Study report");
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {
		ScientificStudy pristudy = person.getStudy();
		StructuredResponse response = new StructuredResponse();
		if (pristudy != null) {
			response.appendHeading("Primary Study");
			CommandHelper.outputStudy(response, pristudy);
		}
		ScientificStudyManager manager = context.getSim().getScientificStudyManager();
		
		List<ScientificStudy> studies = manager.getOngoingCollaborativeStudies(person);
		if (!studies.isEmpty()) {
			response.appendHeading("Collaborating Studies");
			for (ScientificStudy study : studies) {
				CommandHelper.outputStudy(response, study);
				response.append(System.lineSeparator());
			}
		}
		context.println(response.getOutput());
		return true;
	}
}
