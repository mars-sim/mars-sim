/**
 * Mars Simulation Project
 * DashboardCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.settlement;

import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.goods.GoodsManager;

/**
 * Command to display dashboard for this settlement
 * This is a singleton.
 */
public class DashboardCommand extends AbstractSettlementCommand {

	public static final DashboardCommand DASHBOARD = new DashboardCommand();
	
	private DashboardCommand() {
		super("d", "dashboard", "Dashboard of the settlement");
	}

	/** 
	 * Output the current immediate location of the Unit
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {

		StructuredResponse response = new StructuredResponse();
		generatedDashboard(settlement, response);
		
		context.println(response.getOutput());
		
		return true;
	}

	/**
	 * Generate a dashboard for a Settlement.
	 * @param settlement
	 * @return
	 */
	void generatedDashboard(Settlement settlement, StructuredResponse response) {

		response.appendLabeledString("Sponsor", settlement.getSponsor());
		response.appendLabeledString("Objective", settlement.getObjective().getName());
		response.appendLabeledString("Location", settlement.getCoordinates().getCoordinateString());
		response.appendLabelledDigit("Population", settlement.getNumCitizens());	
				
		String[] cats = new String[] { "Repair", "Maintenance", "EVA Suit Production" };

		GoodsManager goodsManager = settlement.getGoodsManager();

		int[] levels = new int[] { goodsManager.getRepairLevel(), goodsManager.getMaintenanceLevel(),
				goodsManager.getEVASuitLevel() };
		
		response.appendBlankLine();
		response.appendTableHeading("Area", 22, "Level");

		for (int i=0; i<3; i++) {
			response.appendTableRow(cats[i], levels[i]);
		}
	}
}
