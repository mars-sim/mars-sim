/*
 * Mars Simulation Project
 * CollectRegolithMeta.java
 * @date 2025-07-06
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission.meta;

import java.util.Set;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.CollectRegolith;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.structure.Settlement;

/**
 * A meta mission for the CollectRegolith mission.
 */
public class CollectRegolithMeta extends AbstractMetaMission {

	private static final int VALUE = 1000;

	/** Starting sol for this mission to commence. */
	public static final int MIN_STARTING_SOL = 4;

	CollectRegolithMeta() {
		super(MissionType.COLLECT_REGOLITH, Set.of(JobType.ARCHITECT, JobType.CHEMIST));
	}

	@Override
	public Mission constructInstance(Person person, boolean needsReview) {
		return new CollectRegolith(person, needsReview);
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
					|| RoleType.CHIEF_OF_MISSION_PLANNING == roleType
					|| RoleType.CHIEF_OF_SUPPLY_N_RESOURCES == roleType
					|| RoleType.MISSION_SPECIALIST == roleType
					|| RoleType.RESOURCE_SPECIALIST == roleType
					|| RoleType.COMMANDER == roleType
					|| RoleType.SUB_COMMANDER == roleType
					) {

	    		missionProbability = new RatingScore(1);
	    		missionProbability.addModifier(MINERALS, settlement.getRegolithProbabilityValue() / VALUE);

				// Job modifier.
	    		missionProbability.addModifier(LEADER, getLeaderSuitability(person));

				// If this town has a manufacturing objective, divided by bonus
				missionProbability.addModifier(GOODS, Math.min(1,
								settlement.getGoodsManager().getCommerceFactor(CommerceType.MANUFACTURING)/2));

				// if introvert, score  0 to  50 --> -2 to 0
				// if extrovert, score 50 to 100 -->  0 to 2
				// Increase probability if extrovert
				int extrovert = person.getExtrovertmodifier();
				missionProbability.addModifier(PERSON_EXTROVERT, (1 + extrovert/2.0));
				missionProbability.applyRange(0, LIMIT);
			}
		}

		return missionProbability;
	}
}
