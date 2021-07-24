/**
 * Mars Simulation Project
 * CollectIceMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.person.ai.mission.CollectIce;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * A meta mission for the CollectIce mission.
 */
public class CollectIceMeta implements MetaMission {

//	private static final Logger logger = Logger.getLogger(CollectIceMeta.class.getName());
//	private static final String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
//			logger.getName().length());
	
	/** Mission name */
	private static final String DEFAULT_DESCRIPTION = Msg.getString("Mission.description.collectIce"); //$NON-NLS-1$

	private static final double VALUE = 50D;
   
	@Override
	public String getName() {
		return DEFAULT_DESCRIPTION;
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

			RoleType roleType = person.getRole().getType();
			
			if (person.getMind().getJob() == JobType.CHEMIST
					|| RoleType.MISSION_SPECIALIST == roleType
					|| RoleType.CHIEF_OF_MISSION_PLANNING == roleType
					|| RoleType.CHIEF_OF_AGRICULTURE == roleType
					|| RoleType.RESOURCE_SPECIALIST == roleType
					|| RoleType.COMMANDER == roleType
					|| RoleType.SUB_COMMANDER == roleType
					) {			
			
				if (settlement.getMissionBaseProbability(DEFAULT_DESCRIPTION))
	            	missionProbability = 1;
	            else
	    			return 0;
	    		
	//			missionProbability = getSettlementProbability(settlement);
	    		int numEmbarked = VehicleMission.numEmbarkingMissions(settlement);
	    		int numThisMission = missionManager.numParticularMissions(DEFAULT_DESCRIPTION, settlement);
	    	
		   		// Check for # of embarking missions.
	    		if (Math.max(1, settlement.getNumCitizens() / 8.0) < numEmbarked + numThisMission) {
	    			return 0;
	    		}	
	    	
	    		if (numThisMission > 1)
	    			return 0;
	    		
	    		int f1 = 2*numEmbarked + 1;
	    		int f2 = 2*numThisMission + 1;
	    		
	    		missionProbability *= settlement.getNumCitizens() / VALUE / f1 / f2 * ( 1 + settlement.getMissionDirectiveModifier(MissionType.COLLECT_ICE));
	    		
				// Job modifier.
				JobType job = person.getMind().getJob();
				if (job != null) {
					missionProbability *= JobUtil.getStartMissionProbabilityModifier(job, CollectIce.class);
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
