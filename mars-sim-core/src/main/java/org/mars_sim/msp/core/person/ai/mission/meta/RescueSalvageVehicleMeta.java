/**
 * Mars Simulation Project
 * RescueSalvageVehicleMeta.java
 * @version 3.07 2014-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
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
        
        double missionProbability = 0D;
        
        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            
            // Check if mission is possible for person based on their circumstance.
            boolean missionPossible = true;
            
            Settlement settlement = person.getParkedSettlement();
        
            Vehicle vehicleTarget = null;
            
            boolean rescue = false;
            
            // Check if there are any beacon vehicles within range that need help.
            try {
                Vehicle vehicle = RoverMission.getVehicleWithGreatestRange(settlement, true);
                if (vehicle != null) {
                    vehicleTarget = RescueSalvageVehicle.findAvailableBeaconVehicle(settlement, 
                            vehicle.getRange());
                    if (vehicleTarget == null) {
                        missionPossible = false;
                    }
                    else if (!RescueSalvageVehicle.isClosestCapableSettlement(settlement, vehicleTarget)) {
                        missionPossible = false;
                    }
                }
            }
            catch (Exception e) {}
           
            if (!missionPossible)
            ;
            // Check if available rover.
            else if (!RoverMission.areVehiclesAvailable(settlement, true)) {
                missionPossible = false;
            }
            
            // Check if min number of EVA suits at settlement.
            else if (Mission.getNumberAvailableEVASuitsAtSettlement(settlement) < 
                    RescueSalvageVehicle.MISSION_MIN_MEMBERS) {
                missionPossible = false;
            }
            
            // Check if person is last remaining person at settlement (for salvage mission but not rescue mission).
            // Also check for backup rover for salvage mission.
            //boolean rescue = false;
            else if (vehicleTarget != null) {
                rescue = (RescueSalvageVehicle.getRescuePeopleNum(vehicleTarget) > 0);
                if (rescue) {
                    // if (!atLeastOnePersonRemainingAtSettlement(settlement, person)) missionPossible = false;
                }
                else {
                    // Check if minimum number of people are available at the settlement.
                    // Plus one to hold down the fort.
                    if (!RoverMission.minAvailablePeopleAtSettlement(settlement, 
                            (RescueSalvageVehicle.MISSION_MIN_MEMBERS + 1))) {
                        missionPossible = false;
                    }
                    
                    // Check if available backup rover.
                    else if (!RoverMission.hasBackupRover(settlement)) {
                        missionPossible = false;
                    }
                }
            }
            
            // Check for embarking missions.
            else if (VehicleMission.hasEmbarkingMissions(settlement)) {
                missionPossible = false;
            }
             
            // Determine mission probability.
            if (missionPossible) {
                if (rescue) {
                    missionProbability = RescueSalvageVehicle.BASE_RESCUE_MISSION_WEIGHT;
                }
                else {
                    missionProbability = RescueSalvageVehicle.BASE_SALVAGE_MISSION_WEIGHT;
                }
                
                // Crowding modifier.
                int crowding = settlement.getCurrentPopulationNum() - settlement.getPopulationCapacity();
                if (crowding > 0) {
                    missionProbability *= (crowding + 1);
                }
                
                // Job modifier.
                Job job = person.getMind().getJob();
                if (job != null) {
                    missionProbability *= job.getStartMissionProbabilityModifier(RescueSalvageVehicle.class);  
                }
            }
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