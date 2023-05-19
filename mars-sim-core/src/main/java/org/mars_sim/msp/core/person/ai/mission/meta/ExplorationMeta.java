/*
 * Mars Simulation Project
 * ExplorationMeta.java
 * @date 2022-07-14
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.MissionUtil;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A meta mission for the Exploration mission.
 */
public class ExplorationMeta extends AbstractMetaMission {

	/** Mission name */
	private static final double VALUE = 1D;

	private static final int MAX = 200;

	/** default logger. */
	private static final Logger logger = Logger.getLogger(ExplorationMeta.class.getName());

	ExplorationMeta() {
		super(MissionType.EXPLORATION, 
					Set.of(JobType.AREOLOGIST, JobType.ASTRONOMER, JobType.METEOROLOGIST));
	}

	@Override
	public Mission constructInstance(Person person, boolean needsReview) {
		return new Exploration(person, needsReview);
	}

	@Override
	public double getProbability(Person person) {

		double missionProbability = 0D;

		if (person.isInSettlement()) {

			Settlement settlement = person.getSettlement();

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
					return 0;
				}

				int numEmbarked = MissionUtil.numEmbarkingMissions(settlement);
				int numThisMission = missionManager.numParticularMissions(MissionType.EXPLORATION, settlement);
				int pop = settlement.getNumCitizens();
				
		   		// Check for # of embarking missions.
	    		if (Math.max(1, pop / 8.0) < numEmbarked + numThisMission) {
	    			return 0;
	    		}

	    		if (numThisMission > Math.max(1, pop / 8.0)) {
	    			return 0;
	    		}

				try {
					// Get available rover.
					Rover rover = RoverMission.getVehicleWithGreatestRange(settlement, false);
					if (rover != null) {
						// Check if any mineral locations within rover range and obtain their concentration
						missionProbability = Math.min(MAX, settlement.getTotalMineralValue(rover)) / VALUE;
						if (missionProbability < 0) {
							missionProbability = 0;
						}
					}

				} catch (Exception e) {
					logger.log(Level.SEVERE, "Error exploring mineral values.", e);
					return 0;
				}

				int f1 = numEmbarked + 1;
				int f2 = numThisMission + 1;

				missionProbability *= (double)pop / f1 / f2;

				// Job modifier.
				missionProbability *= getLeaderSuitability(person)
						* (settlement.getGoodsManager().getTourismFactor()
	               		 + settlement.getGoodsManager().getResearchFactor())/1.5;

				if (missionProbability > LIMIT)
					missionProbability = LIMIT;

				// if introvert, score  0 to  50 --> -2 to 0
				// if extrovert, score 50 to 100 -->  0 to 2
				// Increase probability if extrovert
				int extrovert = person.getExtrovertmodifier();
				missionProbability = missionProbability * (1 + extrovert/2.0);

				if (missionProbability < 0)
					missionProbability = 0;
 			}
		}

		return missionProbability;
	}
}
