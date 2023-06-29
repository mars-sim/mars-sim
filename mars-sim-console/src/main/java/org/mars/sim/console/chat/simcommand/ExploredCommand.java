/*
 * Mars Simulation Project
 * ExploredCommand.java
 * @date 2022-07-07
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.environment.ExploredLocation;
import org.mars_sim.msp.core.environment.SurfaceFeatures;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.RandomUtil;

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
					ExploredLocation newSite = surface.addExploredLocation(siteLocation, 1, null);
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

		List<ExploredLocation> locations = surface.getExploredLocations();
			
		// Filter the list if in a Settlement
		if (context.getCurrentCommand() instanceof ConnectedUnitCommand) {
			Settlement filter = null;
			Unit source = ((ConnectedUnitCommand)context.getCurrentCommand()).getUnit();
			if (source instanceof Settlement) {
				filter = (Settlement) source;
			}
			else {
				filter = source.getAssociatedSettlement();
			}
			
			// Filter to settlement
			final Settlement sFilter = filter;
			locations = locations.stream()
								.filter(s -> (s.getSettlement() == null) || sFilter.equals(s.getSettlement()))
								.collect(Collectors.toList());
		}
		
		StructuredResponse response = new StructuredResponse();
		response.appendTableHeading("Location", CommandHelper.COORDINATE_WIDTH,
									"Settlement", 20, 
									"Status", 8 , "Reviews", "Highest");
		for(ExploredLocation s : locations) {
			String mineral = "";
			if (!s.getEstimatedMineralConcentrations().isEmpty()) {
				// Create summary of minerals
				Optional<Entry<String, Double>> highest = s.getEstimatedMineralConcentrations().entrySet().stream()
									.max(Comparator.comparing(v -> v.getValue()));
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
