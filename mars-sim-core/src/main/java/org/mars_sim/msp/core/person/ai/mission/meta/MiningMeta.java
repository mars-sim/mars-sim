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

import org.mars_sim.msp.core.environment.ExploredLocation;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.MissionUtil;
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
    	super(MissionType.MINING, Set.of(JobType.AREOLOGIST));
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

				int numEmbarked = MissionUtil.numEmbarkingMissions(settlement);
				int numThisMission = missionManager.numParticularMissions(MissionType.MINING, settlement);

		   		// Check for # of embarking missions.
	    		if (Math.max(1, settlement.getNumCitizens() / 6.0) < numEmbarked + numThisMission) {
	    			return 0;
	    		}

	    		if (numThisMission > 1)
	    			return 0;

	    		missionProbability = 0;

	            try {
	                // Get available rover.
	                Rover rover = (Rover) RoverMission.getVehicleWithGreatestRange(MissionType.MINING,
	                        settlement, false);

	                if (rover != null) {
	                    // Find best mining site.
	                    ExploredLocation miningSite = Mining.determineBestMiningSite(
	                            rover, settlement);
	                    if (miningSite != null) {
	                        missionProbability = 1.5 * Mining.getMiningSiteValue(miningSite, settlement);
	    					if (missionProbability < 0)
	    						missionProbability = 0;
	                    }
	                    else // no mining site can be identified
	                    	return 0;
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

				int f1 = numEmbarked + 1;
				int f2 = 2 * numThisMission + 1;

				missionProbability *= (double)settlement.getNumCitizens() / f1 / f2;

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
				missionProbability += extrovert;

				if (missionProbability < 0)
					missionProbability = 0;
 			}
        }

        return missionProbability;
    }
}
