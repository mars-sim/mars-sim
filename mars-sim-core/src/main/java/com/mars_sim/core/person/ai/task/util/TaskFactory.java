/*
 * Mars Simulation Project
 * PersonTaskFactory.java
 * @date 2023-10-29
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.task.util;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.robot.Robot;

/**
 * Represents a Task Factory that can create Tasks for Workers
 */
public interface TaskFactory {

    String getName();

	String getID();

    /**
	 * Constructs an instance of the associated task. 
	 * 
	 * @param person the person to perform the task.
	 * @return task instance.
	 */
	Task constructInstance(Person person);

	/**
	 * Constructs an instance of the associated task. 
	 * 
	 * @param robot the Robot to perform the task.
	 * @return task instance.
	 */
	Task constructInstance(Robot robot);
}
