/**
 * Mars Simulation Project
 * CollectRegolithMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.person.ai.mission.CollectRegolith;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * A meta mission for the CollectRegolith mission.
 */
public class CollectRegolithMeta extends AbstractMetaMission {

	private static final double VALUE = 10D;
   
	/** starting sol for this mission to commence. */
	public final static int MIN_STARTING_SOL = 1;

	CollectRegolithMeta() {
		super(MissionType.COLLECT_REGOLITH, "collectRegolith");
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
				
				if (settlement.getMissionBaseProbability(MissionType.COLLECT_REGOLITH))
	            	missionProbability = 1;
	            else
	    			return 0;
	    	   		
	    		int numEmbarked = VehicleMission.numEmbarkingMissions(settlement);
	    		int numThisMission = missionManager.numParticularMissions(MissionType.COLLECT_REGOLITH, settlement);
	    	
		   		// Check for # of embarking missions.
	    		if (Math.max(1, settlement.getNumCitizens() / 8.0) < numEmbarked + numThisMission) {
	    			return 0;
	    		}	
	    	
	    		if (numThisMission > 1)
	    			return 0;
	    		
	    		int f1 = 2*numEmbarked + 1;
	    		int f2 = 2*numThisMission + 1;
	    		
	    		missionProbability *= settlement.getNumCitizens() / VALUE / f1 / f2 / 2D * ( 1 + settlement.getMissionDirectiveModifier(MissionType.COLLECT_REGOLITH));
	    		
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

		return missionProbability;
	}
}
