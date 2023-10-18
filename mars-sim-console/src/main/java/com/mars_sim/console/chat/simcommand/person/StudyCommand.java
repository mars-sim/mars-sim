/*
 * Mars Simulation Project
 * StudyCommand.java
 * @date 2022-08-24
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.person;

import java.util.Set;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.science.ScientificStudy;

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
