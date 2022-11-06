/*
 * Mars Simulation Project
 * TaskJob.java
 * @date 2022-11-04
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.util;

import java.io.Serializable;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;

/**
 * This rerpresents a potential Task that can be xecuted. The Task has a score for the benefit of
 * doing the Task. 
 */
public interface TaskJob extends Serializable {

    /**
     * Retrns the score benefit of running this Task.
     */
    double getScore();

    /**
     * Description of the task to be performed
     */
    String getDescription();

    /**
     * Create the Task for a person
     */
    Task createTask(Person person);

    /**
     * Create the Task for a Robot
     * @param robot 
     */
    Task createTask(Robot robot);

}
