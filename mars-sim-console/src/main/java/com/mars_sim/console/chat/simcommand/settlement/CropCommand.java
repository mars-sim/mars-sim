/*
 * Mars Simulation Project
 * CropCommand.java
 * @date 2023-11-22
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.settlement;

import java.util.List;
import java.util.stream.Collectors;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.farming.Crop;
import com.mars_sim.core.building.function.farming.Farming;
import com.mars_sim.core.structure.Settlement;

/**
 * Command to display crop status.
 */
public class CropCommand extends AbstractSettlementCommand {

	private static final int CROP_PHASE_WIDTH = 22;
	private static final int CROP_WIDTH = 17;
	public static final ChatCommand CROP = new CropCommand();

	private CropCommand() {
		super("cr", "crop", "Status of crops");
	}

	/** 
	 * Outputs the current immediate location of the Unit.
	 * 
	 * @return 
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();

		List<Farming> farms = settlement.getBuildingManager().getBuildings(FunctionType.FARMING)
				 					.stream().map(Building::getFarming)
									.collect(Collectors.toList());
		// Display each farm separately
		for (Farming farm : farms) {			
			response.append(farm.getBuilding().getName());
			response.appendBlankLine();
			if (farm.getNumCrops2Plant() > 0) {
				response.appendLabelledDigit("Available Crop Spaces", farm.getNumCrops2Plant());
			}
			
			response.appendTableHeading("Crop", CROP_WIDTH, "Type", 7, "Health %",
									"Phase", CROP_PHASE_WIDTH, "Grown %", "Harvest");
			List<Crop> crops = farm.getCrops().stream().sorted().collect(Collectors.toList());
			for (Crop crop : crops) {
				response.appendTableRow(crop.getName(), crop.getCropSpec().getCropCategory().getName(),
										crop.getHealthCondition()*100D,
										crop.getPhase().getName(), crop.getPercentGrowth(),
										crop.getHarvest());
				
			}
			response.appendBlankLine();

		}
		context.println(response.getOutput());
		return true;
	}

}
