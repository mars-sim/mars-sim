/**
 * Mars Simulation Project
 * CollectIceMeta.java
 * @date 2025-07-06
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission.meta;

import java.util.Set;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.CollectIce;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.structure.Settlement;

/**
 * A meta mission for the CollectIce mission.
 */
public class CollectIceMeta extends AbstractMetaMission {


	private static final int VALUE = 750;

	/** Starting sol for this mission to commence. */
	public static final int MIN_STARTING_SOL = 3;
	
	CollectIceMeta() {
		super(MissionType.COLLECT_ICE, Set.of(JobType.CHEMIST, JobType.BOTANIST, JobType.CHEF));
	}

	@Override
	public Mission constructInstance(Person person, boolean needsReview) {
		return new CollectIce(person, needsReview);
	}

	@Override
	public RatingScore getProbability(Person person) {

		RatingScore missionProbability = RatingScore.ZERO_RATING;
    	if (getMarsTime().getMissionSol() < MIN_STARTING_SOL) {
    		return RatingScore.ZERO_RATING;
    	}
    	
		if (person.isInSettlement()) {

			Settlement settlement = person.getSettlement();

			RoleType roleType = person.getRole().getType();

			if (person.getMind().getJob() == JobType.AREOLOGIST
					|| person.getMind().getJob() == JobType.CHEMIST
					|| person.getMind().getJob() == JobType.BOTANIST
					|| person.getMind().getJob() == JobType.CHEF
					|| RoleType.CHIEF_OF_MISSION_PLANNING == roleType
					|| RoleType.CHIEF_OF_SUPPLY_N_RESOURCES == roleType
					|| RoleType.MISSION_SPECIALIST == roleType
					|| RoleType.RESOURCE_SPECIALIST == roleType
					|| RoleType.COMMANDER == roleType
					|| RoleType.SUB_COMMANDER == roleType
					) {
				missionProbability = new RatingScore(1);
	    		missionProbability.addModifier(MINERALS, settlement.getIceProbabilityValue() / VALUE);

				// Job modifier.
	    		missionProbability.addModifier(LEADER, getLeaderSuitability(person));

				// If this town has a crop farm objective, divided by bonus
				missionProbability.addModifier(GOODS, Math.min(1,
							settlement.getGoodsManager().getCommerceFactor(CommerceType.CROP)/2));

				// if introvert, score  0 to  50 --> -2 to 0
				// if extrovert, score 50 to 100 -->  0 to 2
				// Reduce probability if introvert
				int extrovert = person.getExtrovertmodifier();
				missionProbability.addModifier(PERSON_EXTROVERT, (1 + extrovert/2.0));

				missionProbability.applyRange(0D, LIMIT);
			}
		}

		return missionProbability;
	}
}
