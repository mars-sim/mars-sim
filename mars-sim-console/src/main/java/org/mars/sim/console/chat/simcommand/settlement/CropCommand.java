/**
 * Mars Simulation Project
 * CropCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

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

	private static final int CROP_PHASE_WIDTH = 22;
	private static final int CROP_WIDTH = 17;
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
			response.append(farm.getBuilding().getName());
			response.appendBlankLine();
			if (farm.getNumCrops2Plant() > 0) {
				response.appendLabelledDigit("Available Crop Spaces", farm.getNumCrops2Plant());
			}
			
			response.appendTableHeading("Crop", CROP_WIDTH, "Type", 7, "Health %",
									"Phase", CROP_PHASE_WIDTH, "Grown %", "Harvest");
			List<Crop> crops = farm.getCrops().stream().sorted().collect(Collectors.toList());
			for (Crop crop : crops) {
				response.appendTableRow(crop.getCropName(), crop.getCropType().getCropCategoryType().getName(),
										crop.getHealthCondition()*100D,
										crop.getPhaseType().getName(), crop.getPercentGrowth(),
										crop.getMaxHarvest());
				
			}
			response.appendBlankLine();

		}
		context.println(response.getOutput());
		return true;
	}

}
