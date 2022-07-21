/*
 * Mars Simulation Project
 * TradeMeta.java
 * @date 2021-08-28
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.Set;
import java.util.logging.Level;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.goods.CommerceUtil;
import org.mars_sim.msp.core.goods.GoodsManager;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A meta mission for the Trade mission.
 */
public class TradeMeta extends AbstractMetaMission {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TradeMeta.class.getName());


	TradeMeta() {
		super(MissionType.TRADE, "trade",
				Set.of(JobType.POLITICIAN, JobType.TRADER, JobType.REPORTER));
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

		double missionProbability;
		
		// Check for the best trade settlement within range.
		double tradeProfit = 0D;
			
		Rover rover = (Rover) RoverMission.getVehicleWithGreatestRange(MissionType.TRADE, settlement, false);
		GoodsManager gManager = settlement.getGoodsManager();
		try {
			if (rover != null) {
				tradeProfit = gManager.getBestDeal(MissionType.TRADE, rover).getProfit();
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Issues with TradeUtil.", e);
			return 0;
		}

		// Trade value modifier.
		missionProbability = tradeProfit / 1000D * gManager.getTradeFactor();
		if (missionProbability > Trade.MAX_STARTING_PROBABILITY) {
			missionProbability = Trade.MAX_STARTING_PROBABILITY;
		}

		int numEmbarked = VehicleMission.numEmbarkingMissions(settlement);
		int numThisMission = Simulation.instance().getMissionManager().numParticularMissions(MissionType.TRADE, settlement);

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
