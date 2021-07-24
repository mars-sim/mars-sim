/**
 * Mars Simulation Project
 * MiningMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.mars.ExploredLocation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A meta mission for the Mining mission.
 */
public class MiningMeta implements MetaMission {

    /** Mission name */
    private static final String DEFAULT_DESCRIPTION = Msg.getString(
            "Mission.description.mining"); //$NON-NLS-1$

    /** default logger. */
    private static final Logger logger = Logger.getLogger(MiningMeta.class.getName());

	private static final double FACTOR = 200D;
	  
    @Override
    public String getName() {
        return DEFAULT_DESCRIPTION;
    }

    @Override
    public Mission constructInstance(Person person) {
        return new Mining(person);
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
        	
	            if (settlement.getMissionBaseProbability(DEFAULT_DESCRIPTION))
	            	missionProbability = 1;
	            else
	    			return 0;
	       		
	            // Check if there are enough bags at the settlement for collecting minerals.
	            if (settlement.getInventory().findNumBags(true, true) < Mining.NUMBER_OF_LARGE_BAGS)
	            	return 0;
	
	            // Check if available light utility vehicles.
	            //boolean reservableLUV =
	            if (!Mining.isLUVAvailable(settlement))
	            	return 0;
	
	            // Check if LUV attachment parts available.
	            //boolean availableAttachmentParts =
	            if (!Mining.areAvailableAttachmentParts(settlement))
	            	return 0;
	
				int numEmbarked = VehicleMission.numEmbarkingMissions(settlement);
				int numThisMission = missionManager.numParticularMissions(DEFAULT_DESCRIPTION, settlement);
		
		   		// Check for # of embarking missions.
	    		if (Math.max(1, settlement.getNumCitizens() / 6.0) < numEmbarked + numThisMission) {
	    			return 0;
	    		}	
	    		
	    		if (numThisMission > 1)
	    			return 0;
	    		
	    		missionProbability = 0;
	    		
	            try {
	                // Get available rover.
	                Rover rover = (Rover) RoverMission.getVehicleWithGreatestRange(Mining.missionType,
	                        settlement, false);
	
	                if (rover != null) {
	                    // Find best mining site.
	                    ExploredLocation miningSite = Mining.determineBestMiningSite(
	                            rover, settlement);
	                    if (miningSite != null) {
	                        missionProbability = Mining.getMiningSiteValue(miningSite, settlement);
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
	
				int f1 = 2*numEmbarked + 1;
				int f2 = 2*numThisMission + 1;
				
				missionProbability *= settlement.getNumCitizens() / f1 / f2 / 2D * ( 1 + settlement.getMissionDirectiveModifier(MissionType.MINING));
				
	            // Job modifier.
	            JobType job = person.getMind().getJob();
	            if (job != null) {
					// It this town has a tourist objective, add bonus
	                missionProbability *= JobUtil.getStartMissionProbabilityModifier(job, Mining.class)
	                		* (settlement.getGoodsManager().getTourismFactor()
	                  		 + settlement.getGoodsManager().getResearchFactor())/1.5;
	            }
	
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

//        if (missionProbability > 0)
//        	logger.info("MiningMeta's probability : " +
//				 Math.round(missionProbability*100D)/100D);
		 
        return missionProbability;
    }

	@Override
	public Mission constructInstance(Robot robot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProbability(Robot robot) {
		// TODO Auto-generated method stub
		return 0;
	}
}
