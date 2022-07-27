/*
 * Mars Simulation Project
 * ResourceCommand.java
 * @date 2022-07-15
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.settlement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.WaterUseType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.core.structure.building.function.WasteProcess;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;

public class ResourceCommand extends AbstractSettlementCommand {
	private static final String PROJECTED_DAILY_CONSUMED = "Projected daily consumed";

	private static final String TOTAL_GROWING_AREA = "Total growing area";

	private static final String PROCESSES = "Processes";
	
	private static final String WASTES = "Wastes";

	private static final String CONSUMED_DAILY_PER_M2 = "Consumed daily per m2";

	private static final String TOTAL_AMOUNT_CONSUMED_DAILY = "Total consumed daily";

	private static final String CURRENT_RESERVE = "Current reserve";

	private static final String O2_FARMING	= "         Oxygen Generation from Farming";
	private static final String H2O_FARMING	= "         Water Consumption from Farming";
	private static final String CO2_FARMING	= "     Carbon Dioxide Consumption from Farming";

	
	private static final String KG_M2_SOL_FORMAT = "%8.2f kg/m^2/sol";

	private static final String KG_SOL_FORMAT = "%8.2f kg/sol";

	private static final String M2_FORMAT = "%8.2f m^2";
	
	public static final ChatCommand RESOURCE = new ResourceCommand();
	
	private static final String OXYGEN = "o2";
	private static final String CO2 = "co2";
	private static final String WATER = "water";
	private static final String GREY_WATER = "grey water";


	private ResourceCommand() {
		super("rs", "resource", "Settlement resources: either oxygen, co2, water, or grey water");
		
		// Setup the fixed arguments
		setArguments(Arrays.asList(OXYGEN, CO2, WATER, GREY_WATER));
	}
	
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		boolean result = false;
		if (input == null || input.isEmpty()) {
			context.println("Must enter a resource type " + getArguments(context));
		}
		else {
			StructuredResponse response = new StructuredResponse();
			String subCommand = input.trim().toLowerCase();

			switch (subCommand) {
			case OXYGEN:
				displayOxygen(settlement, response);
				result = true;
				break;
			
			case WATER:
				displayWater(settlement, response, ResourceUtil.waterID);
				result = true;
				break;
				
			case GREY_WATER:
				displayWater(settlement, response, ResourceUtil.greyWaterID);
				result = true;
				break;
				
			case CO2:
				displayCO2(settlement, response);
				result = true;
				break;

			default:
				response.append("Sorry don't know about resource " + subCommand);
				break;
			}
			
			context.println(response.getOutput());
		}
		return result;
	}

	private void displayCO2(Settlement settlement, StructuredResponse response) {
		double usage = 0;
		double totalArea = 0;
		double reserve = settlement.getAmountResourceStored(ResourceUtil.co2ID);

		response.appendHeading(CO2_FARMING);
		response.appendLabeledString(CURRENT_RESERVE, String.format(CommandHelper.KG_FORMAT, reserve));

		// Prints greenhouse usage
		List<Building> farms = settlement.getBuildingManager().getBuildings(FunctionType.FARMING);
		for (Building b : farms) {
			Farming f = b.getFarming();
			usage += f.computeUsage(2);
			totalArea += f.getGrowingArea();
		}
		totalArea = (totalArea != 0 ? totalArea: 0.1D); // Guard against divide by zero

		response.appendLabeledString(TOTAL_GROWING_AREA, String.format(M2_FORMAT, totalArea));
		response.appendLabeledString("Generated daily per m2", String.format(KG_M2_SOL_FORMAT,
																			(usage / totalArea)));
		response.appendLabeledString("Total generated daily", String.format(KG_SOL_FORMAT, usage));		
	}

	private void displayWater(Settlement settlement, StructuredResponse response, int id) {
		double reserve = settlement.getAmountResourceStored(id);
		response.appendLabeledString(CURRENT_RESERVE, String.format(CommandHelper.KG_FORMAT, reserve));
		response.appendBlankLine();
			
		double usage = 0;
		double totalArea = 0;
		int type = 0;
		double sign = -1.0;
		if (id == ResourceUtil.greyWaterID) {
			type = 3;
			
		}
		
		// Prints greenhouse usage
		List<Building> farms = settlement.getBuildingManager().getBuildings(FunctionType.FARMING);
		for (Building b : farms) {
			Farming f = b.getFarming();
			usage += f.computeUsage(type);
			totalArea += f.getGrowingArea();
		}

		response.appendHeading(H2O_FARMING); 
		response.appendLabeledString(TOTAL_GROWING_AREA, String.format(M2_FORMAT, totalArea));
		if (totalArea > 0) {
			response.appendLabeledString(CONSUMED_DAILY_PER_M2,	String.format(KG_M2_SOL_FORMAT, (usage / totalArea)));
		}
		response.appendLabeledString(PROJECTED_DAILY_CONSUMED, String.format(KG_SOL_FORMAT, usage));

		response.appendBlankLine();

		double net = 0;
		double greenhouseUsage = 0;
		
		// Prints greenhouse usage
		for (Building b : farms) {
			Farming f = b.getFarming();
			greenhouseUsage += f.getDailyAverageWaterUsage();
		}
		
		response.appendTableHeading("Area", 16, "Consumption (kg/sol)");
		response.appendTableRow("Greenhouse", Math.round(-greenhouseUsage * 100.0) / 100.0);

		net = net - greenhouseUsage;

		// Prints consumption
		double consumption = 0;
		List<Person> ppl = new ArrayList<>(settlement.getAllAssociatedPeople());
		for (Person p : ppl) {
			consumption += p.getDailyUsage(id);
		}
		response.appendTableRow("People", Math.round(- sign * consumption * 100.0) / 100.0);
		net = net - sign * consumption;

		// Add water usage from making meal and dessert
		double cooking = settlement.getDailyWaterUsage(WaterUseType.PREP_MEAL)
					+ settlement.getDailyWaterUsage(WaterUseType.PREP_DESSERT);
		response.appendTableRow("Cooking", Math.round(- sign * cooking * 100.0) / 100.0);
		net = net - sign * cooking;

		// Prints living usage
		List<Building> quarters = settlement.getBuildingManager()
				.getBuildings(FunctionType.LIVING_ACCOMMODATIONS);
		double livingUsage = 0;
		for (Building b : quarters) {
			LivingAccommodations la = b.getLivingAccommodations();
			livingUsage += la.getDailyAverageWaterUsage();
		}		
		response.appendTableRow("Accommodation", Math.round(- sign * livingUsage * 100.0) / 100.0);
		net = net - sign * livingUsage;

		// Prints cleaning usage
		double cleaning = settlement.getDailyWaterUsage(WaterUseType.CLEAN_MEAL)
					+ settlement.getDailyWaterUsage(WaterUseType.CLEAN_DESSERT);
		response.appendTableRow("Cleaning", Math.round(- sign * cleaning * 100.0) / 100.0);
		net = net - sign * cleaning;

		// Prints output from resource processing
		double output = 0;
		List<Building> bldgs = settlement.getBuildingManager().getBuildings(FunctionType.RESOURCE_PROCESSING);
		for (Building b : bldgs) {
			ResourceProcessing rp = b.getResourceProcessing();
			List<ResourceProcess> processes = rp.getProcesses();
			for (ResourceProcess p : processes) {
				if (p.isProcessRunning())
					output += p.getMaxOutputRate(id);
			}
		}
		response.appendTableRow(PROCESSES, Math.round(output * 1_000 * 100.0) / 100.0);
		net = net + output * 1_000;
		
		// Prints output from waste processing
		double output2 = 0;
		for (Building b : settlement.getBuildingManager().getBuildings(FunctionType.WASTE_PROCESSING)) {
			for (WasteProcess p : b.getWasteProcessing().getProcesses()) {
				if (p.isProcessRunning())
					output2 += p.getMaxOutputRate(id);
			}
		}
		response.appendTableRow(WASTES, Math.round(output2 * 1_000 * 100.0) / 100.0);
		net = net + output2 * 1_000;

		response.appendTableRow("NET", Math.round(net * 100.0) / 100.0);		
	}

	private void displayOxygen(Settlement settlement, StructuredResponse response) {
		double usage = 0;
		double totalArea = 0;
		double reserve = settlement.getAmountResourceStored(ResourceUtil.oxygenID);
		
		response.appendHeading(O2_FARMING);
		response.appendLabeledString(CURRENT_RESERVE, String.format(CommandHelper.KG_FORMAT, reserve));

		// Prints greenhouse usage
		List<Building> farms = settlement.getBuildingManager().getBuildings(FunctionType.FARMING);
		for (Building b : farms) {
			Farming f = b.getFarming();
			usage += f.computeUsage(1);
			totalArea += f.getGrowingArea();
		}

		response.appendLabeledString(TOTAL_GROWING_AREA, String.format(M2_FORMAT, totalArea));
		if (totalArea > 0) {
			response.appendLabeledString(CONSUMED_DAILY_PER_M2,
										String.format(KG_M2_SOL_FORMAT, (usage / totalArea)));
		}
		response.appendLabeledString(TOTAL_AMOUNT_CONSUMED_DAILY, String.format(KG_SOL_FORMAT, usage));
	}
}
