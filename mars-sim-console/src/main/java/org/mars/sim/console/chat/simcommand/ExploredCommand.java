/*
 * Mars Simulation Project
 * ExploredCommand.java
 * @date 2022-07-07
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand;

import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.environment.ExploredLocation;
import org.mars_sim.msp.core.environment.SurfaceFeatures;
import org.mars_sim.msp.core.structure.Settlement;

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
		SurfaceFeatures surface = context.getSim().getMars().getSurfaceFeatures();
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
								 .filter(s -> s.getSettlement().equals(sFilter))
								 .collect(Collectors.toList());
		}
		
		StructuredResponse response = new StructuredResponse();
		response.appendTableHeading("Location", 18,
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
		        	return false;
		        }
			}
			
			String status = (s.isMined() ? "Mined" : (s.isReserved() ? "Reserved" : (s.isExplored() ? "Explored" : "")));
			response.appendTableRow(s.getLocation().getFormattedString(),
					                s.getSettlement().getName(),
					                status,
									s.getNumEstimationImprovement(),
									mineral);
		}

		context.println(response.getOutput());
		return true;
	}
}
