/*
 * Mars Simulation Project
 * RobotDemand.java
 * @date 2022-12-23
 * @author Barry Evans
 */
package org.mars_sim.msp.core.robot;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.job.util.JobUtil;
import org.mars_sim.msp.core.robot.ai.job.RobotJob;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * This class scores the Robot demans in a Settlement
 */
public class RobotDemand {
    private static SimLogger logger = SimLogger.getLogger(RobotDemand.class.getName());

    private Settlement target;
    private Map<RobotType,Double> ideal;

    /**
     * Analyse the robot demand for a Settlement.
     * @param target Settlemetn to analyse
     */
    public RobotDemand(Settlement target) {
        this.target = target;
    
        // First get the expected Robot counts per type
        double idealRobotCount = 0;
        Map<RobotType,Double> needed = new HashMap<>();
        for(RobotType rt: RobotType.values()) {
            RobotJob rJob = JobUtil.getRobotJob(rt);
            double required = rJob.getOptimalCount(target);
        
            //logger.info(target, "Optimal " + rt + " = " + required);
            if (required > 0) {
                needed.put(rt, required);
                idealRobotCount = required;
            }
        }

        // Second convert them into perentages
        final double totalRobots = idealRobotCount;
        ideal = needed.entrySet().stream()
                                .collect(Collectors.toMap(
                                                    Map.Entry::getKey,
                                                    e -> (double)e.getValue()/totalRobots));
                                            
    }

    /**
     * The type of Robot this settlement needs most
     * @return
     */
    public RobotType getBestNewRobot() {
        // What is the current allocation
        Collection<Robot> currentRobots = target.getRobots();
        Map<RobotType,Long>  actual = currentRobots.stream()
                .collect(Collectors.groupingBy(Robot::getRobotType, Collectors.counting()));

        int robotCount = currentRobots.size() + 1;      

        // Find the RobotType with the biggest %age shortfall
        RobotType selected = null;
        double biggestShortFall = Double.MIN_VALUE;
        for(Entry<RobotType, Double> required : ideal.entrySet()) {
            double targetCount = required.getValue() * robotCount;
            long actualCount = actual.getOrDefault(required.getKey(), 0L);

            // Calculate shortfall as a percentage of the target. This ensures a balance
            double shortfall = (targetCount - actualCount)/targetCount;
            if (shortfall > biggestShortFall) {
                biggestShortFall = shortfall;
                selected = required.getKey();
            }
        }

        // Failsafe
        if (selected == null) {
            // should never happen
            selected = RobotType.REPAIRBOT;
        }
        //logger.info(target, "Selected  " + selected + ", biggest =" + biggestShortFall);

        return selected;
    }
}
