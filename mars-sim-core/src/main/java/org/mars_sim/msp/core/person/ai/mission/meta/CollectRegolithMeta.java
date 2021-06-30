/**
 * Mars Simulation Project
 * CollectRegolithMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.person.ai.mission.CollectRegolith;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * A meta mission for the CollectRegolith mission.
 */
public class CollectRegolithMeta implements MetaMission {

	// private static Logger logger =
//	private static Logger logger = Logger.getLogger(CollectRegolithMeta.class.getName());
//	private static final String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
//			logger.getName().length());
	
	/** Mission name */
	private static final String DEFAULT_DESCRIPTION = Msg.getString("Mission.description.collectRegolith"); //$NON-NLS-1$

	private static final double VALUE = 10D;

    private static final double LIMIT = 10D;
    
	/** starting sol for this mission to commence. */
	public final static int MIN_STARTING_SOL = 1;

//    private static MissionManager missionManager = Simulation.instance().getMissionManager();
    
	@Override
	public String getName() {
		return DEFAULT_DESCRIPTION;
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

			RoleType roleType = person.getRole().getType();
			
			if (person.getMind().getJob() == JobType.CHEMIST
					|| person.getMind().getJob() == JobType.ENGINEER
					|| RoleType.MISSION_SPECIALIST == roleType
					|| RoleType.CHIEF_OF_MISSION_PLANNING == roleType
					|| RoleType.CHIEF_OF_AGRICULTURE == roleType
					|| RoleType.RESOURCE_SPECIALIST == roleType
					|| RoleType.COMMANDER == roleType
					|| RoleType.SUB_COMMANDER == roleType
					) {			
				
				missionProbability = settlement.getMissionBaseProbability(DEFAULT_DESCRIPTION) / VALUE;
	    		if (missionProbability <= 0)
	    			return 0;
	    	   		
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
	    		
	    		missionProbability *= settlement.getNumCitizens() / f1 / f2 / 2D * ( 1 + settlement.getMissionDirectiveModifier(3));
	    		
				// Job modifier.
				JobType job = person.getMind().getJob();
				if (job != null) {
					missionProbability *= JobUtil.getJobSpec(job).getStartMissionProbabilityModifier(CollectRegolith.class);
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
		
//        if (missionProbability > 0)
//        	logger.info("CollectRegolithMeta's probability : " +
//				 Math.round(missionProbability*100D)/100D);

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
