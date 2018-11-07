/**
 * Mars Simulation Project
 * TradeMeta.java
 * @version 3.1.0 2017-01-19
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Trader;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.person.ai.mission.TradeUtil;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.mission.Trade.TradeProfitInfo;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A meta mission for the Trade mission.
 */
public class TradeMeta implements MetaMission {

	/** Mission name */
	private static final String NAME = Msg.getString("Mission.description.trade"); //$NON-NLS-1$

	/** default logger. */
	private static Logger logger = Logger.getLogger(TradeMeta.class.getName());

	private Person person;
	private Robot robot;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Mission constructInstance(Person person) {
		return new Trade(person);
	}

	@Override
	public double getProbability(Person person) {

		double missionProbability = 0D;

		// Check if person is in a settlement.
		if (person.isInSettlement()) {
			// Check if mission is possible for person based on their circumstance.
			Settlement settlement = person.getSettlement();

			if (person.getMind().getJob() instanceof Trader) {

				try {
					// TODO: checkMission() gives rise to a NULLPOINTEREXCEPTION that points to
					// Inventory
					// It happens only when this sim is a loaded saved sim.
					missionProbability = checkMission(settlement);

				} catch (Exception e) {
					logger.log(Level.SEVERE,
							person + " can't compute the exact need for trading now at " + settlement + ". ", e);
					e.printStackTrace();

					missionProbability = 200D;
				}

			} else {
				missionProbability = 0;
			}
		}

		return missionProbability;
	}

	@Override
	public Mission constructInstance(Robot robot) {
		return null;// new Trade(robot);
	}

	@Override
	public double getProbability(Robot robot) {
		return 0;
	}

	public double checkMission(Settlement settlement) {

		double missionProbability = 0;

		// Check if available rover.
		if (!RoverMission.areVehiclesAvailable(settlement, false)) {
			return 0;
		}

		// Check if available backup rover.
		if (!RoverMission.hasBackupRover(settlement)) {
			return 0;
		}

		// Check if minimum number of people are available at the settlement.
		if (!RoverMission.minAvailablePeopleAtSettlement(settlement, RoverMission.MIN_STAYING_MEMBERS)) {
			return 0;
		}

		// Check if min number of EVA suits at settlement.
		if (Mission.getNumberAvailableEVASuitsAtSettlement(settlement) < RoverMission.MIN_GOING_MEMBERS) {
			return 0;
		}

		// Check for embarking missions.
		if (VehicleMission.hasEmbarkingMissions(settlement)) {
			return 0;
		}

		// Check if settlement has enough basic resources for a rover mission.
		if (!RoverMission.hasEnoughBasicResources(settlement, true)) {
			return 0;
		}

		// Check if starting settlement has minimum amount of methane fuel.
		// AmountResource methane = AmountResource.findAmountResource("methane");
		if (settlement.getInventory().getAmountResourceStored(ResourceUtil.methaneAR,
				false) < RoverMission.MIN_STARTING_SETTLEMENT_METHANE) {
			return 0;
		}

		// Check for the best trade settlement within range.
		double tradeProfit = 0D;
		try {
			Rover rover = (Rover) RoverMission.getVehicleWithGreatestRange(settlement, false);
			if (rover != null) {
				// Only check every couple of Sols, else use cache.
				// Note: this method is very CPU intensive.
				boolean useCache = false;
				MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
				if (currentTime == null) {
					throw new NullPointerException("currentTime == null");
				}

				if (Trade.TRADE_PROFIT_CACHE.containsKey(settlement)) {
					TradeProfitInfo profitInfo = Trade.TRADE_PROFIT_CACHE.get(settlement);
					double timeDiff = MarsClock.getTimeDiff(currentTime, profitInfo.time);
					if (timeDiff < 2000D) {
						tradeProfit = profitInfo.profit;
						useCache = true;
					}
				} else {
					Trade.TRADE_PROFIT_CACHE.put(settlement,
							new TradeProfitInfo(tradeProfit, (MarsClock) currentTime.clone()));
					useCache = true;
				}

				if (!useCache) {
//					double startTime = System.currentTimeMillis();
					tradeProfit = TradeUtil.getBestTradeProfit(settlement, rover);
//					double endTime = System.currentTimeMillis();
//					logger.info("[" + settlement.getName() + "] " // getBestTradeProfit: " + (endTime - startTime)
//					// + " milliseconds "
//							+ " Profit: " + (int) tradeProfit + " VP");
					Trade.TRADE_PROFIT_CACHE.put(settlement,
							new TradeProfitInfo(tradeProfit, (MarsClock) currentTime.clone()));
					Trade.TRADE_SETTLEMENT_CACHE.put(settlement, TradeUtil.bestTradeSettlementCache);
				}
			}
		} catch (Exception e) {
			if (person != null)
				logger.log(Level.SEVERE, person + "can't find vehicles at settlement.", e);
			else if (robot != null)
				logger.log(Level.SEVERE, robot + "can't find vehicles at settlement.", e);
			e.printStackTrace();
		}

		// Determine mission probability.

		// Trade value modifier.
		missionProbability = tradeProfit / 1000D * settlement.getGoodsManager().getTradeFactor();
		if (missionProbability > Trade.MAX_STARTING_PROBABILITY) {
			missionProbability = Trade.MAX_STARTING_PROBABILITY;
		}

		// Crowding modifier.
		int crowding = settlement.getIndoorPeopleCount() - settlement.getPopulationCapacity();
		if (crowding > 0) {
			missionProbability *= (crowding + 1);
		}

		return missionProbability;
	}

}