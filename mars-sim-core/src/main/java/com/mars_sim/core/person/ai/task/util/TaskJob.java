/*
 * Mars Simulation Project
 * TaskJob.java
 * @date 2024-08-04
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.task.util;

import java.io.Serializable;

import com.mars_sim.core.data.Rating;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.robot.Robot;

/**
 * This represents a potential Task that can be executed. Each Task has a score for the benefit of
 * doing the Task. It is used to rate potential future tasks.
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
