/*
 * Mars Simulation Project
 * BasicTaskJob.java
 * @date 2022-11-04
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.util;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;

/**
 * This is an implementation of a TaskJob that delegates the Task creation to a
 * MetaTask instance. Tasks have no extra entities defined in the creation.
 */
@SuppressWarnings("serial")
public class BasicTaskJob extends AbstractTaskJob {

    // MetaTask cannot be serialised
    private transient FactoryMetaTask mt;
    private String mtID;

    public BasicTaskJob(FactoryMetaTask metaTask, double score) {
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
