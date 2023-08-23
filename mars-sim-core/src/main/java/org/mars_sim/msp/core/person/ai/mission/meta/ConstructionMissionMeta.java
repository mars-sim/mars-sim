/*
 * Mars Simulation Project
 * ConstructionMissionMeta.java
 * @date 2023-08-16
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;


import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.mission.ConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.MissionUtil;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.structure.OverrideType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.construction.ConstructionValues;


/**
 * A meta mission for Construction Mission.
 */
public class ConstructionMissionMeta extends AbstractMetaMission {

    /** default logger. */
    private static final Logger logger = Logger.getLogger(MiningMeta.class.getName());
      
    ConstructionMissionMeta() {
    	super(MissionType.CONSTRUCTION, 
				Set.of(JobType.ARCHITECT, JobType.ENGINEER)); // ScienceType.ENGINEERING
    }
    
    @Override
    public Mission constructInstance(Person person, boolean needsReview) {
        return new ConstructionMission(person);
    }

    @Override
    public double getProbability(Person person) {

        double missionProbability = 0D;
         
        // No construction until after the x sols of the simulation.
        if (getMarsTime().getMissionSol() < ConstructionMission.FIRST_AVAILABLE_SOL) {
        	return 0;
        }
        
        // Check if person is in a settlement.
        if (person.isInSettlement()) {
            Settlement settlement = person.getSettlement();
       
            RoleType roleType = person.getRole().getType();
			
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
	            	return 0;
	            }

	            // Check if available light utility vehicles.
	            if (!ConstructionMission.isLUVAvailable(settlement)) {
	            	return 0;
	            }

	            missionProbability = getSettlementPopModifier(settlement, 8) / 2;
				if (missionProbability == 0) {
					return 0;
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
	            	return 0;
	            }
	            
	            // Check if min number of EVA suits at settlement.
	        	if (MissionUtil.getNumberAvailableEVASuitsAtSettlement(settlement) <
	                    ConstructionMission.MIN_PEOPLE) {
	        		return 0;
	            }
	        	
	            try {
	                int constructionSkill = person.getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION);
	                ConstructionValues values =  settlement.getConstructionManager().getConstructionValues();
	
	                // Add construction profit for existing or new construction sites.
	                double constructionProfit = values.getSettlementConstructionProfit(constructionSkill);
	                if (constructionProfit > 0D) {
	                    missionProbability += 10D;
	
	                    double newSiteProfit = values.getNewConstructionSiteProfit(constructionSkill);
	                    double existingSiteProfit = values.getAllConstructionSitesProfit(constructionSkill);
	
	                    // Modify if construction is the person's favorite activity.
	                    if (person.getFavorite().getFavoriteActivity() == FavoriteType.TINKERING
	                    	|| person.getFavorite().getFavoriteActivity() == FavoriteType.FIELD_WORK) {
	                        missionProbability *= 1.25;
	                    }
	                    
	                    if (newSiteProfit > existingSiteProfit) {
	                        missionProbability *= getProbability(settlement);
	                    }
	                }
	            }
	            
	            catch (Exception e) {
	                logger.log(Level.SEVERE, "Error getting construction site.", e);
	                return 0;
	            }
	
	            // Job modifier.
	            missionProbability *= getLeaderSuitability(person);
			}
            
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

        return missionProbability;
    }

    /**
     * Computes probability.
     * 
     * @param settlement
     * @return
     */
    private double getProbability(Settlement settlement) {

        double result = 0D;
        
        // Consider the num of construction sites
        int numSites = settlement.getConstructionManager().getConstructionSites().size();

        // Consider the size of the settlement population
        int numPeople = settlement.getNumCitizens();
        
        int numBots = settlement.getNumBots();
        
        double psuedoNum = numPeople + 0.5 * numBots;
        
        if (psuedoNum - 6 * numSites < 0)
        	return 0;

        result = result/Math.pow(10, 2 + psuedoNum);
        
        return result;
    }
}
