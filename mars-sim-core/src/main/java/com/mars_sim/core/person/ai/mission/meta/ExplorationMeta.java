/*
 * Mars Simulation Project
 * ExplorationMeta.java
 * @date 2025-07-06
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission.meta;

import java.util.Set;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.Exploration;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.RoverMission;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Rover;

/**
 * A meta mission for the Exploration mission.
 */
public class ExplorationMeta extends AbstractMetaMission {

	private static final int MAX = 200;
	
	/** Starting sol for this mission to commence. */
	public static final int MIN_STARTING_SOL = 2;

	private static final double VALUE = 20D;

	ExplorationMeta() {
		super(MissionType.EXPLORATION, 
					Set.of(JobType.AREOLOGIST, JobType.ASTRONOMER, JobType.METEOROLOGIST));
	}

	@Override
	public Mission constructInstance(Person person, boolean needsReview) {
		return new Exploration(person, needsReview);
	}

	@Override
	public RatingScore getProbability(Person person) {

		RatingScore missionProbability = RatingScore.ZERO_RATING;
    	if (getMarsTime().getMissionSol() < MIN_STARTING_SOL) {
    		return RatingScore.ZERO_RATING;
    	}
    	
		Settlement settlement = person.getSettlement();
		
		if (settlement != null) {

            RoleType roleType = person.getRole().getType();

 			if (RoleType.CHIEF_OF_SCIENCE == roleType
 					|| RoleType.SCIENCE_SPECIALIST == roleType
 					|| RoleType.MISSION_SPECIALIST == roleType
 					|| RoleType.CHIEF_OF_MISSION_PLANNING == roleType
 					|| RoleType.CHIEF_OF_SUPPLY_RESOURCE == roleType
 					|| RoleType.RESOURCE_SPECIALIST == roleType
 					|| RoleType.COMMANDER == roleType
 					|| RoleType.SUB_COMMANDER == roleType
 					) {

				// 1. Check if there are enough specimen containers at the settlement for
				// collecting rock samples.
				if (settlement.findNumContainersOfType(EquipmentType.SPECIMEN_BOX) < Exploration.REQUIRED_SPECIMEN_CONTAINERS) {
					return RatingScore.ZERO_RATING;
				}

				missionProbability = new RatingScore(1);
								
				// Get available rover.
				Rover rover = RoverMission.getVehicleWithGreatestRange(settlement, false);
				if (rover != null) {
					// Check if any mineral locations within rover range and obtain their concentration
					missionProbability.addModifier(MINERALS, Math.min(MAX,
									settlement.getExplorations().getTotalMineralValue(rover)) / VALUE);
				}

				// Job modifier.
				missionProbability.addModifier(LEADER, getLeaderSuitability(person));
				missionProbability = applyCommerceAverage(missionProbability, settlement, CommerceType.TOURISM,
													CommerceType.RESEARCH);

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
