/**
 * Mars Simulation Project
 * BuildingConstructionMissionMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;


import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.MissionUtil;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.structure.OverrideType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.construction.ConstructionValues;


/**
 * A meta mission for the BuildingConstructionMission mission.
 */
public class BuildingConstructionMissionMeta extends AbstractMetaMission {

    /** default logger. */
    private static final Logger logger = Logger.getLogger(MiningMeta.class.getName());
      
    BuildingConstructionMissionMeta() {
    	super(MissionType.BUILDING_CONSTRUCTION, Set.of(JobType.ARCHITECT));
    }
    
    @Override
    public Mission constructInstance(Person person, boolean needsReview) {
        return new BuildingConstructionMission(person);
    }

    @Override
    public double getProbability(Person person) {

        double missionProbability = 0D;
         
        // No construction until after the x sols of the simulation.
        if (marsClock.getMissionSol() < BuildingConstructionMission.FIRST_AVAILABLE_SOL) {
        	return 0;
        }
        
        // Check if person is in a settlement.
        if (person.isInSettlement()) {
            Settlement settlement = person.getSettlement();
       
            RoleType roleType = person.getRole().getType();
			
			if (person.getMind().getJob() == JobType.ARCHITECT
					|| RoleType.MISSION_SPECIALIST == roleType
					|| RoleType.CHIEF_OF_MISSION_PLANNING == roleType
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
	            if (!BuildingConstructionMission.isLUVAvailable(settlement)) {
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
	            if (availablePeopleNum < BuildingConstructionMission.MIN_PEOPLE) {
	            	return 0;
	            }
	            
	            // Check if min number of EVA suits at settlement.
	        	if (MissionUtil.getNumberAvailableEVASuitsAtSettlement(settlement) <
	                    BuildingConstructionMission.MIN_PEOPLE) {
	        		return 0;
	            }
	        	
	            try {
	                int constructionSkill = person.getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION);
	                ConstructionValues values =  settlement.getConstructionManager().getConstructionValues();
	
	                // Add construction profit for existing or new construction sites.
	                double constructionProfit = values.getSettlementConstructionProfit(constructionSkill);
	                if (constructionProfit > 0D) {
	                    missionProbability = 100D;
	
	                    double newSiteProfit = values.getNewConstructionSiteProfit(constructionSkill);
	                    double existingSiteProfit = values.getAllConstructionSitesProfit(constructionSkill);
	
	                    // Modify if construction is the person's favorite activity.
	                    if (person.getFavorite().getFavoriteActivity() == FavoriteType.TINKERING) {
	                        missionProbability *= 1.1D;
	                    }
	                    
	                    if (newSiteProfit > existingSiteProfit) {
	                        missionProbability = getProbability(settlement);
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

    private double getProbability(Settlement settlement) {

        double result = 0D;
        
        int numSites = settlement.getConstructionManager().getConstructionSites().size();

        // Consider the size of the settlement population
        int numPeople = settlement.getNumCitizens();
        int limit = (int)(2D * numSites - numPeople/24D);

        result = result/Math.pow(10, 2D + limit);

        return result;
    }
}
