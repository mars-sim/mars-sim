/*
 * Mars Simulation Project
 * MiningMeta.java
 * @date 2022-07-14
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A meta mission for the Mining mission.
 */
public class MiningMeta extends AbstractMetaMission {

    /** default logger. */
    private static final Logger logger = Logger.getLogger(MiningMeta.class.getName());

    MiningMeta() {
    	super(MissionType.MINING, Set.of(JobType.AREOLOGIST, JobType.ENGINEER));
    }

    @Override
    public Mission constructInstance(Person person, boolean needsReview) {
        return new Mining(person, needsReview);
    }

    @Override
    public double getProbability(Person person) {

        double missionProbability = 0D;

        if (person.isInSettlement()) {

        	Settlement settlement = person.getSettlement();

            RoleType roleType = person.getRole().getType();

 			if (RoleType.CHIEF_OF_SCIENCE == roleType
 					|| RoleType.SCIENCE_SPECIALIST == roleType
 					|| RoleType.CHIEF_OF_SUPPLY_N_RESOURCES == roleType
 					|| RoleType.RESOURCE_SPECIALIST == roleType
 					|| RoleType.MISSION_SPECIALIST == roleType
 					|| RoleType.CHIEF_OF_MISSION_PLANNING == roleType
 					|| RoleType.COMMANDER == roleType
 					|| RoleType.SUB_COMMANDER == roleType
 					) {

	            // Check if there are enough bags at the settlement for collecting minerals.
	            if (settlement.findNumContainersOfType(EquipmentType.LARGE_BAG) < Mining.NUMBER_OF_LARGE_BAGS)
	            	return 0;

	            // Check if available light utility vehicles.
	            //boolean reservableLUV =
	            if (!Mining.isLUVAvailable(settlement))
	            	return 0;

	            // Check if LUV attachment parts available.
	            //boolean availableAttachmentParts =
	            if (!Mining.areAvailableAttachmentParts(settlement))
	            	return 0;

				missionProbability = getSettlementPopModifier(settlement, 8);
				if (missionProbability == 0) {
	    			return 0;
	    		}

	            try {
	                // Get available rover.
	                Rover rover = RoverMission.getVehicleWithGreatestRange(settlement, false);

	                if (rover != null) {
	                    // Find best mining site.
	                	missionProbability *= Mining.getMatureMiningSitesTotalScore(rover, settlement);
	                }
	            } catch (Exception e) {
	                logger.log(Level.SEVERE, "Error getting mining site.", e);
	                return 0;
	            }

	            // Crowding modifier
	            int crowding = settlement.getIndoorPeopleCount()
	                    - settlement.getPopulationCapacity();
	            if (crowding > 0) {
	                missionProbability *= (crowding + 1);
	            }

	            // Job modifier.
				missionProbability *= getLeaderSuitability(person)
	                		* (settlement.getGoodsManager().getTourismFactor()
	                  		 + settlement.getGoodsManager().getResearchFactor())/1.5;

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
        }

        return missionProbability;
    }
}
