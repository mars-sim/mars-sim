/*
 * Mars Simulation Project
 * ConstructionMissionMeta.java
 * @date 2025-09-06
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission.meta;


import java.util.Set;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.mission.AbstractMetaMission;
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
    
	private static final Set<JobType> LEADER_JOBS = Set.of(JobType.ARCHITECT, JobType.ENGINEER);
	private static final Set<JobType> WORKER_JOBS = Set.of(JobType.ARCHITECT, JobType.ENGINEER, JobType.TECHNICIAN);

	// Sites have a higher score than queued buildings
	private static final int SITE_BASE = 200;
	private static final int QUEUE_BASE = 50;
		
    public ConstructionMissionMeta() {
    	super(MissionType.CONSTRUCTION, 10, LEADER_JOBS, WORKER_JOBS);
		setMinimumMembers(3);
		setPopulationRatio(30);
		setPopulationThreshold(20);
    }
    
	/**
	 * Constructs an instance of the associated mission.
	 * 
	 * @param crew The crew for the mission, including leader and members
	 * @param needsReview Mission must be reviewed. Construction mission do not need reviewing.
	 * @return mission instance.
	 */
	@Override
	public Mission constructInstance(Roster crew, boolean needsReview) {
        return new ConstructionMission(crew);
    }

    @Override
    public RatingScore getProbability(Person person) {
         
        if (!person.isInSettlement()) {
        	return RatingScore.ZERO_RATING;
        }
	
		Settlement settlement = person.getSettlement();
		
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
		var missionProbability = new RatingScore(need);

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
