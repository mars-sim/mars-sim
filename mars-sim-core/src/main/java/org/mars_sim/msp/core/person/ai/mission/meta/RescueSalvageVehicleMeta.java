/**
 * Mars Simulation Project
 * RescueSalvageVehicleMeta.java
 * @version 3.1.0 2017-04-19
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A meta mission for the RescueSalvageVehicle mission.
 */
public class RescueSalvageVehicleMeta implements MetaMission {

    private static final double LIMIT = 100D;
    
    /** Mission name */
    private static final String DEFAULT_DESCRIPTION = Msg.getString(
            "Mission.description.rescueSalvageVehicle"); //$NON-NLS-1$

    @Override
    public String getName() {
        return DEFAULT_DESCRIPTION;
    }

    @Override
    public Mission constructInstance(Person person) {
        return new RescueSalvageVehicle(person);
    }

    @Override
    public double getProbability(Person person) {

        double missionProbability = 0D;

        if (person.isInSettlement()) {

            Settlement settlement = person.getSettlement();

            Vehicle vehicleTarget = null;

            // Check if there are any beacon vehicles within range that need help.
            try {
                Vehicle vehicle = RoverMission.getVehicleWithGreatestRange(RescueSalvageVehicle.missionType, settlement, true);
                if (vehicle != null) {
                    vehicleTarget = RescueSalvageVehicle.findBeaconVehicle(settlement,
                            vehicle.getRange(RescueSalvageVehicle.missionType));
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
    			e.printStackTrace();
    			return 0;
            }

//            System.out.println("RescueSalvageVehicleMeta - vehicleTarget : " + vehicleTarget.getName()
//            		+ "   missionProbability 1: " + missionProbability);
            
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
            
//            System.out.println("RescueSalvageVehicleMeta - vehicleTarget : " + vehicleTarget.getName()
//    		+ "   missionProbability 2: " + missionProbability);
            
            // Check if min number of EVA suits at settlement.
            if (Mission.getNumberAvailableEVASuitsAtSettlement(settlement) < min_num) {
    	        return 0;
    	    }

            // Check for embarking missions.
            else if (!VehicleMission.hasEmbarkingMissions(settlement)) {
                return missionProbability * 2;
            }
   
            // Check if minimum number of people are available at the settlement.
            if (!RoverMission.minAvailablePeopleAtSettlement(settlement, min_num)) {
                return 0;
            }

            // Check if available backup rover.
            else if (!RoverMission.hasBackupRover(settlement)) {
                return 0;
            }

//            System.out.println("RescueSalvageVehicleMeta - vehicleTarget : " + vehicleTarget.getName()
//    		+ "   missionProbability 3: " + missionProbability);
            
    		if (missionProbability <= 0)
    			return 0;
    		
			int numEmbarked = VehicleMission.numEmbarkingMissions(settlement);
			int numThisMission = missionManager.numParticularMissions(DEFAULT_DESCRIPTION, settlement);
	
//            System.out.println("RescueSalvageVehicleMeta - vehicleTarget : " + vehicleTarget.getName()
//    		+ "   missionProbability 4: " + missionProbability);
            
	   		// Check for # of embarking missions.
    		if (Math.max(1, settlement.getNumCitizens() / 8.0) < numThisMission + numEmbarked) {
    			return 0;
    		}	
    		
    		if (numThisMission > 0)
    			return 0;	

			int f1 = 2 * numEmbarked + 1;
			int f2 = numThisMission + 1;
			
			missionProbability = (1 + missionProbability) * settlement.getNumCitizens() / f2 / f1;
			
            // Crowding modifier.
            int crowding = settlement.getIndoorPeopleCount() - settlement.getPopulationCapacity();
            if (crowding > 0) {
                missionProbability *= (crowding + 1);
            }

//            System.out.println("RescueSalvageVehicleMeta - vehicleTarget : " + vehicleTarget.getName()
//    		+ "   missionProbability 5: " + missionProbability);
            
            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) {
                missionProbability *= job.getStartMissionProbabilityModifier(RescueSalvageVehicle.class);
            }

			if (missionProbability > LIMIT)
				missionProbability = LIMIT;
			else if (missionProbability < 0)
				missionProbability = 0;
			
//	        System.out.println("RescueSalvageVehicleMeta - probability : " + missionProbability + " at " + settlement.getName());
        }

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