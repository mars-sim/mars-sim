/**
 * Mars Simulation Project
 * EmergencySupplyMissionMeta.java
 * @version 3.1.0 2017-09-15
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import org.mars_sim.msp.core.Msg;
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
    //private static Logger logger = Logger.getLogger(EmergencySupplyMissionMeta.class.getName());

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

        if (person.isInSettlement()) {
        		
	        // Determine job modifier.
	        Job job = person.getMind().getJob();
	        double jobModifier = 0D;
	        if (job != null) {
	            jobModifier = job.getStartMissionProbabilityModifier(EmergencySupplyMission.class);
	        }
	
	        // Check if person is in a settlement.
	        if (jobModifier > 0D) {

	            Settlement settlement = person.getSettlement();
	
	            // Check if available rover.
	            if (!RoverMission.areVehiclesAvailable(settlement, false)) {
	                return 0;
	            }
	
	            // Check if available backup rover.
	            if (!RoverMission.hasBackupRover(settlement)) {
	                return 0;
	            }
	
	            int min_num = 0;
	            int all = settlement.getNumCitizens();
	            if (all == 2)
	            	min_num = 0;
	            else 
	            	min_num = RoverMission.MIN_STAYING_MEMBERS;
	    	    
	    	    // Check if minimum number of people are available at the settlement.
	            if (!RoverMission.minAvailablePeopleAtSettlement(settlement, min_num)) {
	    	        return 0;
	    	    }
	
	            if (all == 2)
	            	min_num = 1;
	            else
	            	min_num = RoverMission.MIN_GOING_MEMBERS;
	    	    
	            // Check if min number of EVA suits at settlement.
	            if (Mission.getNumberAvailableEVASuitsAtSettlement(settlement) < min_num) {
	    	        return 0;
	    	    }
	
	            // Check for embarking missions.
	            if (VehicleMission.hasEmbarkingMissions(settlement)) {
	                return 0;
	            }
	
	            // Check if settlement has enough basic resources for a rover mission.
	            if (!RoverMission.hasEnoughBasicResources(settlement, false)) {
	                return 0;
	            }
	
	            Rover rover = (Rover) RoverMission.getVehicleWithGreatestRange(settlement, false);
	            if (rover != null) {
	                Settlement targetSettlement = EmergencySupplyMission.findSettlementNeedingEmergencySupplies(
	                        settlement, rover);
	                if (targetSettlement == null) {
	                    return 0;
	                }
	            }
	
	            missionProbability = EmergencySupplyMission.BASE_STARTING_PROBABILITY;
	
	            // Crowding modifier.
	            int crowding = settlement.getIndoorPeopleCount() - settlement.getPopulationCapacity();
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