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
public class BasicTaskJob implements TaskJob {

    private static final double MAX_TASK_SCORE = 35_000;

    private double score;
    private MetaTask mt;

    BasicTaskJob(MetaTask metaTask, double score) {
        this.mt = metaTask;

        if (Double.isNaN(score) || Double.isInfinite(score) || (score > MAX_TASK_SCORE)) {
            score = MAX_TASK_SCORE;
        }
        this.score = score;
    }

    @Override
    public double getScore() {
        return score;
    }

    @Override
    public String getDescription() {
        return mt.getName();
    }

    @Override
    public Task createTask(Person person) {
        return mt.constructInstance(person);
    }

    @Override
    public Task createTask(Robot robot) {
        return mt.constructInstance(robot);
    }
}
