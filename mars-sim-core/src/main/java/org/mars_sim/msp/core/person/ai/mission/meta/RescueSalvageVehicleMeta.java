/*
 * Mars Simulation Project
 * RescueSalvageVehicleMeta.java
 * @date 2021-08-28
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import org.mars_sim.msp.core.data.RatingScore;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.MissionUtil;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

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
    
            int min_num = 0;
            int all = settlement.getNumCitizens();
 
            if (all <= 3)
            	min_num = 0;
            else if (all > 3)	
            	min_num = RescueSalvageVehicle.MIN_STAYING_MEMBERS;
    	    
            // Check if min number of EVA suits at settlement.
            if (MissionUtil.getNumberAvailableEVASuitsAtSettlement(settlement) < min_num) {
    	        return RatingScore.ZERO_RATING;
    	    }
   
            // Check if minimum number of people are available at the settlement.
            if (!MissionUtil.minAvailablePeopleAtSettlement(settlement, min_num)) {
                return RatingScore.ZERO_RATING;
            }

            // Check if available backup rover.
            else if (!RoverMission.hasBackupRover(settlement)) {
                return RatingScore.ZERO_RATING;
            }
            
			missionProbability.addModifier(SETTLEMENT_POPULATION,
                            getSettlementPopModifier(settlement, 4));

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
