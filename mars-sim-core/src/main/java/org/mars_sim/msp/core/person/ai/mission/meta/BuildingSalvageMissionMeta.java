/**
 * Mars Simulation Project
 * BuildingSalvageMissionMeta.java
 * @version 3.08 2015-02-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.BuildingSalvageMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.Constructionbot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.construction.SalvageValues;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * A meta mission for the BuildingSalvageMission mission.
 */
public class BuildingSalvageMissionMeta implements MetaMission {

    /** Mission name */
    private static final String NAME = Msg.getString(
            "Mission.description.salvageBuilding"); //$NON-NLS-1$

    /** default logger. */
    private static Logger logger = Logger.getLogger(BuildingSalvageMissionMeta.class.getName());

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Mission constructInstance(Person person) {
        return new BuildingSalvageMission(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        // Check if person is in a settlement.
        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            Settlement settlement = person.getSettlement();

            // Check if available light utility vehicles.
            boolean reservableLUV = BuildingSalvageMission.isLUVAvailable(settlement);

            // Check if enough available people at settlement for mission.
            int availablePeopleNum = 0;
            Iterator<Person> i = settlement.getInhabitants().iterator();
            while (i.hasNext()) {
                Person member = i.next();
                boolean noMission = !member.getMind().hasActiveMission();
                boolean isFit = !member.getPhysicalCondition()
                        .hasSeriousMedicalProblems();
                if (noMission && isFit) {
                    availablePeopleNum++;
                }
            }
            boolean enoughPeople = (availablePeopleNum >= BuildingSalvageMission.MIN_PEOPLE);

            // No construction until after the first ten sols of the simulation.
            MarsClock startTime = Simulation.instance().getMasterClock().getInitialMarsTime();
            MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
            double totalTimeMillisols = MarsClock.getTimeDiff(currentTime, startTime);
            double totalTimeSols = totalTimeMillisols / 1000D;
            boolean firstTenSols = (totalTimeSols < 10D);

            // Check if settlement has construction override flag set.
            boolean constructionOverride = settlement.getConstructionOverride();

            if (reservableLUV && enoughPeople && !constructionOverride && !firstTenSols) {
                try {
                    int constructionSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION);
                    SalvageValues values = settlement.getConstructionManager()
                            .getSalvageValues();
                    double salvageProfit = values
                            .getSettlementSalvageProfit(constructionSkill);
                    result = salvageProfit;
                    if (result > 10D) {
                        result = 10D;
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE,
                            "Error getting salvage construction site by a person.", e);
                }
            }

            // Check if min number of EVA suits at settlement.
            if (Mission.getNumberAvailableEVASuitsAtSettlement(person
                    .getSettlement()) < BuildingSalvageMission.MIN_PEOPLE) {
                result = 0D;
            }

            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) {
                result *= job.getStartMissionProbabilityModifier(BuildingSalvageMission.class);
            }
        }

        return result;
    }

	@Override
	public Mission constructInstance(Robot robot) {
        return null;//new BuildingSalvageMission(robot);
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
	            boolean reservableLUV = BuildingSalvageMission.isLUVAvailable(settlement);
	
	            // No construction until after the first ten sols of the simulation.
	            MarsClock startTime = Simulation.instance().getMasterClock().getInitialMarsTime();
	            MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
	            double totalTimeMillisols = MarsClock.getTimeDiff(currentTime, startTime);
	            double totalTimeSols = totalTimeMillisols / 1000D;
	            boolean firstTenSols = (totalTimeSols < 10D);

	            // Check if settlement has construction override flag set.
	            boolean constructionOverride = settlement.getConstructionOverride();

	            if (reservableLUV && !constructionOverride && !firstTenSols) { // && enoughPeople
	                try {
	                    int constructionSkill = robot.getBotMind().getSkillManager().getEffectiveSkillLevel(SkillType.CONSTRUCTION);
	                    SalvageValues values = settlement.getConstructionManager()
	                            .getSalvageValues();
	                    double salvageProfit = values
	                            .getSettlementSalvageProfit(constructionSkill);
	                    result = salvageProfit;
	                    if (result > 10D) {
	                        result = 10D;
	                    }
	                } catch (Exception e) {
	                    logger.log(Level.SEVERE,
	                            "Error getting salvage construction site by a robot.", e);
	                }
	            }

	            // Check if min number of EVA suits at settlement.
	            if (Mission.getNumberAvailableEVASuitsAtSettlement(robot
	                    .getSettlement()) < BuildingSalvageMission.MIN_PEOPLE)
	                result = 0D;


	        }
*/
        return result;
    }
}