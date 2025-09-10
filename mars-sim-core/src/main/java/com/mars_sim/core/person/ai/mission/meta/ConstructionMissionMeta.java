/*
 * Mars Simulation Project
 * ConstructionMissionMeta.java
 * @date 2025-09-06
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
    
	/** default logger. */
	// May add back private static final SimLogger logger = SimLogger.getLogger(ConstructionMissionMeta.class.getName());

	// Sites have a higher score than queued buildings
	private static final int SITE_BASE = 200;
	private static final int QUEUE_BASE = 50;
		
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

		RatingScore missionProbability = RatingScore.ZERO_RATING;
		
		// Find people not on a mission and healthy		
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
		
		var cm = settlement.getConstructionManager();
		int need = cm.getConstructionSites() 
				// Note: using .getConstructionSitesNeedingMission() returns zero sites
				.size() * SITE_BASE;

		if (need == 0) {
			need = (int) cm.getBuildingSchedule().stream()
					.filter(s -> s.isReady())
					.count() * QUEUE_BASE;

			if (need == 0) {
				return RatingScore.ZERO_RATING;
			}
		}
		missionProbability = new RatingScore(need);

       	RoleType roleType = person.getRole().getType();
        double roleModifier = switch(roleType) {
            case ENGINEERING_SPECIALIST -> 1.5;
            case CHIEF_OF_ENGINEERING -> 3;
            case MISSION_SPECIALIST -> 1.5;
            case CHIEF_OF_MISSION_PLANNING -> 3;
            case SUB_COMMANDER -> 4.5;
            case COMMANDER -> 5;
            case MAYOR -> 5;
            case ADMINISTRATOR -> 5;
            case DEPUTY_ADMINISTRATOR -> 5;
            default -> .2;
        };
        
        missionProbability.addModifier("role", roleModifier);		
   
        JobType jobType = person.getMind().getJobType();
        double jobModifier = switch(jobType) {
        	case ENGINEER -> 1.5;
        	case PHYSICIST -> 1.3;
        	case MATHEMATICIAN -> 1.1;
        	case TECHNICIAN -> 1.2;
        	default -> .2;
        };		
            
        missionProbability.addModifier("job", jobModifier);
		
		// Modify if construction is the person's favorite activity.
		if (person.getFavorite().getFavoriteActivity() == FavoriteType.TINKERING) {
			missionProbability.addModifier("favourite", 1.1D);
		}

		// Job modifier.
		missionProbability.addModifier(LEADER, getLeaderSuitability(person));
				
		missionProbability.applyRange(0, LIMIT);
	
        return missionProbability;
    }
}
