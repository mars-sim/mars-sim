package org.mars.sim.console.chat.simcommand.settlement;

import java.util.List;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.farming.Crop;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;

/**
 * Command to display settlement crop
 * This is a singleton.
 */
public class CropCommand extends AbstractSettlementCommand {

	public static final ChatCommand CROP = new CropCommand();

	private CropCommand() {
		super("cr", "crop", "Status of crops");
	}

	/** 
	 * Output the current immediate location of the Unit
	 * @return 
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();

		List<Farming> farms = settlement.getBuildingManager().getBuildings(FunctionType.FARMING)
				 					.stream().map(Building::getFarming)
									.collect(Collectors.toList());
		// Display each farm seperately
		for (Farming farm : farms) {
			response.append(farm.getBuilding().getNickName());
			response.appendBlankLine();

			response.appendTableHeading("Crop", 14, "Health %",
									"Phase", 22, "Grown %", "Harvest");
			List<Crop> crops = farm.getCrops().stream().sorted().collect(Collectors.toList());
			for (Crop crop : crops) {
				response.appendTableRow(crop.getCropName(), crop.getHealthCondition()*100D,
										crop.getPhaseType().getName(), crop.getPercentGrowth(),
										crop.getMaxHarvest());
				
			}
			response.appendBlankLine();

		}
		context.println(response.getOutput());
		return true;
	}

}
