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
import org.mars_sim.msp.core.person.ai.mission.Trade.TradeProfitInfo;
import org.mars_sim.msp.core.person.ai.mission.TradeUtil;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A meta mission for the Trade mission.
 */
public class TradeMeta implements MetaMission {

	/** Mission name */
	private static final String DEFAULT_DESCRIPTION = Msg.getString("Mission.description.trade"); //$NON-NLS-1$

	/** default logger. */
	private static Logger logger = Logger.getLogger(TradeMeta.class.getName());

//    private static final double LIMIT = 50D;
    
	private Person person;
	private Robot robot;

	@Override
	public String getName() {
		return DEFAULT_DESCRIPTION;
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
					missionProbability = getSettlementProbability(settlement);

				} catch (Exception e) {
					logger.log(Level.SEVERE,
							person + " can't compute the exact need for trading now at " + settlement + ". ", e);
					e.printStackTrace();

					missionProbability = 0D;
				}
				
			} else {
				missionProbability = 0;
			}
			
    		if (missionProbability <= 0)
    			return 0;
    		
			int numEmbarked = VehicleMission.numEmbarkingMissions(settlement);
			int numThisMission = Simulation.instance().getMissionManager().numParticularMissions(DEFAULT_DESCRIPTION, settlement);
	
	   		// Check for # of embarking missions.
    		if (Math.max(1, settlement.getNumCitizens()) / 8.0 < numEmbarked + numThisMission) {
    			return 0;
    		}		
    		
    		if (numThisMission > 1)
    			return 0;	
    		

			int f1 = 2*numEmbarked + 1;
			int f2 = 2*numThisMission + 1;
			
			missionProbability *= settlement.getNumCitizens() / f1 / f2 / 2D * ( 1 + settlement.getMissionDirectiveModifier(7));
		
			if (missionProbability > Trade.MAX_STARTING_PROBABILITY)
				missionProbability = Trade.MAX_STARTING_PROBABILITY;
			
			// if introvert, score  0 to  50 --> -2 to 0
			// if extrovert, score 50 to 100 -->  0 to 2
			// Reduce probability if introvert
			int extrovert = person.getExtrovertmodifier();
			missionProbability += extrovert;
			
			if (missionProbability < 0)
				missionProbability = 0;
		}
		
//        if (missionProbability > 0)
//        	logger.info("TradeMeta's probability : " +
//				 Math.round(missionProbability*100D)/100D);
		 
        
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

	public double getSettlementProbability(Settlement settlement) {

		double missionProbability = settlement.getMissionBaseProbability(DEFAULT_DESCRIPTION);

		if (missionProbability == 0)
			return 0;

		missionProbability = 0;
		
		// Check for the best trade settlement within range.
		double tradeProfit = 0D;
		try {
			Rover rover = (Rover) RoverMission.getVehicleWithGreatestRange(Trade.missionType, settlement, false);
			if (rover != null) {
				// Only check every couple of Sols, else use cache.
				// Note: this method is very CPU intensive.
				boolean useCache = false;

				if (Trade.TRADE_PROFIT_CACHE.containsKey(settlement)) {
					TradeProfitInfo profitInfo = Trade.TRADE_PROFIT_CACHE.get(settlement);
					double timeDiff = MarsClock.getTimeDiff(marsClock, profitInfo.time);
					if (timeDiff < 2000D) {
						tradeProfit = profitInfo.profit;
						useCache = true;
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
//					logger.info("[" + settlement.getName() + "] " // getBestTradeProfit: " + (endTime - startTime)
//					// + " milliseconds "
//							+ " Profit: " + (int) tradeProfit + " VP");
					Trade.TRADE_PROFIT_CACHE.put(settlement,
							new TradeProfitInfo(tradeProfit, (MarsClock) marsClock.clone()));
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

		int numEmbarked = VehicleMission.numEmbarkingMissions(settlement);
		int numThisMission = Simulation.instance().getMissionManager().numParticularMissions(DEFAULT_DESCRIPTION, settlement);

   		// Check for # of embarking missions.
		if (Math.max(1, settlement.getNumCitizens() / 8.0) < numEmbarked + numThisMission) {
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