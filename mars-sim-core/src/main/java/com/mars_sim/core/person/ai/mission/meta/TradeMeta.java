/*
 * Mars Simulation Project
 * TradeMeta.java
 * @date 2021-08-28
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission.meta;

import java.util.Set;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.goods.Deal;
import com.mars_sim.core.goods.GoodsManager;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.RoverMission;
import com.mars_sim.core.person.ai.mission.Trade;
import com.mars_sim.core.person.ai.mission.MissionUtil;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Rover;

/**
 * A meta mission for the Trade mission.
 */
public class TradeMeta extends AbstractMetaMission {

	/** default logger. */
//	private static SimLogger logger = SimLogger.getLogger(TradeMeta.class.getName());
	
	/** Starting sol for this mission to commence. */
	public static final int MIN_STARTING_SOL = 4;

	TradeMeta() {
		super(MissionType.TRADE, 
				Set.of(JobType.POLITICIAN, JobType.TRADER, JobType.REPORTER));
	}

	@Override
	public Mission constructInstance(Person person, boolean needsReview) {
		return new Trade(person, needsReview);
	}

	@Override
	public RatingScore getProbability(Person person) {

		RatingScore missionProbability = RatingScore.ZERO_RATING;

		Settlement settlement = person.getAssociatedSettlement();
		
    	if (getMarsTime().getMissionSol() < MIN_STARTING_SOL) {
    		return missionProbability;
    	}
		
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
			}
			
			if (missionProbability.getScore() <= 0)
				return missionProbability;

			// if introvert, score  0 to  50 --> -2 to 0
			// if extrovert, score 50 to 100 -->  0 to 2
			// Reduce probability if introvert
			int extrovert = person.getExtrovertmodifier();
			missionProbability.addModifier(PERSON_EXTROVERT, (1 + extrovert/2.0));
			missionProbability.applyRange(0, LIMIT);
		}

        
		return missionProbability;
	}

	private RatingScore getSettlementProbability(Settlement settlement) {
		
		// Check for the best trade settlement within range.			
		Rover rover = RoverMission.getVehicleWithGreatestRange(settlement, false);
		if (rover == null) {
			return RatingScore.ZERO_RATING;
		}
		GoodsManager gManager = settlement.getGoodsManager();

		Deal deal = gManager.getBestDeal(MissionType.TRADE, rover);
		if (deal == null) {
			return RatingScore.ZERO_RATING;
		}	

		int numEmbarked = MissionUtil.numEmbarkingMissions(settlement);
		int numThisMission = Simulation.instance().getMissionManager().numParticularMissions(MissionType.TRADE, settlement);

   		// Check for # of embarking missions.
		if (Math.max(1, settlement.getNumCitizens() / 4.0) < numEmbarked + numThisMission) {
			return RatingScore.ZERO_RATING;
		}			
		else if (numThisMission > 1)
			return RatingScore.ZERO_RATING;	

		double tradeProfit = deal.getProfit();

		// Trade value modifier.
		RatingScore missionProbability = new RatingScore(tradeProfit / 1000D * gManager.getTradeFactor());
		missionProbability.applyRange(0, Trade.MAX_STARTING_PROBABILITY);


		int f1 = 2*numEmbarked + 1;
		int f2 = 2*numThisMission + 1;
		
		missionProbability.addModifier("missionratio", settlement.getNumCitizens() / f1 / f2 / 2D);
		
		// Crowding modifier.
		int crowding = settlement.getIndoorPeopleCount() - settlement.getPopulationCapacity();
		if (crowding > 0) {
			missionProbability.addModifier(OVER_CROWDING, (crowding + 1));
		}

		return missionProbability;
	}	
}
