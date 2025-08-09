/*
 * Mars Simulation Project
 * SiteCommand.java
 * @date 2025-08-08
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.settlement;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.building.BuildingSpec;
import com.mars_sim.core.building.construction.ConstructionManager;
import com.mars_sim.core.structure.Settlement;

/**
 * Command to display construction sites in a settlement
 */
public class SiteCommand extends AbstractSettlementCommand {

	public static final ChatCommand SITE = new SiteCommand();

	private SiteCommand() {
		super("si", "sites", "Construction sites");
	}

	/** 
	 * Outputs the details of the robots.
	 * 
	 * @return 
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {

		StructuredResponse response = new StructuredResponse();

		var cmgr = settlement.getConstructionManager();

		response.appendTableHeading("Name", 10,
						"Stage", 28, "%", 3, "Build", "Active", "Building");
		for (var site : cmgr.getConstructionSites()) {
			var stage = site.getCurrentConstructionStage();
			response.appendTableRow(site.getName(), stage.getInfo().getName(),
						Math.round(stage.getCompletedPerc() * 100),
						site.isConstruction(), site.isWorkOnSite(), site.getBuildingName());
		}
		
		context.println(response.getOutput());
		
		if (context.getBooleanInput("Add Site")) {
			addSite(context, cmgr);
		}

		return true;
	}

	private void addSite(Conversation context, ConstructionManager cmgr) {
		var buildingName = context.getInput("Which building to build?");

		BuildingSpec building = context.getSim().getConfig().getBuildingConfiguration().getBuildingSpec(buildingName);
		if (building == null) {
			context.println("Cannot find a building called " + buildingName);
		}
		var newSite = cmgr.createNewBuildingSite(building);

		context.println("Added site " + newSite.getName());
	}

}
