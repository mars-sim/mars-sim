/*
 * Mars Simulation Project
 * RescueSalvageVehicleMeta.java
 * @date 2021-08-28
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.logging.Level;

import org.mars_sim.msp.core.logging.SimLogger;
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
   
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(RescueSalvageVehicleMeta.class.getName());

    RescueSalvageVehicleMeta() {
    	super(MissionType.RESCUE_SALVAGE_VEHICLE, null);
    }
  
    @Override
    public Mission constructInstance(Person person, boolean needsReview) {
        return new RescueSalvageVehicle(person, needsReview);
    }

    @Override
    public double getProbability(Person person) {

        double missionProbability = 0D;

        if (person.isInSettlement()) {

            Settlement settlement = person.getSettlement();

            Vehicle vehicleTarget = null;

            // Check if there are any beacon vehicles within range that need help.
            try {
                Vehicle vehicle = RoverMission.getVehicleWithGreatestRange(settlement, true);
                if (vehicle != null) {
                    vehicleTarget = RescueSalvageVehicle.findBeaconVehicle(settlement,
                            vehicle.getRange());
                    if (vehicle == vehicleTarget)
                        return 0;
                    else if (vehicleTarget == null)
                        return 0;
                    else if (!RescueSalvageVehicle.isClosestCapableSettlement(settlement, vehicleTarget))
                        return 0;  
                    
                    missionProbability = (1 + RescueSalvageVehicle.BASE_RESCUE_MISSION_WEIGHT)
                    		* RescueSalvageVehicle.getRescuePeopleNum(vehicleTarget);                  
                }
            }
            catch (Exception e) {
              	logger.log(Level.SEVERE, "Cannot find a vehicle: "+ e.getMessage());
    			return 0;
            }
    
            // Check if available rover.
            if (!RoverMission.areVehiclesAvailable(settlement, true)) {
                return 0;
            }
    
            int min_num = 0;
            int all = settlement.getNumCitizens();
 
            if (all <= 3)
            	min_num = 0;
            else if (all > 3)	
            	min_num = RescueSalvageVehicle.MIN_STAYING_MEMBERS;
    	    
            // FIXME : need to know how many extra EVA suits needed in the broken vehicle
			int numEmbarked = MissionUtil.numEmbarkingMissions(settlement);

            // Check if min number of EVA suits at settlement.
            if (MissionUtil.getNumberAvailableEVASuitsAtSettlement(settlement) < min_num) {
    	        return 0;
    	    }

            // Check for embarking missions.
            else if (numEmbarked == 0) {
                return missionProbability * 2;
            }
   
            // Check if minimum number of people are available at the settlement.
            if (!MissionUtil.minAvailablePeopleAtSettlement(settlement, min_num)) {
                return 0;
            }

            // Check if available backup rover.
            else if (!RoverMission.hasBackupRover(settlement)) {
                return 0;
            }
            
    		if (missionProbability <= 0)
    			return 0;
    		
			int numThisMission = missionManager.numParticularMissions(MissionType.RESCUE_SALVAGE_VEHICLE, settlement);
            
	   		// Check for # of embarking missions.
    		if (Math.max(1, settlement.getNumCitizens() / 8.0) < numThisMission + numEmbarked) {
    			return 0;
    		}	
    		
    		if (numThisMission > 0)
    			return 0;	

			int f1 = 2 * numEmbarked + 1;
			int f2 = numThisMission + 1;
			
			missionProbability = (1 + missionProbability) * settlement.getNumCitizens() / f2 / f1;
			
           	RoleType roleType = person.getRole().getType();
        	
        	if (RoleType.MISSION_SPECIALIST == roleType)
        		missionProbability *= 1.5;
        	else if (RoleType.CHIEF_OF_MISSION_PLANNING == roleType)
        		missionProbability *= 3;
        	else if (RoleType.SUB_COMMANDER == roleType)
        		missionProbability *= 4.5;
        	else if (RoleType.COMMANDER == roleType)
        		missionProbability *= 6;
        	
            // Crowding modifier.
            int crowding = settlement.getIndoorPeopleCount() - settlement.getPopulationCapacity();
            if (crowding > 0) {
                missionProbability *= (crowding + 1);
            }
            
            // Job modifier.
            missionProbability *= getLeaderSuitability(person);

			if (missionProbability > LIMIT)
				missionProbability = LIMIT;
			else if (missionProbability < 0)
				missionProbability = 0;
        }

        return missionProbability;
    }
}
