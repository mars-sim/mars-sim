/*
 * Mars Simulation Project
 * SettlementMetaTask.java
 * @date 2022-11-28
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.util;

import java.util.List;
import java.util.Set;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.Settlement;

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
     * Gets the Person applicable modifiers for this  Meta Task.
     * 
     * @param t The Settlement task being evaluated
     * @param p Person in question
     * @return Default returns 0 being no applicable Task
     */
    double getPersonSettlementModifier(SettlementTask t, Person p);

    /**
     * Gets the Robot applicable modifiers for this  Meta Task.
     * 
     * @param t The Settlement task being evaluated
     * @param r Robot in question
     * @return Default returns 0 
     */
    double getRobotSettlementModifier(SettlementTask t, Robot r);
}