/*
 * Mars Simulation Project
 * DashboardCommand.java
 * @date 2022-07-15
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.settlement;

import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars.sim.mapdata.location.Coordinates;
import org.mars_sim.msp.core.environment.TerrainElevation;
import org.mars_sim.msp.core.goods.GoodsManager;
import org.mars_sim.msp.core.structure.Settlement;

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
	 * Outputs the current immediate location of the Unit.
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {

		StructuredResponse response = new StructuredResponse();
		generatedDashboard(settlement, response);
		
		context.println(response.getOutput());
		
		return true;
	}

	/**
	 * Generates a dashboard for a Settlement.
	 * 
	 * @param settlement
	 * @return
	 */
	void generatedDashboard(Settlement settlement, StructuredResponse response) {
		Coordinates location = settlement.getCoordinates();
		double elevationMOLA = Math.round(TerrainElevation.getMOLAElevation(location) * 1000.0)/1000.0;
//    	double elevationTopo = TerrainElevation.getRGBElevation(location);
		
		response.appendLabeledString("Sponsor", settlement.getReportingAuthority().getDescription());
		response.appendLabeledString("Objective", settlement.getObjective().getName());
		response.appendLabeledString("Location", location.getFormattedString());
		response.appendLabeledString("MOLA Elevation", elevationMOLA + " km");
//		response.appendLabeledString("Topo Elevation", elevationTopo + " km");
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
