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
 * MetaTask instance. Tasks have no extra entities defined in the creation.
 */
public class BasicTaskJob extends AbstractTaskJob {

    // MetaTask cannot be serialised
    private transient FactoryMetaTask mt;
    private String mtID;

    /**
     * Create a basic implemmation of a TaskJob that references a MetaTask as the generator
     * of the actual Task.
     * @param metaTask Creator of the Task
     * @param score The score of the activity
     */
    public BasicTaskJob(FactoryMetaTask metaTask, RatingScore score) {
        super(metaTask.getName(), score);
        
        this.mtID = metaTask.getID();
        this.mt = metaTask;
    }

    /**
     * Reconnects to the MetaTask even after a deserialised instance.
     */
    private FactoryMetaTask getMeta() {
        if (mt == null) {
            mt = (FactoryMetaTask) MetaTaskUtil.getMetaTask(mtID);
        }

        return mt;
    }
    
    @Override
    public Task createTask(Person person) {
    	return getMeta().constructInstance(person);
    }
    
    @Override
    public Task createTask(Robot robot) {
        return getMeta().constructInstance(robot);
    }
}
