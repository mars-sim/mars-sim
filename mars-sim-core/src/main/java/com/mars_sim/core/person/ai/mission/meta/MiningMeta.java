/*
 * Mars Simulation Project
 * MiningMeta.java
 * @date 2025-07-06
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission.meta;

import java.util.Set;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.Mining;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.RoverMission;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Rover;

/**
 * A meta mission for the Mining mission.
 */
public class MiningMeta extends AbstractMetaMission {
	
	/** Starting sol for this mission to commence. */
	public static final int MIN_STARTING_SOL = 4;
	/** The multiplier factor. */
	private static final double FACTOR = 5.0;
	
    MiningMeta() {
    	super(MissionType.MINING, Set.of(JobType.AREOLOGIST, JobType.ENGINEER));
    }

    @Override
    public Mission constructInstance(Person person, boolean needsReview) {
        return new Mining(person, needsReview);
    }

    @Override
    public RatingScore getProbability(Person person) {

        RatingScore missionProbability = RatingScore.ZERO_RATING;
    	if (getMarsTime().getMissionSol() < MIN_STARTING_SOL) {
    		return RatingScore.ZERO_RATING;
    	}
    	
        if (person.isInSettlement()) {

        	Settlement settlement = person.getSettlement();

    		if (settlement.isFirstSol())
    			return missionProbability;
    		
            RoleType roleType = person.getRole().getType();

 			if (RoleType.CHIEF_OF_SCIENCE == roleType
 					|| RoleType.SCIENCE_SPECIALIST == roleType
 					|| RoleType.CHIEF_OF_SUPPLY_RESOURCE == roleType
 					|| RoleType.RESOURCE_SPECIALIST == roleType
 					|| RoleType.MISSION_SPECIALIST == roleType
 					|| RoleType.CHIEF_OF_MISSION_PLANNING == roleType
 					|| RoleType.COMMANDER == roleType
 					|| RoleType.SUB_COMMANDER == roleType
 					) {

	            // Check if there are enough bags at the settlement for collecting minerals.
	            if (settlement.findNumContainersOfType(EquipmentType.LARGE_BAG) < Mining.NUMBER_OF_LARGE_BAGS)
	            	return RatingScore.ZERO_RATING;

	            // Check if available light utility vehicles.
	            //boolean reservableLUV =
	            if (!Mining.isLUVAvailable(settlement))
	            	return RatingScore.ZERO_RATING;

	            // Check if LUV attachment parts available.
	            //boolean availableAttachmentParts =
	            if (!Mining.areAvailableAttachmentParts(settlement))
	            	return RatingScore.ZERO_RATING;

				missionProbability = new RatingScore(1D);
	
				// Get available rover.
				Rover rover = RoverMission.getVehicleWithGreatestRange(settlement, false);

				if (rover != null) {
					// Find best mining site.
					missionProbability.addModifier("miningmaturity",
										Mining.getMatureMiningSitesTotalScore(rover, settlement) * FACTOR);
				}

	            // Crowding modifier
	            int crowding = settlement.getIndoorPeopleCount()
	                    - settlement.getPopulationCapacity();
	            if (crowding > 0) {
	                missionProbability.addModifier(OVER_CROWDING, (crowding + 1));
	            }

	            // Job modifier.
				missionProbability.addModifier(LEADER, getLeaderSuitability(person));
				missionProbability = applyCommerceAverage(missionProbability, settlement, CommerceType.TOURISM,
													CommerceType.RESEARCH);


				// if introvert, score  0 to  50 --> -2 to 0
				// if extrovert, score 50 to 100 -->  0 to 2
				// Reduce probability if introvert
				int extrovert = person.getExtrovertmodifier();
				missionProbability.addModifier(PERSON_EXTROVERT, (1 + extrovert/2.0));

				missionProbability.applyRange(0, LIMIT);
 			}
        }

        return missionProbability;
    }
}
