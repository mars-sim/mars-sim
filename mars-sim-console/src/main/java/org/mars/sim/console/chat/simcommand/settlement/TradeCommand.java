/**
 * Mars Simulation Project
 * TradeCommand.java
 * @date 22-07-22
 * @author Barry Evans
 */
package org.mars.sim.console.chat.simcommand.settlement;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.goods.CommerceUtil;
import org.mars_sim.msp.core.goods.Deal;
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

	private TradeCommand() {
		super("tr", "trade", "Trade deals of a Settlement");
	}

	/** 
	 * Output the current immediate location of the Unit
	 * @return 
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {

		// See what trade can be done
		StructuredResponse response = new StructuredResponse();

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
									"Mission", 8, "Profit");
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
		context.println(response.getOutput());
		return true;
	}

	private void outputDeal(Settlement seller, 
							    MissionType commerce, 
								Settlement buyer, double distanceTo,
								Vehicle delivery,
								StructuredResponse response) {
		

		Deal deal = CommerceUtil.getPotentialDeal(seller, commerce, buyer, delivery);
		String profit = "";
		if (deal != null) {
			profit = String.format(CommandHelper.DOUBLE_FORMAT, deal.getProfit());
		}
		response.appendTableRow(buyer.getName(), String.format(CommandHelper.KM_FORMAT, distanceTo),
								commerce.getName(),	profit);
	}
}
