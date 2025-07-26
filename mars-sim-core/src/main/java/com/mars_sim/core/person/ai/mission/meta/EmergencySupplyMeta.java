/*
 * Mars Simulation Project
 * EmergencySupplyMeta.java
 * @date 2025-07-24
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission.meta;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.EmergencySupply;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.MissionUtil;
import com.mars_sim.core.person.ai.mission.RoverMission;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Rover;

/**
 * A meta mission for the EmergencySupply mission.
 */
public class EmergencySupplyMeta extends AbstractMetaMission {

	EmergencySupplyMeta() {
    	super(MissionType.EMERGENCY_SUPPLY,  null);
    }

    @Override
    public Mission constructInstance(Person person, boolean needsReview) {
        return new EmergencySupply(person, needsReview);
    }

    @Override
    public RatingScore getProbability(Person person) {

        RatingScore missionProbability = RatingScore.ZERO_RATING;
    	
        // Check if person is in a settlement.
        if (person.isInSettlement()) {
        		
            Settlement settlement = person.getSettlement();

            Rover rover = RoverMission.getVehicleWithGreatestRange(settlement, false);
            if (rover != null) {
            	// Question : how to record targetSettlement in such a way that 
            	// it doesn't need to look for it again in EmergencySupply
                Settlement targetSettlement = EmergencySupply.findSettlementNeedingEmergencySupplies(
                        settlement, rover);
                if (targetSettlement == null) {
                    return RatingScore.ZERO_RATING;
                }
                else {
                    missionProbability = new RatingScore(EmergencySupply.BASE_STARTING_PROBABILITY);
                }
            }
            else {
                return RatingScore.ZERO_RATING;
            }
            
            int min_num = 0;
            int all = settlement.getNumCitizens();
            if (all == 2)
            	min_num = 0;
            else 
            	min_num = RoverMission.MIN_STAYING_MEMBERS;
    	    

            if (all == 2)
            	min_num = 1;
            else
            	min_num = RoverMission.MIN_GOING_MEMBERS;
    	    
            // Check if min number of EVA suits at settlement.
            if (MissionUtil.getNumberAvailableEVASuitsAtSettlement(settlement) < min_num) {
    	        return RatingScore.ZERO_RATING;
    	    }
    		   		
	        // Determine job modifier.
            missionProbability.addModifier(LEADER, getLeaderSuitability(person));
	
           	RoleType roleType = person.getRole().getType();
        	double roleModifier = switch(roleType) {
				case MISSION_SPECIALIST -> 1.5;
        		case CHIEF_OF_MISSION_PLANNING -> 3;
        		case SUB_COMMANDER -> 4.5;
        		case COMMANDER -> 6;
				default -> 1;
			};
        	missionProbability.addModifier("Role", roleModifier);

            // Crowding modifier.
            int crowding = settlement.getIndoorPeopleCount() - settlement.getPopulationCapacity();
            if (crowding > 0) missionProbability.addModifier(OVER_CROWDING, (crowding + 1));

            missionProbability.applyRange(0, LIMIT);
        }

        return missionProbability;
    }
}
