/**
 * Mars Simulation Project
 * CollectIceMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.Set;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.mission.CollectIce;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * A meta mission for the CollectIce mission.
 */
public class CollectIceMeta extends AbstractMetaMission {


	private static final double VALUE = 50D;
   
	CollectIceMeta() {
		super(MissionType.COLLECT_ICE, "collectIce", 
				Set.of(JobType.AREOLOGIST, JobType.CHEMIST));
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
			
				if (settlement.getMissionBaseProbability(MissionType.COLLECT_ICE))
	            	missionProbability = 1;
	            else
	    			return 0;
	    		
	//			missionProbability = getSettlementProbability(settlement);
	    		int numEmbarked = VehicleMission.numEmbarkingMissions(settlement);
	    		int numThisMission = missionManager.numParticularMissions(MissionType.COLLECT_ICE, settlement);
	    	
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
	    		missionProbability *= getLeaderSuitability(person);
				// If this town has a tourist objective, divided by bonus
				missionProbability = missionProbability / settlement.getGoodsManager().getTourismFactor();
	
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
