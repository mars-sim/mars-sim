/*
 * Mars Simulation Project
 * TradeMeta.java
 * @date 2021-08-28
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.logging.Level;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.person.ai.mission.Trade.TradeProfitInfo;
import org.mars_sim.msp.core.person.ai.mission.TradeUtil;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A meta mission for the Trade mission.
 */
public class TradeMeta extends AbstractMetaMission {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TradeMeta.class.getName());

    private static final int FREQUENCY = 1000;

	TradeMeta() {
		super(MissionType.TRADE, "trade");
	}

	@Override
	public Mission constructInstance(Person person) {
		return new Trade(person);
	}

	@Override
	public double getProbability(Person person) {

		double missionProbability = 0D;

		Settlement settlement = person.getAssociatedSettlement();
		
		if (settlement.isFirstSol())
			return 0;
		
		// Check if person is in a settlement.
		if (settlement != null) {
	
			RoleType roleType = person.getRole().getType();
			
			if (RoleType.CHIEF_OF_SUPPLY_N_RESOURCES == roleType
					|| RoleType.RESOURCE_SPECIALIST == roleType
		 			|| RoleType.MISSION_SPECIALIST == roleType
		 			|| RoleType.CHIEF_OF_MISSION_PLANNING == roleType	
					|| RoleType.SUB_COMMANDER == roleType
					|| RoleType.COMMANDER == roleType
					) {
			
					// Note: checkMission() gives rise to a NULLPOINTEREXCEPTION that points to
					// Inventory
					// It happens only when this sim is a loaded saved sim.
					missionProbability = getSettlementProbability(settlement);

				
			} else {
				missionProbability = 0;
			}
			
			if (missionProbability <= 0)
				return 0;

			// if introvert, score  0 to  50 --> -2 to 0
			// if extrovert, score 50 to 100 -->  0 to 2
			// Reduce probability if introvert
			int extrovert = person.getExtrovertmodifier();
			missionProbability += extrovert;
			
			if (missionProbability < 0)
				missionProbability = 0;
		}

        
		return missionProbability;
	}

	private double getSettlementProbability(Settlement settlement) {

		double missionProbability = 0;

		if (settlement.getMissionBaseProbability(getName()))
        	missionProbability = 1;
        else
			return 0;

		missionProbability = 0;
		
		// Check for the best trade settlement within range.
		double tradeProfit = 0D;
			
		Rover rover = (Rover) RoverMission.getVehicleWithGreatestRange(Trade.missionType, settlement, false);

		try {
			if (rover != null) {
				// Only check every couple of Sols, else use cache.
				// Note: this method is very CPU intensive.
				boolean useCache = false;

				if (Trade.TRADE_PROFIT_CACHE.containsKey(settlement)) {
					TradeProfitInfo profitInfo = Trade.TRADE_PROFIT_CACHE.get(settlement);
					double timeDiff = MarsClock.getTimeDiff(marsClock, profitInfo.time);
					if (timeDiff < FREQUENCY) {
						tradeProfit = profitInfo.profit;
//						useCache = true;
					}
				} else {
					Trade.TRADE_PROFIT_CACHE.put(settlement,
							new TradeProfitInfo(tradeProfit, (MarsClock) marsClock.clone()));
					useCache = true;
				}

				if (!useCache) {
//					double startTime = System.currentTimeMillis();
					tradeProfit = TradeUtil.getBestTradeProfit(settlement, rover);
//					double endTime = System.currentTimeMillis();
					logger.info(settlement, 30_000, // getBestTradeProfit: " + (endTime - startTime)
//					// + " milliseconds "
							"Best Trade Profit: " + Math.round(tradeProfit*10.0)/10. + " VP");
					Trade.TRADE_PROFIT_CACHE.put(settlement,
							new TradeProfitInfo(tradeProfit, (MarsClock) marsClock.clone()));
					Trade.TRADE_SETTLEMENT_CACHE.put(settlement, TradeUtil.bestTradeSettlementCache);
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Issues with TradeUtil.", e);
			return 0;
		}

		// Trade value modifier.
		missionProbability = tradeProfit / 1000D * settlement.getGoodsManager().getTradeFactor();
		if (missionProbability > Trade.MAX_STARTING_PROBABILITY) {
			missionProbability = Trade.MAX_STARTING_PROBABILITY;
		}

		int numEmbarked = VehicleMission.numEmbarkingMissions(settlement);
		int numThisMission = Simulation.instance().getMissionManager().numParticularMissions(getName(), settlement);

   		// Check for # of embarking missions.
		if (Math.max(1, settlement.getNumCitizens() / 4.0) < numEmbarked + numThisMission) {
			return 0;
		}			
		
		else if (numThisMission > 1)
			return 0;	
		

		int f1 = 2*numEmbarked + 1;
		int f2 = 2*numThisMission + 1;
		
		missionProbability *= settlement.getNumCitizens() / f1 / f2 / 2D;
		
		// Crowding modifier.
		int crowding = settlement.getIndoorPeopleCount() - settlement.getPopulationCapacity();
		if (crowding > 0) {
			missionProbability *= (crowding + 1);
		}

		return missionProbability;
	}
	
}
