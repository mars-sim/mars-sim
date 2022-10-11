/**
 * Mars Simulation Project
 * TravelToSettlementMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.Iterator;
import java.util.Map;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.MissionUtil;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A meta mission for the TravelToSettlement mission.
 */
public class TravelToSettlementMeta extends AbstractMetaMission {
    
    private static final int EARLIEST_SOL_TRAVEL = 28;
	private static final double LIMIT = 0;

	public TravelToSettlementMeta() {
		// Anyone can start ??
    	super(MissionType.TRAVEL_TO_SETTLEMENT, null);
    }
    
    @Override
    public Mission constructInstance(Person person, boolean needsReview) {
        return new TravelToSettlement(person, needsReview);
    }

    @Override
    public double getProbability(Person person) {

    	if (marsClock.getMissionSol() < EARLIEST_SOL_TRAVEL) {
    		return 0;
    	}
    	
        double missionProbability = 0D;

        if (person.isInSettlement()) {
            // Check if mission is possible for person based on their
            // circumstance.
            Settlement settlement = person.getSettlement();

            missionProbability = getMissionProbability(settlement, person);

    		if (missionProbability <= 0) {
    			return 0;
    		}
    		
	        // Job modifier.
    		missionProbability *= getLeaderSuitability(person)
    				* settlement.getGoodsManager().getTourismFactor();
			
			if (missionProbability > LIMIT)
				missionProbability = LIMIT;
			
			// if introvert, score  0 to  50 --> -2 to 0
			// if extrovert, score 50 to 100 -->  0 to 2
			// Reduce probability if introvert
			int extrovert = person.getExtrovertmodifier();
			missionProbability = missionProbability * (1 + extrovert/2.0);
			
			if (missionProbability < 0)
				missionProbability = 0;
        }
		 
        return missionProbability;
    }

    private double getMissionProbability(Settlement settlement, Worker member) {
        double missionProbability = 0;
        
        // Check if there are any desirable settlements within range.
        double topSettlementDesirability = 0D;
        Vehicle vehicle = RoverMission.getVehicleWithGreatestRange(MissionType.TRAVEL_TO_SETTLEMENT, settlement, false);
        if (vehicle != null) {
        	Map<Settlement, Double> desirableSettlements = TravelToSettlement.getDestinationSettlements(
                    member, settlement, vehicle.getRange(MissionType.TRAVEL_TO_SETTLEMENT));

            if ((desirableSettlements == null) || desirableSettlements.isEmpty()) {
            	return 0;
            }

            Iterator<Settlement> i = desirableSettlements.keySet().iterator();
            while (i.hasNext()) {
                Settlement desirableSettlement = i.next();
                double desirability = desirableSettlements.get(desirableSettlement);
                if (desirability > topSettlementDesirability) {
                    topSettlementDesirability = desirability;
                }
            }
        }


        // Determine mission probability.
        missionProbability = TravelToSettlement.BASE_MISSION_WEIGHT
                + (topSettlementDesirability / 100D);

		int numEmbarked = MissionUtil.numEmbarkingMissions(settlement);
		int numThisMission = missionManager.numParticularMissions(MissionType.TRAVEL_TO_SETTLEMENT, settlement);

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
        int crowding = settlement.getIndoorPeopleCount()
                - settlement.getPopulationCapacity();
        if (crowding > 0) {
            missionProbability *= (crowding + 1);
        }

        return missionProbability;
    }

}
