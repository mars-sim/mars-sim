/**
 * Mars Simulation Project
 * CollectIceMeta.java
 * @version 3.1.0 2017-05-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.CollectIce;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * A meta mission for the CollectIce mission.
 */
public class CollectIceMeta implements MetaMission {

	private static Logger logger = Logger.getLogger(CollectIceMeta.class.getName());

	/** Mission name */
	private static final String NAME = Msg.getString("Mission.description.collectIce"); //$NON-NLS-1$

	private static final double VALUE = 20D;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Mission constructInstance(Person person) {
		return new CollectIce(person);
	}

	@Override
	public double getProbability(Person person) {

		double missionProbability = 0D;

		if (person.isInSettlement()) {

			Settlement settlement = person.getSettlement();

			missionProbability = settlement.getMissionBaseProbability() / VALUE;
    		if (missionProbability == 0)
    			return 0;
    		
//			missionProbability = getSettlementProbability(settlement);
			
			// Job modifier.
			Job job = person.getMind().getJob();
			if (job != null) {
				missionProbability *= job.getStartMissionProbabilityModifier(CollectIce.class);
				// If this town has a tourist objective, divided by bonus
				missionProbability = missionProbability / settlement.getGoodsManager().getTourismFactor();
			}


			if (missionProbability > 10D)
				missionProbability = 10D;
			else if (missionProbability < 0)
				missionProbability = 0;
		}
		
//		if (missionProbability > 0)
//			logger.info("CollectIceMeta's probability : " + Math.round(missionProbability*100D)/100D);

		return missionProbability;
	}

//	public double getSettlementProbability(Settlement settlement) {
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
//			missionProbability = settlement.getIceProbabilityValue() / VALUE;
//		}
//
//		if (missionProbability <= 0)
//			return 0;
//		
//		int f1 = numEmbarked + 1;
//		int f2 = numThisMission + 1;
//		
//		missionProbability *= settlement.getNumCitizens() / f1 / f2 / 2D;
//		
//		// Crowding modifier.
//		int crowding = settlement.getIndoorPeopleCount() - settlement.getPopulationCapacity();
//		if (crowding > 0) {
//			missionProbability *= (crowding + 1);
//		}
//
////		 logger.info("CollectIceMeta's probability : " +
////				 Math.round(result*100D)/100D);
//		 
//		return missionProbability;
//	}
	
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