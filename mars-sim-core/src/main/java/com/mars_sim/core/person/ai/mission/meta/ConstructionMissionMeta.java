/*
 * Mars Simulation Project
 * ConstructionMissionMeta.java
 * @date 2023-08-16
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission.meta;


import java.util.Collection;
import java.util.Set;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.ConstructionMission;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.MissionUtil;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.construction.ConstructionValues;


/**
 * A meta mission for Construction Mission.
 */
public class ConstructionMissionMeta extends AbstractMetaMission {
      
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
         
        // No construction until after the x sols of the simulation.
        if ((getMarsTime().getMissionSol() < ConstructionMission.FIRST_AVAILABLE_SOL)
			|| !person.isInSettlement()) {
        	return RatingScore.ZERO_RATING;
        }
	
		Settlement settlement = person.getSettlement();
	
		RoleType roleType = person.getRole().getType();
		
		RatingScore missionProbability = RatingScore.ZERO_RATING;
		if (person.getMind().getJob() == JobType.ARCHITECT
//					|| RoleType.MISSION_SPECIALIST == roleType
//					|| RoleType.CHIEF_OF_MISSION_PLANNING == roleType
				|| RoleType.CHIEF_OF_ENGINEERING == roleType
				|| RoleType.ENGINEERING_SPECIALIST == roleType
				|| RoleType.COMMANDER == roleType
				|| RoleType.SUB_COMMANDER == roleType
				) {							

			// Check if settlement has construction override flag set.
			if (settlement.getProcessOverride(OverrideType.CONSTRUCTION)) {
				return RatingScore.ZERO_RATING;
			}

			// Check if available light utility vehicles.
			if (!ConstructionMission.isLUVAvailable(settlement)) {
				return RatingScore.ZERO_RATING;
			}
			
			int availablePeopleNum = 0;

			Collection<Person> list = settlement.getIndoorPeople();
			for (Person member : list) {
				boolean noMission = !member.getMind().hasActiveMission();
				boolean isFit = !member.getPhysicalCondition().hasSeriousMedicalProblems();
				if (noMission && isFit)
					availablePeopleNum++;
			}

			// Check if enough available people at settlement for mission.
			if (availablePeopleNum < ConstructionMission.MIN_PEOPLE) {
				return RatingScore.ZERO_RATING;
			}
			
			// Check if min number of EVA suits at settlement.
			if (MissionUtil.getNumberAvailableEVASuitsAtSettlement(settlement) <
					ConstructionMission.MIN_PEOPLE) {
				return RatingScore.ZERO_RATING;
			}
			
			int constructionSkill = person.getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION);
			ConstructionValues values =  settlement.getConstructionManager().getConstructionValues();

			// Add construction profit for existing or new construction sites.
			double constructionProfit = values.getSettlementConstructionProfit(constructionSkill);
			if (constructionProfit <= 0D) {
				return RatingScore.ZERO_RATING;
			}
			
			missionProbability = new RatingScore(100D);
			missionProbability.addModifier(SETTLEMENT_POPULATION,
								getSettlementPopModifier(settlement, 8)/2);

			double newSiteProfit = values.getNewConstructionSiteProfit(constructionSkill);
			double existingSiteProfit = values.getAllConstructionSitesProfit(constructionSkill);

			if (newSiteProfit > existingSiteProfit) {
				missionProbability.addModifier("Site", getProbability(settlement));
			}

			// Modify if construction is the person's favorite activity.
			if (person.getFavorite().getFavoriteActivity() == FavoriteType.TINKERING) {
				missionProbability.addModifier("favourite", 1.1D);
			}

			// Job modifier.
			missionProbability.addModifier(LEADER, getLeaderSuitability(person));
					
			// if introvert, score  0 to  50 --> -2 to 0
			// if extrovert, score 50 to 100 -->  0 to 2
			// Reduce probability if introvert
			int extrovert = person.getExtrovertmodifier();
			missionProbability.addModifier(PERSON_EXTROVERT, -2D + (extrovert/25D));
			missionProbability.applyRange(0, LIMIT);
		}
	
        return missionProbability;
    }

    /**
     * Computes probability.
     * 
     * @param settlement
     * @return
     */
    private double getProbability(Settlement settlement) {

        double result = 1D;
        
        // Consider the num of construction sites
        int numSites = settlement.getConstructionManager().getConstructionSites().size();

        // Consider the size of the settlement population
        int numPeople = settlement.getNumCitizens();
        
        double limit = Math.max(-1, 6 * numSites - numPeople);

        result = result/Math.pow(10, 2 + limit);
        
        return result;
    }
}
