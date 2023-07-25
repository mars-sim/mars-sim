/*
 * Mars Simulation Project
 * BuildingSalvageMissionMeta.java
 * @date 2021-08-28
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.mission.SalvageMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.MissionUtil;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.structure.OverrideType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.construction.SalvageValues;

/**
 * A meta mission for the BuildingSalvageMission mission.
 */
public class BuildingSalvageMissionMeta extends AbstractMetaMission {

    /** default logger. */
    private static final Logger logger = Logger.getLogger(BuildingSalvageMissionMeta.class.getName());

    BuildingSalvageMissionMeta() {
    	super(MissionType.SALVAGE, Set.of(JobType.ARCHITECT));
    }
    
    @Override
    public Mission constructInstance(Person person, boolean needsReview) {
        return new SalvageMission(person);
    }

    @Override
    public double getProbability(Person person) {

        double missionProbability = 0D;
  
      
        if (getMarsTime().getMissionSol() < SalvageMission.FIRST_AVAILABLE_SOL)
        	return 0;

        // Check if person is in a settlement.
        if (person.isInSettlement()) {
            Settlement settlement = person.getSettlement();

            // Check if settlement has construction override flag set.
            if (settlement.getProcessOverride(OverrideType.CONSTRUCTION))
            	return 0;
            
            // Check if available light utility vehicles.
            if (!SalvageMission.isLUVAvailable(settlement))
                return 0;
            
	        RoleType roleType = person.getRole().getType();
			
			if (person.getMind().getJob() == JobType.ARCHITECT
					|| RoleType.MISSION_SPECIALIST == roleType
					|| RoleType.CHIEF_OF_MISSION_PLANNING == roleType
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
	                return 0;
	
	            // Check if min number of EVA suits at settlement.
	            if (MissionUtil.getNumberAvailableEVASuitsAtSettlement(settlement) < SalvageMission.MIN_PEOPLE) {
	            	return 0;
	            }
	
	            try {
	                int constructionSkill = person.getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION);
	                SalvageValues values = settlement.getConstructionManager()
	                        .getSalvageValues();
	                double salvageProfit = values
	                        .getSettlementSalvageProfit(constructionSkill);
	                missionProbability = salvageProfit;
	                if (missionProbability > 10D) {
	                    missionProbability = 10D;
	                }
	            } catch (Exception e) {
	                logger.log(Level.SEVERE,
	                        "Error getting salvage construction site by a person.", e);
	            	return 0;
	            }
	
	            // Job modifier.
	            missionProbability *= getLeaderSuitability(person);
	            
				if (missionProbability > LIMIT)
					missionProbability = LIMIT;
				
				// if introvert, score  0 to  50 --> -2 to 0
				// if extrovert, score 50 to 100 -->  0 to 2
				// Reduce probability if introvert
				int extrovert = person.getExtrovertmodifier();
				missionProbability += extrovert;
				
				if (missionProbability < 0)
					missionProbability = 0;
	        }
        }
        
        return missionProbability;
    }
}
