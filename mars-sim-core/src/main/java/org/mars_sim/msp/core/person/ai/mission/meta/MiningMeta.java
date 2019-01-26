/**
 * Mars Simulation Project
 * MiningMeta.java
 * @version 3.1.0 2017-05-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.Bag;
import org.mars_sim.msp.core.mars.ExploredLocation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A meta mission for the Mining mission.
 */
public class MiningMeta implements MetaMission {

    /** Mission name */
    private static final String NAME = Msg.getString(
            "Mission.description.mining"); //$NON-NLS-1$

    /** default logger. */
    private static Logger logger = Logger.getLogger(MiningMeta.class.getName());

    @Override
    public String getName() {
        return NAME;
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

            missionProbability = settlement.getMissionBaseProbability();
       		if (missionProbability == 0)
    			return 0;
       		
            // Check if there are enough bags at the settlement for collecting minerals.
            if (settlement.getInventory().findNumEmptyUnitsOfClass(Bag.class, false) < Mining.NUMBER_OF_BAGS)
            	return 0;

            // Check if available light utility vehicles.
            //boolean reservableLUV =
            else if (!Mining.isLUVAvailable(settlement))
            	return 0;

            // Check if LUV attachment parts available.
            //boolean availableAttachmentParts =
            else if (!Mining.areAvailableAttachmentParts(settlement))
            	return 0;

			int numEmbarked = VehicleMission.numEmbarkingMissions(settlement);
			int numThisMission = missionManager.numParticularMissions(NAME, settlement);
	
    		// Check for embarking missions.
    		if (settlement.getNumCitizens() / 4.0 < numEmbarked + numThisMission) {
    			return 0;
    		}	
    		
    		else if (numThisMission > 1)
    			return 0;
    		
            try {
                // Get available rover.
                Rover rover = (Rover) RoverMission.getVehicleWithGreatestRange(
                        settlement, false);

                if (rover != null) {
                    // Find best mining site.
                    ExploredLocation miningSite = Mining.determineBestMiningSite(
                            rover, settlement);
                    if (miningSite != null) {
                        missionProbability = Mining.getMiningSiteValue(miningSite, settlement);
                        if (missionProbability > 5D) {
                            missionProbability = 5D;
                        }
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error getting mining site.", e);
            }

            // Crowding modifier
            int crowding = settlement.getIndoorPeopleCount()
                    - settlement.getPopulationCapacity();
            if (crowding > 0) {
                missionProbability *= (crowding + 1);
            }

			int f1 = numEmbarked + 1;
			int f2 = numThisMission + 1;
			
			missionProbability *= settlement.getNumCitizens() / f1 / f2 / 2D;
			
            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) {
				// It this town has a tourist objective, add bonus
                missionProbability *= job.getStartMissionProbabilityModifier(Mining.class)
                		* (settlement.getGoodsManager().getTourismFactor()
                  		 + settlement.getGoodsManager().getResearchFactor())/1.5;
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