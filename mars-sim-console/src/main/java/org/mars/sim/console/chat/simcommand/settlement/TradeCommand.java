/*
 * Mars Simulation Project
 * TradeCommand.java
 * @date 2022-07-22
 * @author Barry Evans
 */
package org.mars.sim.console.chat.simcommand.settlement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.goods.CommerceUtil;
import org.mars_sim.msp.core.goods.Deal;
import org.mars_sim.msp.core.goods.Good;
import org.mars_sim.msp.core.goods.ShoppingItem;
import org.mars_sim.msp.core.person.ai.mission.DroneMission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Drone;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Command to display trade details for a Settlement
 * This is a singleton.
 */
public class TradeCommand extends AbstractSettlementCommand {

	public static final ChatCommand TRADE = new TradeCommand();
	private static final int COST_WIDTH = 10;
	private static final String DEALS = "deals";
	private static final String BUYING = "buying";
	private static final String SELLING = "selling";

	private TradeCommand() {
		super("tr", "trade", "Trade deals of a Settlement");
		setArguments(List.of(DEALS, BUYING, SELLING));
	}

	/** 
	 * Output the current immediate location of the Unit
	 * @return 
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {

		// See what trade can be done
		StructuredResponse response = new StructuredResponse();

		if (BUYING.equals(input)) {
			outputShoppingList(settlement.getGoodsManager().getBuyList(), "Items to Buy", response);
		}
		else if (SELLING.equals(input)) {
			outputShoppingList(settlement.getGoodsManager().getSellList(), "Items for Sale", response);
		}
		else if (DEALS.equals(input) || (input == null)) {
			outputDeals(context, settlement, response);
		}
		else {
			response.append("The command " + input + " is unknown.");
		}

		context.println(response.getOutput());
		return true;
	}

	private void outputShoppingList(Map<Good,ShoppingItem> list, String name, StructuredResponse response) {
		response.appendHeading(name);
		
		response.appendTableHeading("Good", CommandHelper.GOOD_WIDTH, "Quantity", 6,
									"Price", COST_WIDTH);

		List<Good> ordered = new ArrayList<>(list.keySet());
		Collections.sort(ordered);						
		for(Good good : ordered) {
			ShoppingItem item = list.get(good);
			response.appendTableRow(good.getName(), item.getQuantity(),
						String.format(CommandHelper.MONEY_FORMAT, item.getPrice()));
		}
	}

	private void outputDeals(Conversation context, Settlement settlement, StructuredResponse response) {

		// Find some example vehicles
		response.appendLabeledString("Delivery Mission Range", String.format(CommandHelper.KM_FORMAT,
									 (double) settlement.getMissionRadius(MissionType.DELIVERY)));
		Drone drone = DroneMission.getDroneWithGreatestRange(MissionType.DELIVERY, settlement, true);
		if (drone != null) {
			response.appendLabeledString("Drone Range", String.format(CommandHelper.KM_FORMAT,
										drone.getRange(MissionType.DELIVERY)));
			response.appendLabeledString("Drone Base Range", String.format(CommandHelper.KM_FORMAT,
										drone.getBaseRange()));
		}

		response.appendLabeledString("Trade Mission Range", String.format(CommandHelper.KM_FORMAT,
									(double) settlement.getMissionRadius(MissionType.TRADE)));
		Rover rover = (Rover) RoverMission.getVehicleWithGreatestRange(MissionType.TRADE, settlement, true);
		if (rover != null) {
			response.appendLabeledString("Rover Range", String.format(CommandHelper.KM_FORMAT,
										rover.getRange(MissionType.TRADE)));
			response.appendLabeledString("Rover Base Range", String.format(CommandHelper.KM_FORMAT,
										rover.getBaseRange()));
		}

		// Get deals for all other settlements
		response.appendBlankLine();
		response.appendTableHeading("Buyer", CommandHelper.PERSON_WIDTH, "Distance", 9,
									"Mission", 8, "Sell", COST_WIDTH, "Buy", COST_WIDTH,
									"Cost", COST_WIDTH, "Profit", COST_WIDTH);
		for (Settlement tradingSettlement : context.getSim().getUnitManager().getSettlements()) {
			if (settlement.equals(tradingSettlement)) {
				continue;
			}

			double distanceTo = settlement.getCoordinates().getDistance(tradingSettlement.getCoordinates());
			if (rover != null) {
				outputDeal(settlement, MissionType.TRADE, tradingSettlement, distanceTo, rover, response);
			}
			if (drone != null) {
				outputDeal(settlement, MissionType.DELIVERY, tradingSettlement, distanceTo, drone, response);
			}
		}
	}

	private void outputDeal(Settlement seller, 
							    MissionType commerce, 
								Settlement buyer, double distanceTo,
								Vehicle delivery,
								StructuredResponse response) {
		

		Deal deal = CommerceUtil.getPotentialDeal(seller, commerce, buyer, delivery);
		if (deal != null) {
			response.appendTableRow(buyer.getName(), String.format(CommandHelper.KM_FORMAT, distanceTo),
									commerce.getName(),
									String.format(CommandHelper.MONEY_FORMAT, deal.getSellingRevenue()),
									String.format(CommandHelper.MONEY_FORMAT, deal.getBuyingRevenue()),
									String.format(CommandHelper.MONEY_FORMAT, deal.getTradeCost()),
									String.format(CommandHelper.MONEY_FORMAT, deal.getProfit()));
		}
		else {
			response.appendTableRow(buyer.getName(), String.format(CommandHelper.KM_FORMAT, distanceTo),
								commerce.getName(),	"", "", "", "");
		}
	}
}
