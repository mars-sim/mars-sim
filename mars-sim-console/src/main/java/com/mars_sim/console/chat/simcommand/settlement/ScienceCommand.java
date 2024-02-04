/*
 * Mars Simulation Project
 * ScienceCommand.java
 * @Date 2021-10-05
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.settlement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudyManager;
import com.mars_sim.core.structure.Settlement;

public class ScienceCommand extends AbstractSettlementCommand {
	public static final ChatCommand SCIENCE = new ScienceCommand();
	private static final List<String> NOTES = Arrays.asList("Succ   : # of Successfully Completed Research",
						"Fail   : # of Failed Research",
						"Canx   : # of Cancelled Research",
						"Prim   : # of Ongoing Primary Research",
						"Collab : # of Ongoing Collaborative Research",
						"Achiev : the settlement's achievement score on completed studies");

	private static record ScienceScore (ScienceType scienceType, double subtotal)
			implements Comparable<ScienceScore> {

		@Override
		public int compareTo(ScienceScore o) {
			return Double.compare(subtotal, o.subtotal);
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

		Collections.sort(list);
		response.appendTableHeading("Rank", 5, "Score", "Science", 14, "Succ", "Fail", "Canx",
				                    "Prim", "Collab", "Achiev");

		int rank = 1;
		for (ScienceScore item : list) {
			ScienceType t = item.scienceType();
			String n = t.getName();

			// 0 = succeed 	
			// 1 = failed
			// 2 = canceled
			// 3 = oPri
			// 4 = oCol
			int[] counts = scientificManager.getNumScienceStudy(settlement, t);
			double achieve = Math.round(10.0 *settlement.getScientificAchievement(t))/10.0;
			response.appendTableRow("#" + rank++, item.subtotal, n, counts[0], counts[1], counts[2],
									counts[3], counts[4], achieve); 
		}

		response.appendSeperator();
		response.appendText(" Overall : " + Math.round(total * 10.0) / 10.0 + "\n");

		response.appendNumberedList("Notes", NOTES);

		context.println(response.getOutput());
		
		return true;
	}
}
