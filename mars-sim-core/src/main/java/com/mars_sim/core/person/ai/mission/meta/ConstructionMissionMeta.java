/*
 * Mars Simulation Project
 * ConstructionMissionMeta.java
 * @date 2023-08-16
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission.meta;


import java.util.Set;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.ConstructionMission;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.MissionUtil;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.structure.Settlement;


/**
 * A meta mission for Construction Mission.
 */
public class ConstructionMissionMeta extends AbstractMetaMission {
      
	private static final double BASE_SCORE = 40D;
		
    ConstructionMissionMeta() {
    	super(MissionType.CONSTRUCTION, 
				Set.of(JobType.ARCHITECT, JobType.ENGINEER));
    }
    
    @Override
    public Mission constructInstance(Person person, boolean needsReview) {
        return new ConstructionMission(person);
    }

    @Override
    public RatingScore getProbability(Person person) {
         
        if (!person.isInSettlement()) {
        	return RatingScore.ZERO_RATING;
        }
	
		Settlement settlement = person.getSettlement();
	
		RoleType roleType = person.getRole().getType();
		var jobType = person.getMind().getJob();
		
		RatingScore missionProbability = RatingScore.ZERO_RATING;
		if (jobType == JobType.ARCHITECT
				|| jobType == JobType.ENGINEER
				|| RoleType.CHIEF_OF_ENGINEERING == roleType
				|| RoleType.ENGINEERING_SPECIALIST == roleType
				|| RoleType.COMMANDER == roleType
				|| RoleType.SUB_COMMANDER == roleType
				) {							

			// Fint people not on a misison and healthy		
			long availablePeopleNum = settlement.getIndoorPeople().stream()
						.filter(p -> !p.getMind().hasActiveMission()
									&& !p.getPhysicalCondition().hasSeriousMedicalProblems())
						.count();

			// Check if enough available people at settlement for mission.
			if ((availablePeopleNum < ConstructionMission.MIN_PEOPLE) 
				|| !ConstructionMission.isLUVAvailable(settlement)
				|| (MissionUtil.getNumberAvailableEVASuitsAtSettlement(settlement) <
					ConstructionMission.MIN_PEOPLE)) {
				return RatingScore.ZERO_RATING;
			}
			
			int need = settlement.getConstructionManager().getConstructionSitesNeedingMission(true).size();
			if (need == 0) {
				return RatingScore.ZERO_RATING;
			}
			missionProbability = new RatingScore(need * BASE_SCORE);
	
			// Modify if construction is the person's favorite activity.
			if (person.getFavorite().getFavoriteActivity() == FavoriteType.TINKERING) {
				missionProbability.addModifier("favourite", 1.1D);
			}

			// Job modifier.
			missionProbability.addModifier(LEADER, getLeaderSuitability(person));
					
			missionProbability.applyRange(0, LIMIT);
		}
	
        return missionProbability;
    }
}
