/**
 * Mars Simulation Project
 * CollectRegolithMeta.java
 * @version 3.1.0 2017-05-04
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.CollectRegolith;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * A meta mission for the CollectRegolith mission.
 */
public class CollectRegolithMeta implements MetaMission {

	// private static Logger logger =
	private static Logger logger = Logger.getLogger(CollectRegolithMeta.class.getName());

	/** Mission name */
	private static final String NAME = Msg.getString("Mission.description.collectRegolith"); //$NON-NLS-1$

	private static final double VALUE = 200D;

    private static final double LIMIT = 10D;
    
	/** starting sol for this mission to commence. */
	public final static int MIN_STARTING_SOL = 1;

//    private static MissionManager missionManager = Simulation.instance().getMissionManager();
    
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Mission constructInstance(Person person) {
		return new CollectRegolith(person);
	}

	@Override
	public double getProbability(Person person) {

		double missionProbability = 0;

		if (person.isInSettlement()) {

			Settlement settlement = person.getSettlement();

//			result = getSettlementProbability(settlement);

			missionProbability = settlement.getMissionBaseProbability() / VALUE;
    		if (missionProbability == 0)
    			return 0;
    	   		
    		int numEmbarked = VehicleMission.numEmbarkingMissions(settlement);
    		int numThisMission = missionManager.numParticularMissions(NAME, settlement);
    	
	   		// Check for # of embarking missions.
    		if (Math.max(1, settlement.getNumCitizens() / 8.0) < numEmbarked + numThisMission) {
    			return 0;
    		}	
    	
    		int f1 = 2*numEmbarked + 1;
    		int f2 = 2*numThisMission + 1;
    		
    		missionProbability *= settlement.getNumCitizens() / f1 / f2 / 2D;
    		
			// Job modifier.
			Job job = person.getMind().getJob();
			if (job != null) {
				missionProbability *= job.getStartMissionProbabilityModifier(CollectRegolith.class);
				// If this town has a tourist objective, divided by bonus
				missionProbability = missionProbability / settlement.getGoodsManager().getTourismFactor();
			}

			if (missionProbability > LIMIT)
				missionProbability = LIMIT;
			else if (missionProbability < 0)
				missionProbability = 0;
		}
		
//        if (missionProbability > 0)
//        	logger.info("CollectRegolithMeta's probability : " +
//				 Math.round(missionProbability*100D)/100D);

		return missionProbability;
	}

//  public double getSettlementProbability(Settlement settlement) {
//
//        double missionProbability = CollectResourcesMission.getNewMissionProbability(settlement, Bag.class, 
//                CollectRegolith.REQUIRED_BAGS, CollectRegolith.MIN_PEOPLE);
//   		if (missionProbability == 0)
//			return 0;
//   		
//		int numEmbarked = VehicleMission.numEmbarkingMissions(settlement);
//		int numThisMission = missionManager.numParticularMissions(NAME, settlement);
//
//		// Check for embarking missions.
//		if (settlement.getNumCitizens() / 4.0 < numEmbarked + numThisMission) {
//			return 0;
//		}
//		
//		else if (numThisMission > 1)
//			return 0;
//		
//		else {
//			missionProbability = settlement.getRegolithProbabilityValue() / VALUE;
//		}
//		
//		if (missionProbability <= 0)
//			return 0;
//
//		// Check for embarking missions.
//		if (settlement.getNumCitizens() / 4.0 < numEmbarked + numThisMission) {
//			return 0;
//		}	
//	
//		int f1 = numEmbarked + 1;
//		int f2 = numThisMission + 1;
//		
//		missionProbability *= settlement.getNumCitizens() / f1 / f2 / 2D;
//        
//        return missionProbability;
//    }
	  
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
	
//	/**
//	 * Reloads instances after loading from a saved sim
//	 * 
//	 * @param {{@link MissionManager}
//	 */
//	public static void setInstances(MissionManager m) {
//		missionManager = m;
//	}
}