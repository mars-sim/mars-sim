/**
 * Mars Simulation Project
 * TravelToSettlementMeta.java
 * @version 3.1.0 2017-01-19
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.Iterator;
import java.util.Map;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A meta mission for the TravelToSettlement mission.
 */
public class TravelToSettlementMeta implements MetaMission {

	/** default logger. */
//	private static Logger logger = Logger.getLogger(TravelToSettlementMeta.class.getName());

    private static final double LIMIT = 1D;
    
    /** Mission name */
    private static final String DEFAULT_DESCRIPTION = Msg.getString(
            "Mission.description.travelToSettlement"); //$NON-NLS-1$
    
    @Override
    public String getName() {
        return DEFAULT_DESCRIPTION;
    }

    @Override
    public Mission constructInstance(Person person) {
        return new TravelToSettlement(person);
    }

    @Override
    public Mission constructInstance(Robot robot) {
        return null;//new TravelToSettlement(robot);
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
	        Job job = person.getMind().getJob();
	        if (job != null)
	        	missionProbability *= job.getStartMissionProbabilityModifier(
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

//        if (missionProbability > 0)
//        	logger.info("TravelToSettlementMeta's probability : " +
//				 Math.round(missionProbability*100D)/100D);
		 
        return missionProbability;
    }

    @Override
    public double getProbability(Robot robot) {
        return 0;
    }

    public double getMissionProbability(Settlement settlement, Unit unit) {
    	Person person = null;
    	Robot robot = null;

        double missionProbability = settlement.getMissionBaseProbability(DEFAULT_DESCRIPTION);
		if (missionProbability == 0)
			return 0;
		
		missionProbability = 0;
		
        // Check if there are any desirable settlements within range.
        double topSettlementDesirability = 0D;
        Vehicle vehicle = RoverMission.getVehicleWithGreatestRange(TravelToSettlement.missionType, settlement, false);
        if (vehicle != null) {
        	Map<Settlement, Double> desirableSettlements = null;
			if (unit instanceof Person) {
				person = (Person) unit;
	            desirableSettlements = TravelToSettlement.getDestinationSettlements(
	                    person, settlement, vehicle.getRange(TravelToSettlement.missionType));

			}
			else if (unit instanceof Robot) {
				robot = (Robot) unit;
	            desirableSettlements = TravelToSettlement.getDestinationSettlements(
	                    robot, settlement, vehicle.getRange(TravelToSettlement.missionType));
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
		int numThisMission = Simulation.instance().getMissionManager().numParticularMissions(DEFAULT_DESCRIPTION, settlement);

   		// Check for # of embarking missions.
		if (Math.max(1, settlement.getNumCitizens() / 8.0) < numEmbarked + numThisMission) {
			return 0;
		}			
		
		else if (numThisMission > 1)
			return 0;	
		

		int f1 = 2*numEmbarked + 1;
		int f2 = 2*numThisMission + 1;
		
		missionProbability *= settlement.getNumCitizens() / f1 / f2 / 2D * ( 1 + settlement.getMissionDirectiveModifier(8));
		
        // Crowding modifier.
        int crowding = settlement.getIndoorPeopleCount()
                - settlement.getPopulationCapacity();
        if (crowding > 0) {
            missionProbability *= (crowding + 1);
        }

        return missionProbability;
    }

}