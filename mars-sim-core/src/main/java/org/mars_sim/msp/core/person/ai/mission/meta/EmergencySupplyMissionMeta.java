/**
 * Mars Simulation Project
 * EmergencySupplyMissionMeta.java
 * @version 3.07 2014-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.EmergencySupplyMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A meta mission for the EmergencySupplyMission mission.
 */
public class EmergencySupplyMissionMeta implements MetaMission {

    /** Mission name */
    private static final String NAME = Msg.getString(
            "Mission.description.emergencySupplyMission"); //$NON-NLS-1$
    
    /** default logger. */
    private static Logger logger = Logger.getLogger(EmergencySupplyMissionMeta.class.getName());
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Mission constructInstance(Person person) {
        return new EmergencySupplyMission(person);
    }

    @Override
    public double getProbability(Person person) {
        
        double missionProbability = 0D;

        // Determine job modifier.
        Job job = person.getMind().getJob();
        double jobModifier = 0D;
        if (job != null) {
            jobModifier = job.getStartMissionProbabilityModifier(EmergencySupplyMission.class);
        }

        // Check if person is in a settlement.
        boolean inSettlement = person.getLocationSituation() == LocationSituation.IN_SETTLEMENT;

        if (inSettlement && (jobModifier > 0D)) {
            
            // Check if mission is possible for person based on their circumstance.
            boolean missionPossible = true;
            Settlement settlement = person.getSettlement();

            // Check if available rover.
            if (!RoverMission.areVehiclesAvailable(settlement, false)) {
                missionPossible = false;
            }

            // Check if available backup rover.
            if (!RoverMission.hasBackupRover(settlement)) {
                missionPossible = false;
            }

            // Check if minimum number of people are available at the settlement.
            // Plus one to hold down the fort.
            if (!RoverMission.minAvailablePeopleAtSettlement(settlement, RoverMission.MIN_PEOPLE + 1)) {
                missionPossible = false;
            }

            // Check if minimum number of EVA suits at settlement.
            if (Mission.getNumberAvailableEVASuitsAtSettlement(settlement) < RoverMission.MIN_PEOPLE) {
                missionPossible = false;
            }
            
            // Check for embarking missions.
            if (VehicleMission.hasEmbarkingMissions(settlement)) {
                missionPossible = false;
            }

            // Check if settlement has enough basic resources for a rover mission.
            if (!RoverMission.hasEnoughBasicResources(settlement)) {
                missionPossible = false;
            }
            
            if (missionPossible) {
                
                Rover rover = (Rover) RoverMission.getVehicleWithGreatestRange(settlement, false);
                if (rover != null) {
                    Settlement targetSettlement = EmergencySupplyMission.findSettlementNeedingEmergencySupplies(
                            settlement, rover);
                    if (targetSettlement == null) {
                        missionPossible = false;
                    }
                }
            }
            
            if (missionPossible) {
                missionProbability = EmergencySupplyMission.BASE_STARTING_PROBABILITY;
                
                // Crowding modifier.
                int crowding = settlement.getCurrentPopulationNum() - settlement.getPopulationCapacity();
                if (crowding > 0) missionProbability *= (crowding + 1);
                
                // Job modifier.
                missionProbability *= jobModifier;
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