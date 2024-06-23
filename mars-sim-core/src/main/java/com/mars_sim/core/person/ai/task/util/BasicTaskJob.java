/*
 * Mars Simulation Project
 * BasicTaskJob.java
 * @date 2022-11-04
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.task.util;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.robot.Robot;

/**
 * This is an implementation of a TaskJob that delegates the Task creation to a
 * TaskFactory instance. Tasks have no extra entities defined in the creation.
 */
public class BasicTaskJob extends AbstractTaskJob {

    private static final long serialVersionUID = 1L;
	// MetaTask cannot be serialised
    private transient TaskFactory mt;
    private String mtID;

    /**
     * Create a basic implemmation of a TaskJob that references a MetaTask as the generator
     * of the actual Task.
     * @param factory Creator of the Task
     * @param score The score of the activity
     */
    public BasicTaskJob(TaskFactory factory, RatingScore score) {
        super(factory.getName(), score);
        
        this.mtID = factory.getID();
        this.mt = factory;
    }

    /**
     * Reconnects to the MetaTask even after a deserialised instance.
     */
    private TaskFactory getFactory() {
        if (mt == null) {
            mt = (TaskFactory) MetaTaskUtil.getMetaTask(mtID);
        }

        return mt;
    }
    
    @Override
    public Task createTask(Person person) {
    	return getFactory().constructInstance(person);
    }
    
    @Override
    public Task createTask(Robot robot) {
        return getFactory().constructInstance(robot);
    }
}
