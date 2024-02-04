/*
 * Mars Simulation Project
 * ExploredCommand.java
 * @date 2022-07-07
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.core.Unit;
import com.mars_sim.core.environment.ExploredLocation;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.mapdata.location.Coordinates;

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

	private ExploredCommand() {
		super(TopLevel.SIMULATION_GROUP, "ep", "explored", "Summary of explored locations");
	}

	@Override
	public boolean execute(Conversation context, String input) {
		SurfaceFeatures surface = context.getSim().getSurfaceFeatures();

		displayExploredLocations(context, surface);

		// Add a new one
		if ("add".equals(input)) {
			boolean addSite = true;
			while(addSite) {
				// Get location and check concentration
				Coordinates siteLocation = CommandHelper.getCoordinates("Site Location", context);

				// Check the location has minerals
				boolean hasMinerals = false;
				Map<String, Integer> minerals = surface.getMineralMap().getAllMineralConcentrations(siteLocation);
				for(int conc : minerals.values()) {
					hasMinerals = conc > 0D;
					if (hasMinerals) {
						break;
					}
 				}

				if (hasMinerals) {
					// Add new site but at maximum estimation improvement
					ExploredLocation newSite = surface.declareRegionOfInterest(siteLocation, 1, null);
					newSite.setExplored(true);
				}
				else {
					context.println("No minerals @ " + siteLocation.getFormattedString());
				}

				addSite = "Y".equalsIgnoreCase(context.getInput("Add Another site (Y/N)"));
			}

			// Display new locations
			displayExploredLocations(context, surface);
		}
		return true;
	}

	/**
	 * Displays a table of the explored location for this Unit.
	 * 
	 * @param context Context of conversation
	 * @param surface Mars Surface features
	 */
	private void displayExploredLocations(Conversation context, SurfaceFeatures surface) {

		Set<ExploredLocation> locations = surface.getAllRegionOfInterestLocations();
			
		// Filter the list if in a Settlement
		if (context.getCurrentCommand() instanceof ConnectedUnitCommand cuc) {
			Settlement filter = null;
			Unit source = cuc.getUnit();
			if (source instanceof Settlement s) {
				filter = s;
			}
			else {
				filter = source.getAssociatedSettlement();
			}
			
			// Filter to settlement
			final Settlement sFilter = filter;
			locations = locations.stream()
								.filter(s -> (s.getSettlement() == null) || sFilter.equals(s.getSettlement()))
								.collect(Collectors.toSet());
		}
		
		StructuredResponse response = new StructuredResponse();
		response.appendTableHeading("Location", CommandHelper.COORDINATE_WIDTH,
									"Settlement", 20, 
									"Status", 8 , "Reviews", "Highest");
		for (ExploredLocation s : locations) {
			String mineral = "";
			if (!s.getEstimatedMineralConcentrations().isEmpty()) {
				// Create summary of minerals
				Optional<Entry<String, Double>> highest = s.getEstimatedMineralConcentrations().entrySet().stream()
									.max(Comparator.comparing(Entry::getValue));
				if (highest.isPresent())
					mineral = String.format("%s - %.2f", highest.get().getKey(), highest.get().getValue());
				else {
					context.println("Invalid mineral concentrations. Try again later.");
				}
			}
			
			String status = (s.isMinable() ? "Minable" : (s.isReserved() ? "Reserved" : (s.isExplored() ? "Explored" : "")));
			Settlement owner = s.getSettlement();
			response.appendTableRow(s.getLocation().getFormattedString(),
									(owner != null ? owner.getName() : ""),
									status,
									s.getNumEstimationImprovement(),
									mineral);
		}

		context.println(response.getOutput());
	}
}
