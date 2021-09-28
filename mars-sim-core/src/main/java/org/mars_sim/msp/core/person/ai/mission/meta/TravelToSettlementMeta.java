/**
 * Mars Simulation Project
 * TravelToSettlementMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.Iterator;
import java.util.Map;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A meta mission for the TravelToSettlement mission.
 */
public class TravelToSettlementMeta extends AbstractMetaMission {
    
    private static final double LIMIT = 0;

	public TravelToSettlementMeta() {
    	super(MissionType.TRAVEL_TO_SETTLEMENT, "travelToSettlement");
    }
    
    @Override
    public Mission constructInstance(Person person) {
        return new TravelToSettlement(person);
    }

    @Override
    public double getProbability(Person person) {

    	if (marsClock.getMissionSol() < 28)
    		return 0;
    	
        double missionProbability = 0D;

        if (person.isInSettlement()) {
            // Check if mission is possible for person based on their
            // circumstance.
            Settlement settlement = person.getSettlement();

            missionProbability = getMissionProbability(settlement, person);

    		if (missionProbability <= 0)
    			return 0;
    		
	        // Job modifier.
	        JobType job = person.getMind().getJob();
	        if (job != null)
	        	missionProbability *= JobUtil.getJobSpec(job).getStartMissionProbabilityModifier(
	                    TravelToSettlement.class)* settlement.getGoodsManager().getTourismFactor();
			
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
		 
        return missionProbability;
    }

    private double getMissionProbability(Settlement settlement, Unit unit) {
    	Person person = null;
    	Robot robot = null;

        double missionProbability = 0;
        
        if (settlement.getMissionBaseProbability(MissionType.TRAVEL_TO_SETTLEMENT))
        	missionProbability = 1;
        else
			return 0;
 
        // Check if there are any desirable settlements within range.
        double topSettlementDesirability = 0D;
        Vehicle vehicle = RoverMission.getVehicleWithGreatestRange(MissionType.TRAVEL_TO_SETTLEMENT, settlement, false);
        if (vehicle != null) {
        	Map<Settlement, Double> desirableSettlements = null;
			if (unit instanceof Person) {
				person = (Person) unit;
	            desirableSettlements = TravelToSettlement.getDestinationSettlements(
	                    person, settlement, vehicle.getRange(MissionType.TRAVEL_TO_SETTLEMENT));

			}
			else if (unit instanceof Robot) {
				robot = (Robot) unit;
	            desirableSettlements = TravelToSettlement.getDestinationSettlements(
	                    robot, settlement, vehicle.getRange(MissionType.TRAVEL_TO_SETTLEMENT));
			}

            if (desirableSettlements.size() == 0) {
            	return 0;
            }

            Iterator<Settlement> i = desirableSettlements.keySet().iterator();
            while (i.hasNext()) {
                Settlement desirableSettlement = i.next();
                double desirability = desirableSettlements.get(desirableSettlement);
                if (desirability > topSettlementDesirability) {
                    topSettlementDesirability = desirability;
                }
            }
        }


        // Determine mission probability.

        missionProbability = TravelToSettlement.BASE_MISSION_WEIGHT
                + (topSettlementDesirability / 100D);

		int numEmbarked = VehicleMission.numEmbarkingMissions(settlement);
		int numThisMission = Simulation.instance().getMissionManager().numParticularMissions(MissionType.TRAVEL_TO_SETTLEMENT, settlement);

   		// Check for # of embarking missions.
		if (Math.max(1, settlement.getNumCitizens() / 8.0) < numEmbarked + numThisMission) {
			return 0;
		}			
		
		else if (numThisMission > 1)
			return 0;	
		

		int f1 = 2*numEmbarked + 1;
		int f2 = 2*numThisMission + 1;
		
		missionProbability *= settlement.getNumCitizens() / f1 / f2 / 2D * ( 1 + settlement.getMissionDirectiveModifier(MissionType.TRAVEL_TO_SETTLEMENT));
		
        // Crowding modifier.
        int crowding = settlement.getIndoorPeopleCount()
                - settlement.getPopulationCapacity();
        if (crowding > 0) {
            missionProbability *= (crowding + 1);
        }

        return missionProbability;
    }

}
