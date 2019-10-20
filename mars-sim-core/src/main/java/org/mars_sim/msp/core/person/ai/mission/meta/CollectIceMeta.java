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
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * A meta mission for the CollectIce mission.
 */
public class CollectIceMeta implements MetaMission {

	private static Logger logger = Logger.getLogger(CollectIceMeta.class.getName());

	/** Mission name */
	private static final String NAME = Msg.getString("Mission.description.collectIce"); //$NON-NLS-1$

	private static final double VALUE = 500D;

    private static final double LIMIT = 10D;
    
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
    		if (missionProbability <= 0)
    			return 0;
    		
//			missionProbability = getSettlementProbability(settlement);
    		int numEmbarked = VehicleMission.numEmbarkingMissions(settlement);
    		int numThisMission = missionManager.numParticularMissions(NAME, settlement);
    	
	   		// Check for # of embarking missions.
    		if (Math.max(1, settlement.getNumCitizens() / 8.0) < numEmbarked + numThisMission) {
    			return 0;
    		}	
    	
    		if (numThisMission > 1)
    			return 0;
    		
    		int f1 = 2*numEmbarked + 1;
    		int f2 = 2*numThisMission + 1;
    		
    		missionProbability *= settlement.getNumCitizens() / f1 / f2 / 2D;
    		
			// Job modifier.
			Job job = person.getMind().getJob();
			if (job != null) {
				missionProbability *= job.getStartMissionProbabilityModifier(CollectIce.class);
				// If this town has a tourist objective, divided by bonus
				missionProbability = missionProbability / settlement.getGoodsManager().getTourismFactor();
			}

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
		
//		if (missionProbability > 0)
//			logger.info("CollectIceMeta's probability : " + Math.round(missionProbability*100D)/100D);

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
	
//	/**
//	 * Reloads instances after loading from a saved sim
//	 * 
//	 * @param {{@link MissionManager}
//	 */
//	public static void setInstances(MissionManager m) {
//		missionManager = m;
//	}
}