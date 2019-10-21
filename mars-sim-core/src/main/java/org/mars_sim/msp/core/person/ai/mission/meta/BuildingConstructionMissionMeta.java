/**
 * Mars Simulation Project
 * BuildingConstructionMissionMeta.java
 * @version 3.1.0 2017-09-22
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;


import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.construction.ConstructionValues;


/**
 * A meta mission for the BuildingConstructionMission mission.
 */
public class BuildingConstructionMissionMeta implements MetaMission {

    /** Mission name */
    private static final String DEFAULT_DESCRIPTION = Msg.getString(
            "Mission.description.buildingConstructionMission"); //$NON-NLS-1$

    /** default logger. */
    private static Logger logger = Logger.getLogger(MiningMeta.class.getName());

    private static final double LIMIT = 100D;
    
//    private static MarsClock marsClock;
    
    @Override
    public String getName() {
        return DEFAULT_DESCRIPTION;
    }

    @Override
    public Mission constructInstance(Person person) {
        return new BuildingConstructionMission(person);
    }

    @Override
    public double getProbability(Person person) {

        double missionProbability = 0D;
        
//        if (marsClock == null)
//        	marsClock = Simulation.instance().getMasterClock().getMarsClock();
////        
        // No construction until after the x sols of the simulation.
        if (marsClock.getMissionSol() < BuildingConstructionMission.FIRST_AVAILABLE_SOL)
        	return 0;
        
        // Check if person is in a settlement.
        if (person.isInSettlement()) {
            Settlement settlement = person.getSettlement();

            int availablePeopleNum = 0;

            Collection<Person> list = settlement.getIndoorPeople();
            for (Person member : list) {
                boolean noMission = !member.getMind().hasActiveMission();
                boolean isFit = !member.getPhysicalCondition().hasSeriousMedicalProblems();
                if (noMission && isFit)
                	availablePeopleNum++;
            }

            // Check if available light utility vehicles.
            if (!BuildingConstructionMission.isLUVAvailable(settlement))
            	return 0;

            // Check if enough available people at settlement for mission.
            else if (!(availablePeopleNum >= BuildingConstructionMission.MIN_PEOPLE))
            	return 0;

            // Check if settlement has construction override flag set.
            else if (settlement.getConstructionOverride())
            	return 0;

            // Check if min number of EVA suits at settlement.
        	else if (Mission.getNumberAvailableEVASuitsAtSettlement(settlement) <
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
                    if (person.getFavorite().getFavoriteActivity() == FavoriteType.TINKERING)
                        missionProbability *= 1.1D;

                    if (newSiteProfit > existingSiteProfit) {
                        missionProbability = getProbability(settlement);
                    }
                }
            }
            
            catch (Exception e) {
                logger.log(Level.SEVERE, "Error getting construction site.", e);
            }

            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) {
                missionProbability *= job.getStartMissionProbabilityModifier(BuildingConstructionMission.class);
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

        //if (result > 1.1) logger.info("probability : "+ result);
        return missionProbability;
    }

    public double getProbability(Settlement settlement) {

        double result = 0D;
        
        int numSites = settlement.getConstructionManager().getConstructionSites().size();

        // Consider the size of the settlement population
        int numPeople = settlement.getNumCitizens();//.getIndoorPeopleCount();
        int limit = (int)(2D * numSites - numPeople/24D);

        result = result/Math.pow(10, 2 + limit);// /5D;

        //if (result > 1.1) logger.info("probability : "+ result);
        return result;
    }
    
    public double getSettlementProbability(Settlement settlement) {

        double result = 0D;
        
//        if (marsClock == null)
//        	marsClock = Simulation.instance().getMasterClock().getMarsClock();
//        
        // No construction until after the x sols of the simulation.
        if (marsClock.getMissionSol() < BuildingConstructionMission.FIRST_AVAILABLE_SOL)
        	return 0;
        
        int availablePeopleNum = 0;

        Collection<Person> list = settlement.getIndoorPeople();
        for (Person member : list) {
            boolean noMission = !member.getMind().hasActiveMission();
            boolean isFit = !member.getPhysicalCondition().hasSeriousMedicalProblems();
            if (noMission && isFit)
            	availablePeopleNum++;
        }

        // Check if available light utility vehicles.
        if (!BuildingConstructionMission.isLUVAvailable(settlement))
        	return 0;

        // Check if enough available people at settlement for mission.
        else if (!(availablePeopleNum >= BuildingConstructionMission.MIN_PEOPLE))
        	return 0;

        // Check if settlement has construction override flag set.
        else if (settlement.getConstructionOverride())
        	return 0;

        // Check if min number of EVA suits at settlement.
    	else if (Mission.getNumberAvailableEVASuitsAtSettlement(settlement) <
                BuildingConstructionMission.MIN_PEOPLE) {
    		return 0;
        }
            
        result = getProbability(settlement);
        
        return result;
    }
    
	@Override
	public Mission constructInstance(Robot robot) {
        return null;//new BuildingConstructionMission(robot);
	}

	@Override
	public double getProbability(Robot robot) {
        return 0;
    }
	
//	public static void setInstances(MarsClock c) {
//		marsClock = c;
//	}
	
}