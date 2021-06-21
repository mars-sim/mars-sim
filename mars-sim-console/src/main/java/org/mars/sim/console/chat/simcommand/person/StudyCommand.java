/**
 * Mars Simulation Project
 * StudyCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.person;

import java.util.Set;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.science.ScientificStudy;

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
		
		Set<ScientificStudy> studies = person.getCollabStudies();
		if (!studies.isEmpty()) {
			response.appendBlankLine();
			response.appendHeading("Collaborating Studies");
			for (ScientificStudy study : studies) {
				CommandHelper.outputStudy(response, study);
				response.appendBlankLine();
			}
		}
		context.println(response.getOutput());
		return true;
	}
}
