/**
 * Mars Simulation Project
 * CollectRegolithMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.Set;

import org.mars_sim.msp.core.data.Rating;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.mission.CollectRegolith;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * A meta mission for the CollectRegolith mission.
 */
public class CollectRegolithMeta extends AbstractMetaMission {

	private static final double VALUE = 200D;

	/** starting sol for this mission to commence. */
	public final static int MIN_STARTING_SOL = 2;

	CollectRegolithMeta() {
		super(MissionType.COLLECT_REGOLITH, Set.of(JobType.ARCHITECT, JobType.CHEMIST));
	}

	@Override
	public Mission constructInstance(Person person, boolean needsReview) {
		return new CollectRegolith(person, needsReview);
	}

	@Override
	public Rating getProbability(Person person) {

		Rating missionProbability = Rating.ZERO_RATING;

		if (person.isInSettlement()) {

			Settlement settlement = person.getSettlement();

			RoleType roleType = person.getRole().getType();

			if (person.getMind().getJob() == JobType.AREOLOGIST
					|| person.getMind().getJob() == JobType.CHEMIST
					|| person.getMind().getJob() == JobType.ENGINEER
					|| RoleType.MISSION_SPECIALIST == roleType
					|| RoleType.CHIEF_OF_MISSION_PLANNING == roleType
					|| RoleType.CHIEF_OF_AGRICULTURE == roleType
					|| RoleType.RESOURCE_SPECIALIST == roleType
					|| RoleType.COMMANDER == roleType
					|| RoleType.SUB_COMMANDER == roleType
					) {

	    		missionProbability = new Rating(1);
				missionProbability.addModifier(SETTLEMENT_POPULATION,
							getSettlementPopModifier(settlement, 8));
				if (missionProbability.getScore() == 0) {
	    			return missionProbability;
	    		}
	    		missionProbability.addModifier(MINERALS, (settlement.getRegolithProbabilityValue() / VALUE));

				// Job modifier.
	    		missionProbability.addModifier(LEADER, getLeaderSuitability(person));

				// If this town has a tourist objective, divided by bonus
				missionProbability.addModifier(GOODS, 1D / settlement.getGoodsManager().getTourismFactor());

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
