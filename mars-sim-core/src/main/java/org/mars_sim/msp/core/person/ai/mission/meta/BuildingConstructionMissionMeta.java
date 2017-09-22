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
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.core.structure.construction.ConstructionValues;


/**
 * A meta mission for the BuildingConstructionMission mission.
 */
public class BuildingConstructionMissionMeta implements MetaMission {

    /** Mission name */
    private static final String NAME = Msg.getString(
            "Mission.description.buildingConstructionMission"); //$NON-NLS-1$

    /** default logger. */
    private static Logger logger = Logger.getLogger(MiningMeta.class.getName());

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Mission constructInstance(Person person) {
        return new BuildingConstructionMission(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (Simulation.instance().getMasterClock().getMarsClock().getMissionSol() 
        		< BuildingConstructionMission.FIRST_AVAILABLE_SOL)
        	return 0;


        // Check if person is in a settlement.
        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            Settlement settlement = person.getSettlement();

            boolean go = true;

            int availablePeopleNum = 0;
/*
            // No construction until after the first ten sols of the simulation.
            MarsClock startTime = Simulation.instance().getMasterClock().getInitialMarsTime();
            MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
            double totalTimeMillisols = MarsClock.getTimeDiff(currentTime, startTime);
            double totalTimeSols = totalTimeMillisols / 1000D;
*/
            Collection<Person> list = settlement.getInhabitants();
            for (Person member : list) {
                boolean noMission = !member.getMind().hasActiveMission();
                boolean isFit = !member.getPhysicalCondition().hasSeriousMedicalProblems();
                if (noMission && isFit)
                	availablePeopleNum++;
            }

            // Check if available light utility vehicles.
            if (!BuildingConstructionMission.isLUVAvailable(settlement))
            	go = false;

            // Check if enough available people at settlement for mission.
            else if (!(availablePeopleNum >= BuildingConstructionMission.MIN_PEOPLE))
            	go = false;

            // Check if settlement has construction override flag set.
            else if (settlement.getConstructionOverride())
            	go = false;

            // No construction until after the first ten sols of the simulation.
            //else if (totalTimeSols < FIRST_AVAILABLE_SOL)
            //	go = false;

            // Check if min number of EVA suits at settlement.
        	else if (Mission.getNumberAvailableEVASuitsAtSettlement(person.getSettlement()) <
                    BuildingConstructionMission.MIN_PEOPLE) {
                //result = 0D;
            	go = false;
            }

            if (go) {
            //if (reservableLUV && enoughPeople && !constructionOverride && !firstTenSols) {
                ConstructionManager manager = settlement.getConstructionManager();
                try {
                    int constructionSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION);
                    ConstructionValues values =  manager.getConstructionValues();

                    // Add construction profit for existing or new construction sites.
                    double constructionProfit = values.getSettlementConstructionProfit(constructionSkill);
                    if (constructionProfit > 0D) {
                        result = 100D;

                        double newSiteProfit = values.getNewConstructionSiteProfit(constructionSkill);
                        double existingSiteProfit = values.getAllConstructionSitesProfit(constructionSkill);

                        // Modify if construction is the person's favorite activity.
                        if (person.getFavorite().getFavoriteActivity().equalsIgnoreCase("Construction")) {
                            result *= 1.1D;
                        }

                        if (newSiteProfit > existingSiteProfit) {
                            // Divide profit by 10 to the power of the number of existing construction sites.
                            int numSites = manager.getConstructionSites().size();

                            // 2016-06-06 Added considering the size of the settlement population
                            int numPeople = settlement.getNumCurrentPopulation();
                            int limit = (int)(2D * numSites - numPeople/24D);

                            result = result/Math.pow(10, 2 + limit) /5D;

                        }


                    }
                }
                catch (Exception e) {
                    logger.log(Level.SEVERE, "Error getting construction site.", e);
                }
            }

            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) {
                result *= job.getStartMissionProbabilityModifier(BuildingConstructionMission.class);
            }
        }

        if (result > 1.1) System.out.println("result : "+ result);
        return result;
    }

	@Override
	public Mission constructInstance(Robot robot) {
        return null;//new BuildingConstructionMission(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 0D;
/*
        if (robot.getBotMind().getRobotJob() instanceof Constructionbot)
	        // Check if robot is in a settlement.
	        if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
	            Settlement settlement = robot.getSettlement();

	            // Check if available light utility vehicles.
	            boolean reservableLUV = BuildingConstructionMission.isLUVAvailable(settlement);

	            // Check if enough available people at settlement for mission.

	//            int availablePeopleNum = 0;
	 //           Iterator<Robot> i = settlement.getRobots().iterator();
	//            while (i.hasNext()) {
	//                Robot member = i.next();
	 //               boolean noMission = !member.getBotMind().hasActiveMission();
	//                boolean isFit = !member.getPhysicalCondition().hasSeriousMedicalProblems();
	//                if (noMission && isFit) availablePeopleNum++;
	//            }
	//            boolean enoughPeople = (availablePeopleNum >= BuildingConstructionMission.MIN_PEOPLE);

	            // Check if settlement has construction override flag set.
	            boolean constructionOverride = settlement.getConstructionOverride();

	            // No construction until after the first ten sols of the simulation.
	            MarsClock startTime = Simulation.instance().getMasterClock().getInitialMarsTime();
	            MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
	            double totalTimeMillisols = MarsClock.getTimeDiff(currentTime, startTime);
	            double totalTimeSols = totalTimeMillisols / 1000D;
	            boolean firstTenSols = (totalTimeSols < 10D);

	            if (reservableLUV  && !constructionOverride && !firstTenSols) {

	                try {
	                    int constructionSkill = robot.getBotMind().getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION);
	                    ConstructionValues values =  settlement.getConstructionManager().getConstructionValues();

	                    // Add construction profit for existing or new construction sites.
	                    double constructionProfit = values.getSettlementConstructionProfit(constructionSkill);
	                    if (constructionProfit > 0D) {
	                        result = 10D;

	                        double newSiteProfit = values.getNewConstructionSiteProfit(constructionSkill);
	                        double existingSiteProfit = values.getAllConstructionSitesProfit(constructionSkill);

	                        if (newSiteProfit > existingSiteProfit) {
	                            // Divide profit by 10 to the power of the number of existing construction sites.
	                            ConstructionManager manager = settlement.getConstructionManager();
	                            int numSites = manager.getConstructionSites().size();
	                            result/= Math.pow(10, numSites);
	                        }
	                    }
	                }
	                catch (Exception e) {
	                    logger.log(Level.SEVERE, "Error getting construction site.", e);
	                }
	            }

	            // Check if min number of EVA suits at settlement.
	            //if (Mission.getNumberAvailableEVASuitsAtSettlement(robot.getSettlement()) <
	            //        BuildingConstructionMission.MIN_PEOPLE) {
	            //    result = 0D;
	            //}

	        }
*/
        return result;
    }
}