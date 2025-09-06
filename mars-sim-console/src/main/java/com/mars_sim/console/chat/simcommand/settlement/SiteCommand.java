/*
 * Mars Simulation Project
 * SiteCommand.java
 * @date 2025-08-08
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.settlement;

import java.util.List;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.ConversationRole;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.building.BuildingSpec;
import com.mars_sim.core.building.construction.ConstructionManager;
import com.mars_sim.core.structure.Settlement;

/**
 * Command to display construction sites in a settlement
 */
public class SiteCommand extends AbstractSettlementCommand {

	public static final ChatCommand SITE = new SiteCommand();
	private static final int STAGE_WIDTH = 30;

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

		// Output Sites
		response.appendHeading("Sites");
		response.appendTableHeading("Name", 11,
						"Stage", 28, "Work", 6, "Build", "Active", "Left", "Building", -STAGE_WIDTH);
		for (var site : cmgr.getConstructionSites()) {
			var stage = site.getCurrentConstructionStage();
			response.appendTableRow(site.getName(), stage.getInfo().getName(),
						stage.getRequiredWorkTime(),
						site.isConstruction(), site.getWorkOnSite() != null,
						site.getRemainingPhases().size(), site.getBuildingName());
		}
		response.appendBlankLine();

		// Output queue
		response.appendHeading("Building Queue");
		response.appendTableHeading("Building", STAGE_WIDTH,"When", -CommandHelper.TIMESTAMP_TRUNCATED_WIDTH);
		for (var item : cmgr.getBuildingSchedule()) {
			String when = (item.isReady() ? "Now" : item.getStart().getDateTimeStamp());
			response.appendTableRow(item.getBuildingType(), when);
		}

		context.println(response.getOutput());
		
		if (context.getRoles().contains(ConversationRole.EXPERT) && context.getBooleanInput("Add Site")) {
			addSite(context, cmgr);
		}
		
		return true;
	}

	private void addSite(Conversation context, ConstructionManager cmgr) {
		var buildingName = context.getInput("Which building to build?");

		BuildingSpec building = context.getSim().getConfig().getBuildingConfiguration().getBuildingSpec(buildingName);
		if (building == null) {
			context.println("Cannot find a building called " + buildingName);
			return;
		}

		var choice = CommandHelper.getOptionInput(context, List.of("Site", "Queue"),
												"Create " + building.getName() + " as");
		if (choice == 0) {
			var newSite = cmgr.createNewBuildingSite(building);
			context.println("Added site " + newSite.getName());
		}
		else if (choice == 1) {
			cmgr.addBuildingToQueue(building.getName(), null);
			context.println("Added to queue");	
		}
	}
}
