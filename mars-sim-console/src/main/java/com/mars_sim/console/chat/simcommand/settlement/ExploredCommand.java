/*
 * Mars Simulation Project
 * ExploredCommand.java
 * @date 2025-07-06
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.settlement;

import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.environment.MineralSite;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.person.ai.mission.Mining;
import com.mars_sim.core.structure.Settlement;

/**
 * Command to display details of Explored Locations. The locations are filtered
 * depending on the Settlement
 */
public class ExploredCommand extends AbstractSettlementCommand {

	public static final ChatCommand EXPLORED = new ExploredCommand();
	private static final List<String> EXPLORE_OPTIONS = List.of("Partial", "Mature");

	private ExploredCommand() {
		super("ep", "explored", "Summary of explored locations");
	}

	
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		SurfaceFeatures surface = context.getSim().getSurfaceFeatures();


		displayExploredLocations(context, settlement, surface);

		// Add a new one
		if ("add".equals(input)) {
			addExploredLocation(context, settlement, surface);		
		}

		return true;
	}

	private void addExploredLocation(Conversation context, Settlement filter, SurfaceFeatures surface) {
		// Get location for centre of search
		Coordinates searchBase = filter.getCoordinates();
		context.println("Will base the search at " + filter.getName());
		var eMgr = filter.getExplorations();
		
		boolean addSite = true;
		while(addSite) {
			int searchRange = 50;

			var found = eMgr.acquireNearbyMineralLocation(searchRange);
			if (found != null) {
				context.println("Possible site found at " + found.getFormattedString());
				var newSite = eMgr.createROI(found, 5);

				if (newSite == null) {
					// THeory should be low due to use of acquire and createROI skill
					context.println("Site did not have enough minerals");
				}
				else if (context.getBooleanInput("Claim the new site for " + filter.getReportingAuthority().getName())) {
					eMgr.claimSite(newSite);

					//set Explored
					var explored = CommandHelper.getOptionInput(context, EXPLORE_OPTIONS, "Level of exploration");
					if (explored >= 0) {
						newSite.setExplored(true);
						if (explored == 1) {
							newSite.incrementNumImprovement(Mining.MATURE_ESTIMATE_NUM);
						}
					}
				}
			}
			else {
				context.println("No minerals within " + searchRange + "km of " + searchBase.getFormattedString());
			}

			addSite = context.getBooleanInput("Add Another site");
		}

		// Display new locations
		displayExploredLocations(context, filter, surface);
	}

	/**
	 * Displays a table of the explored location for this Unit.
	 * 
	 * @param context Context of conversation
	 * @param filter Optional filtered Settlement
	 * @param surface Mars Surface features
	 */
	private void displayExploredLocations(Conversation context, Settlement filter, SurfaceFeatures surface) {

			
		// Filter the list if in a Settlement
		final var sFilter = filter.getReportingAuthority();
		var locations = surface.getAllPossibleRegionOfInterestLocations().stream()
								.filter(s -> (s.getOwner() == null) || sFilter.equals(s.getOwner()))
								.toList();
		
		StructuredResponse response = new StructuredResponse();
		
		response.appendText("");
		response.appendText(" A total of " + locations.size() + " mineral sites have been identified.");
		response.appendText("");
		
		response.appendTableHeading("Location", CommandHelper.COORDINATE_WIDTH,
									"Authority", 18, 
									"Status*", "Reviews", "Mineral with highest %");
		for (MineralSite s : locations) {
			String mineral = "";

			// Create summary of minerals
			Optional<Entry<String, Double>> highest = s.getEstimatedMineralConcentrations().entrySet().stream()
								.max(Comparator.comparing(Entry::getValue));
			if (highest.isPresent())
				mineral = String.format("%s - %.1f", highest.get().getKey(), highest.get().getValue());
			
			String status = (s.isMinable() ? "M" : "-") +  (s.isExplored() ? "E" : "-") + (s.isClaimed() ? "C" : "-") +(s.isReserved() ? "R" : "-") + "  ";
			var owner = s.getOwner();
			response.appendTableRow(s.getLocation().getFormattedString(),
									(owner != null ? owner.getName() : ""),
									status,
									s.getNumEstimationImprovement(),
									mineral);
		}

		response.appendText(" Note *: 'M' = Minable, 'E' = Explored, 'C' = Claimed, 'R' = Reserved");
		
		context.println(response.getOutput());
	}
}
