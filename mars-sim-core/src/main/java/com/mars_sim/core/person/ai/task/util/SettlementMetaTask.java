/*
 * Mars Simulation Project
 * SettlementMetaTask.java
 * @date 2022-11-28
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.task.util;

import java.util.List;
import java.util.Set;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.Settlement;

/**
 * Meta task that create tasks specific to a Settlement. It doesn't have the ability to create Task 
 * instances itself. These are used but the ShiftTaskManager to maintain a shared pool of tasks.
 * 
 * @see SettlementTaskManager
 */
public interface SettlementMetaTask {

    /**
	 * Gets the Robots that is most suitable to this Task.
	 * 
	 * @return
	 */
	Set<RobotType> getPreferredRobot();

    /**
     * Gets the potential pending Tasks for a Settlement. These have no person/robot modifiers applied.
     * 
     * @param settlement Settlement to be scanned
     * @return List if SettlementTasks
     */
    List<SettlementTask> getSettlementTasks(Settlement settlement);

    /**
     * Assess a Person for a specific SettlementTask of this type.
     * 
     * @param t The Settlement task being evaluated
     * @param p Person in question
     * @return A new rating score applying the Person's modifiers
     */
    RatingScore assessPersonSuitability(SettlementTask t, Person p);

    /**
     * Assess a Robot for a specific SettlementTask of this type.
     * Default implementation return Robot is not suitable.
     * 
     * @param t The Settlement task being evaluated
     * @param r Robot in question
     * @return A new rating score applying the Robot's modifiers
     */
    default RatingScore assessRobotSuitability(SettlementTask t, Robot r) {
        return RatingScore.ZERO_RATING;
    }
}