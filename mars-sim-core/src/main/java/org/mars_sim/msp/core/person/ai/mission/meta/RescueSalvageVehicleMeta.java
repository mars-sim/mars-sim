/**
 * Mars Simulation Project
 * RescueSalvageVehicleMeta.java
 * @version 3.1.0 2017-04-19
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
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

    /** Mission name */
    private static final String NAME = Msg.getString(
            "Mission.description.rescueSalvageVehicle"); //$NON-NLS-1$

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Mission constructInstance(Person person) {
        return new RescueSalvageVehicle(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.isInSettlement()) {

            Settlement settlement = person.getSettlement();

            Vehicle vehicleTarget = null;

            boolean rescuePeople = false;

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
                }
            }
            catch (Exception e) {
    			e.printStackTrace();
    			return 0;
            }

            // Check if available rover.
            if (!RoverMission.areVehiclesAvailable(settlement, true)) {
                return 0;
            }

            
            int min_num = 0;
            int all = settlement.getNumCitizens();
 
            if (all == 2)
            	min_num = 1;
            else
            	min_num = RescueSalvageVehicle.MIN_GOING_MEMBERS;
    	    
            // Check if min number of EVA suits at settlement.
            if (Mission.getNumberAvailableEVASuitsAtSettlement(settlement) < min_num) {
    	        return 0;
    	    }

            // Check for embarking missions.
//            else if (VehicleMission.hasEmbarkingMissions(settlement)) {
//                return 0;
//            }

            // Check if person is last remaining person at settlement (for salvage mission but not rescue mission).
            // Also check for backup rover for salvage mission.
            //boolean rescue = false;
            else if (vehicleTarget != null) {
                rescuePeople = (RescueSalvageVehicle.getRescuePeopleNum(vehicleTarget) > 0);
                if (rescuePeople) {
                    //if (!atLeastOnePersonRemainingAtSettlement(settlement, person))
                    //	return 0;
                	//if (!RoverMission.minAvailablePeopleAtSettlement(settlement, 0))
                    //    return 0;
                }
                else {
                    if (all == 2)
                    	min_num = 0;
                    else 
                    	min_num = RescueSalvageVehicle.MIN_STAYING_MEMBERS;
           	            	
                    // Check if minimum number of people are available at the settlement.
                    if (!RoverMission.minAvailablePeopleAtSettlement(settlement,
                            (RescueSalvageVehicle.MIN_STAYING_MEMBERS))) {
                        return 0;
                    }

                    // Check if available backup rover.
                    else if (!RoverMission.hasBackupRover(settlement)) {
                        return 0;
                    }
                }
            }

            // Determine mission probability.
            if (rescuePeople) {
                result = RescueSalvageVehicle.BASE_RESCUE_MISSION_WEIGHT;
            }
            else {
                result = RescueSalvageVehicle.BASE_SALVAGE_MISSION_WEIGHT;
            }

			int numEmbarked = VehicleMission.numEmbarkingMissions(settlement);
			int numThisMission = missionManager.numParticularMissions(NAME, settlement);
	
    		// Check for embarking missions.
    		if (settlement.getNumCitizens() / 4.0 < numEmbarked + numThisMission) {
    			return 0;
    		}	
    		
    		else if (numThisMission > 1)
    			return 0;	

			int f1 = numEmbarked + 1;
			int f2 = numThisMission + 1;
			
			result *= settlement.getNumCitizens() / f1 / f2 / 2D;
			
            // Crowding modifier.
            int crowding = settlement.getIndoorPeopleCount() - settlement.getPopulationCapacity();
            if (crowding > 0) {
                result *= (crowding + 1);
            }

            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) {
                result *= job.getStartMissionProbabilityModifier(RescueSalvageVehicle.class);
            }

        }

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