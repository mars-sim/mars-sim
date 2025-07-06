/*
 * Mars Simulation Project
 * ExploredCommand.java
 * @date 2022-07-07
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.core.Unit;
import com.mars_sim.core.environment.MineralSite;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.person.ai.mission.Mining;
import com.mars_sim.core.structure.Settlement;

/**
 * Command to display details of Explored Locations. The locations are filtered
 * depending on the context:
 * - Top - all locations
 * - Settlement - location of this Settlement
 * - Other Units - associated Settlement
 * This is a singleton.
 */
public class ExploredCommand extends ChatCommand {

	public static final ChatCommand EXPLORED = new ExploredCommand();
	private static final List<String> EXPLORE_OPTIONS = List.of("Partial", "Mature");

	private ExploredCommand() {
		super(TopLevel.SIMULATION_GROUP, "ep", "explored", "Summary of explored locations");
	}

	@Override
	public boolean execute(Conversation context, String input) {
		SurfaceFeatures surface = context.getSim().getSurfaceFeatures();

		// Filter the list if in a Settlement
		Settlement filter = null;
		if (context.getCurrentCommand() instanceof ConnectedUnitCommand cuc) {
			Unit source = cuc.getUnit();
			if (source instanceof Settlement s) {
				filter = s;
			}
			else {
				filter = source.getAssociatedSettlement();
			}
		}

		displayExploredLocations(context, filter, surface);

		// Add a new one
		if ("add".equals(input)) {
			addExploredLocation(context, filter, surface);		
		}

		return true;
	}

	private void addExploredLocation(Conversation context, Settlement filter, SurfaceFeatures surface) {

		boolean addSite = true;
		while(addSite) {
			// Get location and check concentration
			Coordinates searchBase = null;
			if (filter != null) {
				searchBase = filter.getCoordinates();
				context.println("Will base the search at " + filter.getName());
			}
			else {
				searchBase = CommandHelper.getCoordinates("Set a search Location", context);
			}

			int searchRange = 100;

			// Check the location has minerals
			var found = surface.getMineralMap().findRandomMineralLocation(searchBase, searchRange, Collections.emptyList());

			if (found != null) {
				var newLocn = found.getKey();
				context.println("Possible site found at " + newLocn.getFormattedString() + " distance of " + found.getValue());
				MineralSite newSite = surface.declareRegionOfInterest(newLocn, 1);

				if (newSite == null) {
					context.println("Site did not have engouh monerals");
				}
				else if (context.getBooleanInput("Claim the new site")) {
					newSite.setClaimed(filter);

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

		var locations = surface.getAllPossibleRegionOfInterestLocations();
			
		// Filter the list if in a Settlement
		if (filter != null) {
			// Filter to settlement
			final Settlement sFilter = filter;
			locations = locations.stream()
								.filter(s -> (s.getSettlement() == null) || sFilter.equals(s.getSettlement()))
								.toList();
		}
		
		StructuredResponse response = new StructuredResponse();
		response.appendTableHeading("Location", CommandHelper.COORDINATE_WIDTH,
									"Settlement", 20, 
									"Status *", 8 , "Reviews", "Highest");
		for (MineralSite s : locations) {
			String mineral = "";

			// Create summary of minerals
			Optional<Entry<String, Double>> highest = s.getEstimatedMineralConcentrations().entrySet().stream()
								.max(Comparator.comparing(Entry::getValue));
			if (highest.isPresent())
				mineral = String.format("%s - %.2f", highest.get().getKey(), highest.get().getValue());
			
			String status = (s.isMinable() ? "M" : "-") + (s.isReserved() ? "R" : "-") + (s.isExplored() ? "E" : "-");
			Settlement owner = s.getSettlement();
			response.appendTableRow(s.getLocation().getFormattedString(),
									(owner != null ? owner.getName() : ""),
									status,
									s.getNumEstimationImprovement(),
									mineral);
		}

		response.appendText("* - 'M' = Minable, 'R' = Reserved, 'E' = Explored");
		context.println(response.getOutput());
	}
}
