package org.mars.sim.console.chat.simcommand.settlement;

import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Command to display Studies in a Settlement
 * This is a singleton.
 */
public class StudyCommand extends AbstractSettlementCommand {

	public static final ChatCommand STUDY = new StudyCommand();

	private StudyCommand() {
		super("st", "study", "Settlement Science studies");
	}

	/** 
	 * Output the 
	 * @return 
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		ScientificStudyManager manager = context.getSim().getScientificStudyManager();
		
		// TODO this should come off the Settlement object
		List<ScientificStudy> studies = manager.getAllStudies(settlement);
		
		StructuredResponse response = new StructuredResponse();
		
		for (ScientificStudy study : studies) {
			CommandHelper.outputStudy(response, study);
			response.append(System.lineSeparator());
		}
		
		context.println(response.getOutput());
		
		return true;
	}


}
