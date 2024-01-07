/*
 * Mars Simulation Project
 * RescueSalvageVehicleMeta.java
 * @date 2021-08-28
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission.meta;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.MissionUtil;
import com.mars_sim.core.person.ai.mission.RescueSalvageVehicle;
import com.mars_sim.core.person.ai.mission.RoverMission;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * A meta mission for the RescueSalvageVehicle mission.
 */
public class RescueSalvageVehicleMeta extends AbstractMetaMission {
   
    RescueSalvageVehicleMeta() {
    	super(MissionType.RESCUE_SALVAGE_VEHICLE, null);
    }
  
    @Override
    public Mission constructInstance(Person person, boolean needsReview) {
        return new RescueSalvageVehicle(person, needsReview);
    }

    @Override
    public RatingScore getProbability(Person person) {

        RatingScore missionProbability = RatingScore.ZERO_RATING;

        if (person.isInSettlement()) {

            Settlement settlement = person.getSettlement();

            Vehicle vehicleTarget = null;

            // Check if there are any beacon vehicles within range that need help.
            Vehicle vehicle = RoverMission.getVehicleWithGreatestRange(settlement, true);
            if (vehicle != null) {
                vehicleTarget = RescueSalvageVehicle.findBeaconVehicle(settlement,
                        vehicle.getRange());
                if (vehicle == vehicleTarget)
                    return RatingScore.ZERO_RATING;
                else if (vehicleTarget == null)
                    return RatingScore.ZERO_RATING;
                else if (!RescueSalvageVehicle.isClosestCapableSettlement(settlement, vehicleTarget))
                    return RatingScore.ZERO_RATING;  
                
                missionProbability = new RatingScore(1 + RescueSalvageVehicle.BASE_RESCUE_MISSION_WEIGHT);
                missionProbability.addModifier("stranded", 
                                    RescueSalvageVehicle.getRescuePeopleNum(vehicleTarget));                  
            }
            else {
                return RatingScore.ZERO_RATING;
            }
    
            int all = settlement.getNumCitizens();
            int minMembers = 0;
            if (all <= 3)
            	minMembers = 0;
            else if (all > 3)	
            	minMembers = RescueSalvageVehicle.MIN_STAYING_MEMBERS;
    	    
            // Check if min number of EVA suits at settlement.
            if (MissionUtil.getNumberAvailableEVASuitsAtSettlement(settlement) < minMembers) {
    	        return RatingScore.ZERO_RATING;
    	    }
   
            // Check if minimum number of people are available at the settlement.
            // and a backup rover
            if (!MissionUtil.minAvailablePeopleAtSettlement(settlement, minMembers)
                    || !RoverMission.hasBackupRover(settlement)) {
                return RatingScore.ZERO_RATING;
            }

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
            if (crowding > 0) {
                missionProbability.addModifier(OVER_CROWDING, (crowding + 1));
            }
            
            // Job modifier.
            missionProbability.addModifier(LEADER, getLeaderSuitability(person));
            missionProbability.applyRange(0, LIMIT);
        }

        return missionProbability;
    }
}
