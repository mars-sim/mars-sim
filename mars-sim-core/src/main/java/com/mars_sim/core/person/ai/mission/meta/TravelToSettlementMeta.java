/**
 * Mars Simulation Project
 * TravelToSettlementMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission.meta;

import java.util.Iterator;
import java.util.Map;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.RoverMission;
import com.mars_sim.core.person.ai.mission.TravelToSettlement;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * A meta mission for the TravelToSettlement mission.
 */
public class TravelToSettlementMeta extends AbstractMetaMission {
    
    private static final int EARLIEST_SOL_TRAVEL = 28;

	public TravelToSettlementMeta() {
		// Anyone can start ??
    	super(MissionType.TRAVEL_TO_SETTLEMENT, null);
    }
    
    @Override
    public Mission constructInstance(Person person, boolean needsReview) {
        return new TravelToSettlement(person, needsReview);
    }

    @Override
    public RatingScore getProbability(Person person) {

        RatingScore missionProbability = RatingScore.ZERO_RATING;
    	if (getMarsTime().getMissionSol() < EARLIEST_SOL_TRAVEL) {
    		return RatingScore.ZERO_RATING;
    	}
    	
        if (person.isInSettlement()) {
            // Check if mission is possible for person based on their
            // circumstance.
            Settlement settlement = person.getSettlement();

            missionProbability = getMissionProbability(settlement, person);

    		if (missionProbability.getScore() <= 0) {
    			return RatingScore.ZERO_RATING;
    		}
    		
	        // Job modifier.
    		missionProbability.addModifier(LEADER, getLeaderSuitability(person));
            missionProbability = MetaTask.applyCommerceFactor(missionProbability, settlement, CommerceType.TOURISM);
			
			// if introvert, score  0 to  50 --> -2 to 0
			// if extrovert, score 50 to 100 -->  0 to 2
			// Reduce probability if introvert
			int extrovert = person.getExtrovertmodifier();
			missionProbability.addModifier(PERSON_EXTROVERT, (1 + extrovert/2.0));
			
			missionProbability.applyRange(0, LIMIT);
        }
		 
        return missionProbability;
    }

    private RatingScore getMissionProbability(Settlement settlement, Worker member) {
        
        // Check if there are any desirable settlements within range.
        double topSettlementDesirability = 0D;
        Vehicle vehicle = RoverMission.getVehicleWithGreatestRange(settlement, false);
        if (vehicle == null) {
            return RatingScore.ZERO_RATING;
        }

        Map<Settlement, Double> desirableSettlements = TravelToSettlement.getDestinationSettlements(
                member, settlement, vehicle.getEstimatedRange());

        if ((desirableSettlements == null) || desirableSettlements.isEmpty()) {
            return RatingScore.ZERO_RATING;
        }

        Iterator<Settlement> i = desirableSettlements.keySet().iterator();
        while (i.hasNext()) {
            Settlement desirableSettlement = i.next();
            double desirability = desirableSettlements.get(desirableSettlement);
            if (desirability > topSettlementDesirability) {
                topSettlementDesirability = desirability;
            }
        }

        // Determine mission probability.
        RatingScore missionProbability = new RatingScore(TravelToSettlement.BASE_MISSION_WEIGHT
                                                + (topSettlementDesirability / 100D));
		
        // Crowding modifier.
        int crowding = settlement.getIndoorPeopleCount()
                - settlement.getPopulationCapacity();
        if (crowding > 0) {
            missionProbability.addModifier(OVER_CROWDING, (crowding + 1));
        }

        return missionProbability;
    }

}
