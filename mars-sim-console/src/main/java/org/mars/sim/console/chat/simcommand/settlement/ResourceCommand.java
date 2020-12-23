package org.mars.sim.console.chat.simcommand.settlement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
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
import org.mars_sim.msp.core.structure.building.function.farming.Farming;

public class ResourceCommand extends AbstractSettlementCommand {
	public static final ChatCommand RESOURCE = new ResourceCommand();
	
	private static final String OXYGEN = "oxygen";
	private static final String CO2 = "co2";
	private static final String WATER = "water";

	private ResourceCommand() {
		super("rs", "resource", "Settlement resources; either oxygen, co2 or water");
		
		setIntroduction("Display the Resources held");
		
		// Setup  
		// Setup the fixed arguments
		setArguments(Arrays.asList(OXYGEN, CO2, WATER));
	}
	
	@Override
	protected void execute(Conversation context, String input, Settlement settlement) {
		if (input == null || input.isEmpty()) {
			context.println("Must enter a resource type");
		}
		else {
			StructuredResponse response = new StructuredResponse();
			String subCommand = input.trim().toLowerCase();

			if (OXYGEN.equals(subCommand)) {
				displayOxygen(settlement, response);
			}
			else if (WATER.equals(subCommand)) {
				displayWater(settlement, response);
			}
			else if (CO2.equals(subCommand)) {
				displayCO2(settlement, response);
			}
			else {
				response.append("Sorry don't know about resource " + subCommand);
			}
			
			context.println(response.getOutput());
		}
	}

	private void displayCO2(Settlement settlement, StructuredResponse response) {
		double usage = 0;
		double totalArea = 0;
		double reserve = settlement.getInventory().getAmountResourceStored(ResourceUtil.co2ID, false);

		response.appendHeading("Greenhouse Farming");
		response.appendLabeledString("Current reserve", Math.round(reserve * 100.0) / 100.0 + " kg");

		// Prints greenhouse usage
		List<Building> farms = settlement.getBuildingManager().getBuildings(FunctionType.FARMING);
		for (Building b : farms) {
			Farming f = b.getFarming();
			usage += f.computeUsage(2);
			totalArea += f.getGrowingArea();
		}

		response.appendLabeledString("Total growing area", Math.round(totalArea * 100.0) / 100.0 + " m2");
		response.appendLabeledString("Generated daily per m2",
									Math.round(usage / totalArea * 100.0) / 100.0 + " kg/m^2/sol");
		response.appendLabeledString("Total amount generated daily", Math.round(usage * 100.0) / 100.0 + " kg/sol");		
	}

	private void displayWater(Settlement settlement, StructuredResponse response) {
		double reserve = settlement.getInventory().getAmountResourceStored(ResourceUtil.waterID, false);
		response.appendLabeledString("Current reserve", Math.round(reserve * 100.0) / 100.0 + " kg");
		response.append(System.lineSeparator());
		
		double usage = 0;
		double totalArea = 0;

		// Prints greenhouse usage
		List<Building> farms = settlement.getBuildingManager().getBuildings(FunctionType.FARMING);
		for (Building b : farms) {
			Farming f = b.getFarming();
			usage += f.computeUsage(0);
			totalArea += f.getGrowingArea();
		}

		response.appendHeading("Greenhouse Farming");
		response.appendLabeledString("Total growing area", Math.round(totalArea * 100.0) / 100.0 + " m2");
		response.appendLabeledString("Consumed daily per m2",
				Math.round(usage / totalArea * 100.0) / 100.0 + " kg/m^2/sol");
		response.appendLabeledString("Projected daily consumed", Math.round(usage * 100.0) / 100.0 + " kg/sol");

		response.append(System.lineSeparator());

		double net = 0;
		double greenhouseUsage = 0;
		// Prints greenhouse usage
		for (Building b : farms) {
			Farming f = b.getFarming();
			greenhouseUsage += f.getDailyAverageWaterUsage();
		}
		
		response.appendTableHeading("Area", 16, "Consumption (kg/sol)");
		response.appendTableDouble("Greenhouse", Math.round(-greenhouseUsage * 100.0) / 100.0);

		net = net - greenhouseUsage;

		// Prints consumption
		double consumption = 0;
		List<Person> ppl = new ArrayList<>(settlement.getAllAssociatedPeople());
		for (Person p : ppl) {
			consumption += p.getDailyUsage(ResourceUtil.waterID);
		}
		response.appendTableDouble("People", Math.round(-consumption * 100.0) / 100.0);
		net = net - consumption;

		// Add water usage from making meal and dessert
		double cooking = settlement.getDailyWaterUsage(WaterUseType.PREP_MEAL)
					+ settlement.getDailyWaterUsage(WaterUseType.PREP_DESSERT);
		response.appendTableDouble("Cooking", Math.round(-cooking * 100.0) / 100.0);
		net = net - cooking;

		// Prints living usage
		List<Building> quarters = settlement.getBuildingManager()
				.getBuildings(FunctionType.LIVING_ACCOMMODATIONS);
		double livingUsage = 0;
		for (Building b : quarters) {
			LivingAccommodations la = b.getLivingAccommodations();
			livingUsage += la.getDailyAverageWaterUsage();

		}
		response.appendTableDouble("Accomodation", Math.round(-livingUsage * 100.0) / 100.0);
		net = net - livingUsage;

		// Prints cleaning usage
		double cleaning = settlement.getDailyWaterUsage(WaterUseType.CLEAN_MEAL)
					+ settlement.getDailyWaterUsage(WaterUseType.CLEAN_DESSERT);
		response.appendTableDouble("Cleaning", Math.round(-cleaning * 100.0) / 100.0);
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
		response.appendTableDouble("Processes", Math.round(output * 1_000 * 100.0) / 100.0);
		net = net + output * 1_000;
		
		response.appendTableDouble("Net", Math.round(net * 100.0) / 100.0);		
	}

	private void displayOxygen(Settlement settlement, StructuredResponse response) {
		double usage = 0;
		double totalArea = 0;
		double reserve = settlement.getInventory().getAmountResourceStored(ResourceUtil.oxygenID, false);

		response.appendHeading("Greenhouse Farming");
		response.appendLabeledString("Current reserve", Math.round(reserve * 100.0) / 100.0 + " kg");

		// Prints greenhouse usage
		List<Building> farms = settlement.getBuildingManager().getBuildings(FunctionType.FARMING);
		for (Building b : farms) {
			Farming f = b.getFarming();
			usage += f.computeUsage(1);
			totalArea += f.getGrowingArea();
		}

		response.appendLabeledString("Total growing area", Math.round(totalArea * 100.0) / 100.0 + " m2");
		response.appendLabeledString("Consumed daily per m2",
									Math.round(usage / totalArea * 100.0) / 100.0 + " kg/m^2/sol");
		response.appendLabeledString("Total amount consumed daily", Math.round(usage * 100.0) / 100.0 + " kg/sol");
	}
}
