/**
 * Mars Simulation Project
 * EmergencySupplyMissionMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.person.ai.mission.EmergencySupply;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A meta mission for the EmergencySupplyMission mission.
 */
public class EmergencySupplyMissionMeta implements MetaMission {

    /** default logger. */
    //private static Logger logger = Logger.getLogger(EmergencySupplyMissionMeta.class.getName());
   
    /** Mission name */
    private static final String DEFAULT_DESCRIPTION = Msg.getString(
            "Mission.description.emergencySupply"); //$NON-NLS-1$

    @Override
    public String getName() {
        return DEFAULT_DESCRIPTION;
    }

    @Override
    public Mission constructInstance(Person person) {
        return new EmergencySupply(person);
    }

    @Override
    public double getProbability(Person person) {

        double missionProbability = 0D;

        if (person.isInSettlement()) {
        		
            Settlement settlement = person.getSettlement();
        	
            if (settlement.getMissionBaseProbability(DEFAULT_DESCRIPTION))
            	missionProbability = 1;
            else
    			return 0;
    		
	        // Determine job modifier.
	        JobType job = person.getMind().getJob();
	        double jobModifier = 0D;
	        if (job != null) {
	            jobModifier = JobUtil.getStartMissionProbabilityModifier(job, EmergencySupply.class);
	        }
	
	        // Check if person is in a settlement.
	        if (jobModifier > 0D) {

	            Rover rover = (Rover) RoverMission.getVehicleWithGreatestRange(EmergencySupply.missionType, settlement, false);
	            if (rover != null) {
	                Settlement targetSettlement = EmergencySupply.findSettlementNeedingEmergencySupplies(
	                        settlement, rover);
	                if (targetSettlement == null) {
	                    return 0;
	                }
	            }
	            
	            int min_num = 0;
	            int all = settlement.getNumCitizens();
	            if (all == 2)
	            	min_num = 0;
	            else 
	            	min_num = RoverMission.MIN_STAYING_MEMBERS;
	    	    

	            if (all == 2)
	            	min_num = 1;
	            else
	            	min_num = RoverMission.MIN_GOING_MEMBERS;
	    	    
	            // Check if min number of EVA suits at settlement.
	            if (Mission.getNumberAvailableEVASuitsAtSettlement(settlement) < min_num) {
	    	        return 0;
	    	    }
	
	            missionProbability = EmergencySupply.BASE_STARTING_PROBABILITY;
	
	    		int numEmbarked = VehicleMission.numEmbarkingMissions(settlement);	
	    		int numThisMission = missionManager.numParticularMissions(DEFAULT_DESCRIPTION, settlement);
	    		
		   		// Check for # of embarking missions.
	    		if (Math.max(1, settlement.getNumCitizens() / 8.0) < numEmbarked + numThisMission) {
	    			return 0;
	    		}	
	    		
	    		if (numThisMission > 1)
	    			return 0;	

	    		if (missionProbability <= 0)
	    			return 0;
	    		
	    		int f1 = 2*numEmbarked + 1;
	    		int f2 = 2*numThisMission + 1;
	    		
	    		missionProbability *= settlement.getNumCitizens() / f1 / f2 / 2D;
	    		
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
	            if (crowding > 0) missionProbability *= (crowding + 1);
	
	            // Job modifier.
	            missionProbability *= jobModifier;
	
				if (missionProbability > LIMIT)
					missionProbability = LIMIT;
				else if (missionProbability < 0)
					missionProbability = 0;
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
