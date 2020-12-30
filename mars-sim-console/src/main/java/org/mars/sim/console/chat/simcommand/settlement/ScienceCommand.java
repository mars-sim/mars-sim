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

		list.sort((ScienceScore d1, ScienceScore d2) -> (int)(d1.total - d2.total));
		response.appendTableHeading("Rank", 5, "Score", "Science", 14, "Succ", "Fail", "Cenx",
				                    "Prim", "Collab", "Achiev");

		int rank = 1;
		for (ScienceScore item : list) {
			ScienceType t = item.type;
			String n = Conversion.capitalize(t.getName().toLowerCase());

			int suc = scientificManager.getAllSuccessfulStudies(settlement, t).size();
			int fail = scientificManager.getAllFailedStudies(settlement, t).size();
			int canx = scientificManager.getAllCanceledStudies(settlement, t).size();
			int oPri = scientificManager.getOngoingPrimaryStudies(settlement, t).size();
			int oCol = scientificManager.getOngoingCollaborativeStudies(settlement, t).size();
			double achieve = Math.round(10.0 *settlement.getScientificAchievement(t))/10.0;
			response.appendTableRow("#" + rank++, item.total, n, suc, fail, canx, oPri, oCol, achieve); 
		}

		response.appendSeperator();
		response.append(" Overall : " + Math.round(total * 10.0) / 10.0 + "\n");
		response.append("Notes:\n");
		response.append("1. Succ   : # of Successfully Completed Research\n");
		response.append("2. Fail   : # of Failed Research\n");
		response.append("3. Canx   : # of Cancelled Research\n");
		response.append("4. Prim   : # of Ongoing Primary Research\n");
		response.append("5. Collab : # of Ongoing Collaborative Research\n");
		response.append("6. Achiev : the settlement's achievement score on completed studies\n");
		
		context.println(response.getOutput());
		
		return true;
	}
}
