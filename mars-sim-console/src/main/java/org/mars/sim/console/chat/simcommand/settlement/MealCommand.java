package org.mars.sim.console.chat.simcommand.settlement;

import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.cooking.CookedMeal;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;

/**
 * Command to display cooked meals in a Settlement
 * This is a singleton.
 */
public class MealCommand extends AbstractSettlementCommand {

	public static final ChatCommand MEALS = new MealCommand();

	private MealCommand() {
		super("me", "meals", "Available Meals");
	}

	/** 
	 * Output the current immediate location of the Unit
	 * @return 
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		List<Building> kitchens = settlement.getBuildingManager().getBuildings(FunctionType.COOKING);
		
		StructuredResponse response = new StructuredResponse();
		for (Building building : kitchens) {
			Cooking kitchen = building.getCooking();
			response.appendHeading(building.getNickName());
			List<CookedMeal> meals = kitchen.getCookedMealList();
			if (meals.isEmpty()) {
				response.append("No meals available");
			}
			else {
				response.appendTableHeading("Meal", 34, "Servings", "Best Before");
				for (CookedMeal m : meals) {
					response.appendTableRow(m.getName(), m.getQuality(), m.getExpirationTime().getDateTimeStamp());
				}
			}
			response.appendBlankLine();
		}
		
		context.println(response.getOutput());
		
		return true;
	}

}
