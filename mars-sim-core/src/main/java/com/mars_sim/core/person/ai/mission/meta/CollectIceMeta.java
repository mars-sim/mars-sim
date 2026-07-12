/**
 * Mars Simulation Project
 * CollectIceMeta.java
 * @date 2025-07-06
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission.meta;

import java.util.Collections;
import java.util.Set;

import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.mission.AbstractMetaMission;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.CollectIce;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.VehicleType;

/**
 * A meta mission for the CollectIce mission.
 */
public class CollectIceMeta extends AbstractMetaMission {

	private static final Set<JobType> PREFERRED_LEADER_JOBS = Set.of(JobType.CHEMIST, JobType.BOTANIST);
	private static final Set<JobType> PREFERRED_WORKER_JOBS = Collections.emptySet();
	private static final int VALUE = 750;

	/** Starting sol for this mission to commence. */
	private static final int MIN_STARTING_SOL = 3;
	
	public CollectIceMeta() {
		super(MissionType.COLLECT_ICE, 4, PREFERRED_LEADER_JOBS, PREFERRED_WORKER_JOBS);

		setPreferredVehicle(VehicleType.ROVER_TYPES);
		setPopulationRatio(5);
		setSolThreshold(MIN_STARTING_SOL);
	}

	/**
	 * Constructs a new instance of the CollectIce mission with the given crew and review status.
	 * @param crew the roster of crew members for the mission.
	 * @param needsReview whether the mission requires review before execution.
	 * @return a new instance of the CollectIce mission.
	 */
	@Override
	public Mission constructInstance(Roster crew, boolean needsReview) {
		return new CollectIce(crew, needsReview);
	}

	@Override
	public RatingScore getProbability(Person person) {

    	if (!person.isInSettlement()) {
    		return RatingScore.ZERO_RATING;
    	}
    	
		Settlement settlement = person.getSettlement();

		RoleType roleType = person.getRole().getType();
		if (roleType.isCouncil()
				|| person.getMind().getJobType() == JobType.AREOLOGIST
				|| person.getMind().getJobType() == JobType.CHEMIST
				|| person.getMind().getJobType() == JobType.BOTANIST
				|| person.getMind().getJobType() == JobType.CHEF
				|| RoleType.CHIEF_OF_MISSION_PLANNING == roleType
				|| RoleType.CHIEF_OF_SUPPLY_RESOURCE == roleType
				|| RoleType.MISSION_SPECIALIST == roleType
				|| RoleType.RESOURCE_SPECIALIST == roleType
				) {

			// Check if there are enough barrel at the settlement for collecting ice.
			int stored = settlement.findNumContainersOfType(EquipmentType.BARREL);
			int needed = CollectIce.REQUIRED_BARRELS;
			if (stored < needed) {
				BuildingManager.injectEquipmentDemand(EquipmentType.BARREL, settlement, stored, needed);
				return RatingScore.ZERO_RATING;
			}
								
			// Get available rover.
			var rover = selectVehicle(settlement);
			if (rover == null) {
				return RatingScore.ZERO_RATING;
			}

			var missionProbability = new RatingScore(1);
			missionProbability.addModifier(DEMAND_PROBABILITY, settlement.getIceDemandCache() / VALUE);

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

			return missionProbability;
		}

		return RatingScore.ZERO_RATING;
	}
}
