/*
 * Mars Simulation Project
 * EmergencySupplyMeta.java
 * @date 2022-07-14
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import org.mars_sim.msp.core.data.Rating;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.EmergencySupply;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.MissionUtil;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Rover;

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
    public Rating getProbability(Person person) {

        Rating missionProbability = Rating.ZERO_RATING;

        if (person.isInSettlement()) {
        		
            Settlement settlement = person.getSettlement();
        	
            missionProbability = new Rating(EmergencySupply.BASE_STARTING_PROBABILITY);
    		
	        // Determine job modifier.
            missionProbability.addModifier(LEADER, getLeaderSuitability(person));
	
	        // Check if person is in a settlement.
	        if (missionProbability.getScore() > 0D) {

	            Rover rover = RoverMission.getVehicleWithGreatestRange(settlement, false);
	            if (rover != null) {
	                Settlement targetSettlement = EmergencySupply.findSettlementNeedingEmergencySupplies(
	                        settlement, rover);
	                if (targetSettlement == null) {
	                    return Rating.ZERO_RATING;
	                }
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
	    	        return Rating.ZERO_RATING;
	    	    }
	
	        	missionProbability.addModifier(SETTLEMENT_POPULATION,
									getSettlementPopModifier(settlement, 8));
	    		
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
        }

        return missionProbability;
    }
}
