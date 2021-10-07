/*
 * Mars Simulation Project
 * ScienceCommand.java
 * @Date 2021-10-05
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.settlement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.Conversion;

public class ScienceCommand extends AbstractSettlementCommand {
	public static final ChatCommand SCIENCE = new ScienceCommand();
	private static final List<String> NOTES = Arrays.asList("Succ   : # of Successfully Completed Research",
						"Fail   : # of Failed Research",
						"Canx   : # of Cancelled Research",
						"Prim   : # of Ongoing Primary Research",
						"Collab : # of Ongoing Collaborative Research",
						"Achiev : the settlement's achievement score on completed studies");

	private static final class ScienceScore {
		ScienceType type;
		double total;
		
		public ScienceScore(ScienceType scienceType, double subtotal) {
			this.type = scienceType;
			this.total = subtotal;
		}		
	}
	
	private ScienceCommand() {
		super("sc", "science", "Settlement science progress");
	}

	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {

		StructuredResponse response = new StructuredResponse();
		ScientificStudyManager scientificManager = context.getSim().getScientificStudyManager();

		List<ScienceScore> list = new ArrayList<>();
		double total = 0;
		List<ScienceType> sciences = Arrays.asList(ScienceType.values());
		for (ScienceType scienceType : sciences) {

			double score = scientificManager.getScienceScore(settlement, scienceType);
			double achievement = settlement.getScientificAchievement(scienceType);

			double subtotal = score + achievement;
			total += subtotal;
			list.add(new ScienceScore(scienceType, subtotal));
		}

		list.sort((ScienceScore d1, ScienceScore d2) -> (d1.total < d2.total ? 1 : (d1.total == d2.total ? 0 : -1)));
		response.appendTableHeading("Rank", 5, "Score", "Science", 14, "Succ", "Fail", "Canx",
				                    "Prim", "Collab", "Achiev");

		int rank = 1;
		for (ScienceScore item : list) {
			ScienceType t = item.type;
			String n = Conversion.capitalize(t.getName().toLowerCase());

			// 0 = succeed 	
			// 1 = failed
			// 2 = canceled
			// 3 = oPri
			// 4 = oCol
			int[] counts = scientificManager.getNumScienceStudy(settlement, t);
			double achieve = Math.round(10.0 *settlement.getScientificAchievement(t))/10.0;
			response.appendTableRow("#" + rank++, item.total, n, counts[0], counts[1], counts[2],
									counts[3], counts[4], achieve); 
		}

		response.appendSeperator();
		response.appendText(" Overall : " + Math.round(total * 10.0) / 10.0 + "\n");

		response.appendNumberedList("Notes", NOTES);

		context.println(response.getOutput());
		
		return true;
	}
}
