/*
 * Mars Simulation Project
 * TaskJob.java
 * @date 2022-11-04
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.util;

import java.io.Serializable;

import org.mars_sim.msp.core.data.Rating;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;

/**
 * This represents a potential Task that can be executed. The Task has a score for the benefit of
 * doing the Task. 
 * This is used to rate potnetial future tasks.
 */
public interface TaskJob extends Rating, Serializable {

    /**
     * Creates the task for a person.
     */
    Task createTask(Person person);

    /**
     * Creates the task for a Robot.
     * 
     * @param robot 
     */
    Task createTask(Robot robot);

}
