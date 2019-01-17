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
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.equipment.Bag;
import org.mars_sim.msp.core.mars.ExploredLocation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.ResourceUtil;
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

        double result = 0D;

        if (person.isInSettlement()) {
        	
        	Settlement settlement = person.getSettlement();

            // Check if a mission-capable rover is available.
            //boolean reservableRover =
            if (!RoverMission.areVehiclesAvailable(settlement, false))
                return 0;

            // Check if available backup rover.
            //boolean backupRover =
            else if (!RoverMission.hasBackupRover(settlement))
        		return 0;

    	    // Check if minimum number of people are available at the settlement.
            else if (!RoverMission.minAvailablePeopleAtSettlement(settlement, RoverMission.MIN_STAYING_MEMBERS)) {
    	        return 0;
    	    }

    	    // Check if min number of EVA suits at settlement.
            else if (Mission.getNumberAvailableEVASuitsAtSettlement(settlement) < RoverMission.MIN_GOING_MEMBERS) {
    	        return 0;
    	    }
            // Check if there are enough bags at the settlement for collecting minerals.
            //boolean enoughBags = false;

            else if (settlement.getInventory().findNumEmptyUnitsOfClass(Bag.class, false) < Mining.NUMBER_OF_BAGS)
            //int numBags = settlement.getInventory().findNumEmptyUnitsOfClass(Bag.class, false);
            //enoughBags = (numBags >= Mining.NUMBER_OF_BAGS);
        		return 0;

            // Check for embarking missions.
            //boolean embarkingMissions =
//            else if (VehicleMission.hasEmbarkingMissions(settlement))
//    			return 0;

            // Check if settlement has enough basic resources for a rover mission.
            //boolean hasBasicResources =
            else if (!RoverMission.hasEnoughBasicResources(settlement, true))
            	return 0;

            // Check if available light utility vehicles.
            //boolean reservableLUV =
            else if (!Mining.isLUVAvailable(settlement))
            	return 0;

            // Check if LUV attachment parts available.
            //boolean availableAttachmentParts =
            else if (!Mining.areAvailableAttachmentParts(settlement))
            	return 0;
            // Check if starting settlement has minimum amount of methane fuel.

            else if (settlement.getInventory().getAmountResourceStored(ResourceUtil.methaneID, false) <
                    RoverMission.MIN_STARTING_SETTLEMENT_METHANE) {
            	return 0;
            }
            
			int numEmbarked = VehicleMission.numEmbarkingMissions(settlement);
			int numThisMission = Simulation.instance().getMissionManager().numParticularMissions(NAME, settlement);
	
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
                        result = Mining.getMiningSiteValue(miningSite, settlement);
                        if (result > 5D) {
                            result = 5D;
                        }
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error getting mining site.", e);
            }

            // Crowding modifier
//            int crowding = settlement.getIndoorPeopleCount()
//                    - settlement.getPopulationCapacity();
//            if (crowding > 0) {
//                result *= (crowding + 1);
//            }

			int f1 = numEmbarked + 1;
			int f2 = numThisMission + 1;
			
			result *= settlement.getNumCitizens() / f1 / f2 / 2D;
			
            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) {
				// It this town has a tourist objective, add bonus
                result *= job.getStartMissionProbabilityModifier(Mining.class)
                		* (settlement.getGoodsManager().getTourismFactor()
                  		 + settlement.getGoodsManager().getResearchFactor())/1.5;
            }

        }

        if (result > 0)
        	logger.info("MiningMeta's probability : " +
				 Math.round(result*100D)/100D);
		 
        return result;
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