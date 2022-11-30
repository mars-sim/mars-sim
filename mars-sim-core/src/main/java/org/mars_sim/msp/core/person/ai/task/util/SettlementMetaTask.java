/*
 * Mars Simulation Project
 * SettlementMetaTask.java
 * @date 2022-11-28
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.util;

import java.util.List;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Meta task that create tasks specific to a Settlement. It doesn't have the ability to create Task 
 * instances itself. These are used but the ShiftTaskManager to maintain a shared pool of tasks.
 * @see SettlementTaskManager
 */
public abstract class SettlementMetaTask extends MetaTask {

    protected SettlementMetaTask(String name, WorkerType workerType) {
        super(name, workerType, TaskScope.SETTLEMENT);
    }
    
    /**
     * Get the potential pending Tasks for a Settlment. These have no person/robot modifiers applied.
     * @param settlement Swettlement to be scanned
     * @return List if SettlementTasks
     */
    public abstract List<SettlementTask> getSettlementTasks(Settlement settlement);

    /**
     * Get the Person applicable modifiers for this  Meta Task.
     * @param t The Settlement task being evaluated
     * @param p Person in question
     * @return Default returns 0 being no applicable Task
     */
    public double getPersonSettlementModifier(SettlementTask t, Person p) {
        return 0;
    }

    /**
     * Get the Robot applicable modifiers for this  Meta Task.
     * @param t The Settlement task being evaluated
     * @param r Robot in question
     * @return Default returns 0 
     */
    public double getRobotSettlementModifier(SettlementTask t, Robot r) {
        return 0;
    }
}
