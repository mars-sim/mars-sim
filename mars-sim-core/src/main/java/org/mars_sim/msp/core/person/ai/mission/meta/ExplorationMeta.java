/*
 * Mars Simulation Project
 * ExplorationMeta.java
 * @date 2022-07-14
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.Set;

import org.mars_sim.msp.core.data.RatingScore;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A meta mission for the Exploration mission.
 */
public class ExplorationMeta extends AbstractMetaMission {

	/** Mission name */
	private static final double VALUE = 20D;

	private static final int MAX = 200;

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

		Settlement settlement = person.getSettlement();
		
		if (settlement != null) {

            RoleType roleType = person.getRole().getType();

 			if (RoleType.CHIEF_OF_SCIENCE == roleType
 					|| RoleType.SCIENCE_SPECIALIST == roleType
 					|| RoleType.MISSION_SPECIALIST == roleType
 					|| RoleType.CHIEF_OF_MISSION_PLANNING == roleType
 					|| RoleType.CHIEF_OF_SUPPLY_N_RESOURCES == roleType
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
				missionProbability.addModifier(SETTLEMENT_POPULATION,
									getSettlementPopModifier(settlement, 8));
				if (missionProbability.getScore() == 0) {
	    			return RatingScore.ZERO_RATING;
	    		}

				// Get available rover.
				Rover rover = RoverMission.getVehicleWithGreatestRange(settlement, false);
				if (rover != null) {
					// Check if any mineral locations within rover range and obtain their concentration
					missionProbability.addModifier(MINERALS, Math.min(MAX, settlement.getTotalMineralValue(rover)) / VALUE);
				}

				// Job modifier.
				missionProbability.addModifier(LEADER, getLeaderSuitability(person));
				missionProbability.addModifier(GOODS, (settlement.getGoodsManager().getTourismFactor()
	               		 + settlement.getGoodsManager().getResearchFactor())/2);


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
