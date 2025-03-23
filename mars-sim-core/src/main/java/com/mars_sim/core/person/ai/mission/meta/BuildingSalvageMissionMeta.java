/*
 * Mars Simulation Project
 * BuildingSalvageMissionMeta.java
 * @date 2021-08-28
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission.meta;

import java.util.Iterator;
import java.util.Set;

import com.mars_sim.core.building.construction.SalvageValues;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.SalvageMission;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.MissionUtil;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.Settlement;

/**
 * A meta mission for the BuildingSalvageMission mission.
 */
public class BuildingSalvageMissionMeta extends AbstractMetaMission {

    BuildingSalvageMissionMeta() {
    	super(MissionType.SALVAGE, 
    			Set.of(JobType.ARCHITECT, JobType.ENGINEER)); // ScienceType.ENGINEERING
    }
    
    @Override
    public Mission constructInstance(Person person, boolean needsReview) {
        return new SalvageMission(person);
    }

    @Override
    public RatingScore getProbability(Person person) {

        RatingScore missionProbability = RatingScore.ZERO_RATING;
  
        // Check if person is in a settlement.
        if (person.isInSettlement()) {
            Settlement settlement = person.getSettlement();

            // Check if settlement has construction override flag set.
            if (settlement.getProcessOverride(OverrideType.CONSTRUCTION))
            	return RatingScore.ZERO_RATING;
            
            // Check if available light utility vehicles.
            if (!SalvageMission.isLUVAvailable(settlement))
                return RatingScore.ZERO_RATING;
            
	        RoleType roleType = person.getRole().getType();
			
			if (person.getMind().getJob() == JobType.ARCHITECT
//					|| RoleType.MISSION_SPECIALIST == roleType
//					|| RoleType.CHIEF_OF_MISSION_PLANNING == roleType
					|| RoleType.CHIEF_OF_ENGINEERING == roleType
					|| RoleType.ENGINEERING_SPECIALIST == roleType
					|| RoleType.COMMANDER == roleType
					|| RoleType.SUB_COMMANDER == roleType
					) {
	
	            // Check if enough available people at settlement for mission.
	            int availablePeopleNum = 0;
	            Iterator<Person> i = settlement.getIndoorPeople().iterator();
	            while (i.hasNext()) {
	                Person member = i.next();
	                boolean noMission = !member.getMind().hasActiveMission();
	                boolean isFit = !member.getPhysicalCondition()
	                        .hasSeriousMedicalProblems();
	                if (noMission && isFit) {
	                    availablePeopleNum++;
	                }
	            }
	
	            if (availablePeopleNum < SalvageMission.MIN_PEOPLE)
	                return RatingScore.ZERO_RATING;
	
	            // Check if min number of EVA suits at settlement.
	            if (MissionUtil.getNumberAvailableEVASuitsAtSettlement(settlement) < SalvageMission.MIN_PEOPLE) {
	            	return RatingScore.ZERO_RATING;
	            }
	
				int constructionSkill = person.getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION);
				SalvageValues values = settlement.getConstructionManager()
						.getSalvageValues();
				double salvageProfit = values
						.getSettlementSalvageProfit(constructionSkill);
				if (salvageProfit > 10D) {
					salvageProfit = 10D;
				}
				missionProbability = new RatingScore(salvageProfit);
	
	            // Job modifier.
	            missionProbability.addModifier(LEADER, getLeaderSuitability(person));  
				
				// if introvert, score  0 to  50 --> -2 to 0
				// if extrovert, score 50 to 100 -->  0 to 2
				// Reduce probability if introvert
				int extrovert = person.getExtrovertmodifier();
				missionProbability.addModifier(PERSON_EXTROVERT, (-2 + extrovert/25.0));
				missionProbability.applyRange(0, LIMIT);
	        }
        }
        
        return missionProbability;
    }
}
