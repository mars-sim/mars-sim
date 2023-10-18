/**
 * Mars Simulation Project
 * StudyCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.settlement;

import java.util.List;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.ScientificStudyManager;
import com.mars_sim.core.structure.Settlement;

/**
 * Command to display Studies in a Settlement
 * This is a singleton.
 */
public class StudyCommand extends AbstractSettlementCommand {

	public static final ChatCommand STUDY = new StudyCommand();

	private StudyCommand() {
		super("ss", "study", "Settlement Scientific Studies");
	}

	/** 
	 * Output the 
	 * @return 
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		ScientificStudyManager manager = context.getSim().getScientificStudyManager();
		
		List<ScientificStudy> studies = manager.getAllStudies(settlement);
		
		StructuredResponse response = new StructuredResponse();
		
		for (ScientificStudy study : studies) {
			CommandHelper.outputStudy(response, study);
			response.appendBlankLine();
		}
		
		context.println(response.getOutput());
		
		return true;
	}


}
