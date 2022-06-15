/*
 * Mars Simulation Project
 * ResourceCommand.java
 * @date 2021-10-21
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
import org.mars_sim.msp.core.structure.building.function.WasteProcessing;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;

public class ResourceCommand extends AbstractSettlementCommand {
	private static final String PROJECTED_DAILY_CONSUMED = "Projected daily consumed";

	private static final String TOTAL_GROWING_AREA = "Total growing area";

	private static final String PROCESSES = "Processes";
	
	private static final String WASTES = "Wastes";

	private static final String CONSUMED_DAILY_PER_M2 = "Consumed daily per m2";

	private static final String TOTAL_AMOUNT_CONSUMED_DAILY = "Total consumed daily";

	private static final String CURRENT_RESERVE = "Current reserve";

	private static final String GREENHOUSE_FARMING = "Greenhouse Farming";
	
	private static final String WHITESPACES = "         ";
	
	private static final String KG_M2_SOL_FORMAT = "%0.2f kg/m^2/sol";

	private static final String KG_SOL_FORMAT = "%0.2f kg/sol";

	private static final String M2_FORMAT = "%0.2f m^2";
	
	public static final ChatCommand RESOURCE = new ResourceCommand();
	
	private static final String OXYGEN = "o2";
	private static final String CO2 = "co2";
	private static final String WATER = "water";


	private ResourceCommand() {
		super("rs", "resource", "Settlement resources; either oxygen, co2 or water");
		
		setIntroduction(" Display the Resources held");
		
		// Setup the fixed arguments
		setArguments(Arrays.asList(OXYGEN, CO2, WATER));
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
				displayWater(settlement, response);
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

		response.appendHeading(WHITESPACES + "CO2 Usage & " + GREENHOUSE_FARMING);
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

	private void displayWater(Settlement settlement, StructuredResponse response) {
		double reserve = settlement.getAmountResourceStored(ResourceUtil.waterID);
		response.appendLabeledString(CURRENT_RESERVE, String.format(CommandHelper.KG_FORMAT, reserve));
		response.appendBlankLine();
		
		double usage = 0;
		double totalArea = 0;

		// Prints greenhouse usage
		List<Building> farms = settlement.getBuildingManager().getBuildings(FunctionType.FARMING);
		for (Building b : farms) {
			Farming f = b.getFarming();
			usage += f.computeUsage(0);
			totalArea += f.getGrowingArea();
		}

		response.appendHeading(WHITESPACES + "Water Usage & " + GREENHOUSE_FARMING);
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
			consumption += p.getDailyUsage(ResourceUtil.waterID);
		}
		response.appendTableRow("People", Math.round(-consumption * 100.0) / 100.0);
		net = net - consumption;

		// Add water usage from making meal and dessert
		double cooking = settlement.getDailyWaterUsage(WaterUseType.PREP_MEAL)
					+ settlement.getDailyWaterUsage(WaterUseType.PREP_DESSERT);
		response.appendTableRow("Cooking", Math.round(-cooking * 100.0) / 100.0);
		net = net - cooking;

		// Prints living usage
		List<Building> quarters = settlement.getBuildingManager()
				.getBuildings(FunctionType.LIVING_ACCOMMODATIONS);
		double livingUsage = 0;
		for (Building b : quarters) {
			LivingAccommodations la = b.getLivingAccommodations();
			livingUsage += la.getDailyAverageWaterUsage();

		}
		response.appendTableRow("Accommodation", Math.round(-livingUsage * 100.0) / 100.0);
		net = net - livingUsage;

		// Prints cleaning usage
		double cleaning = settlement.getDailyWaterUsage(WaterUseType.CLEAN_MEAL)
					+ settlement.getDailyWaterUsage(WaterUseType.CLEAN_DESSERT);
		response.appendTableRow("Cleaning", Math.round(-cleaning * 100.0) / 100.0);
		net = net - cleaning;

		// Prints output from resource processing
		double output = 0;
		List<Building> bldgs = settlement.getBuildingManager().getBuildings(FunctionType.RESOURCE_PROCESSING);
		for (Building b : bldgs) {
			ResourceProcessing rp = b.getResourceProcessing();
			List<ResourceProcess> processes = rp.getProcesses();
			for (ResourceProcess p : processes) {
				if (p.isProcessRunning())
					output += p.getMaxOutputResourceRate(ResourceUtil.waterID);
			}
		}
		response.appendTableRow(PROCESSES, Math.round(output * 1_000 * 100.0) / 100.0);
		net = net + output * 1_000;
		
		// Prints output from waste processing
		double output2 = 0;
		List<Building> bldgs2 = settlement.getBuildingManager().getBuildings(FunctionType.WASTE_PROCESSING);
		for (Building b : bldgs2) {
			WasteProcessing wp = b.getWasteProcessing();
			List<WasteProcess> processes = wp.getProcesses();
			for (WasteProcess p : processes) {
				if (p.isProcessRunning())
					output2 += p.getMaxOutputRate(ResourceUtil.waterID);
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
		
		response.appendHeading(WHITESPACES + "O2 Usage & " + GREENHOUSE_FARMING);
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
