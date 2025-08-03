/*
 * Mars Simulation Project
 * AbstractTaskJob.java
 * @date 2022-11-11
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.task.util;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.robot.Robot;

/**
 * Abstract implementation of a Task Job for holding its name & the rating score.
 */
public abstract class AbstractTaskJob implements TaskJob {

	private static final long serialVersionUID = 1L;
	
	private static final double CAP = 1000D;
    private RatingScore score;
    private String name;

    /**
     * Creates an abstract task job.
     * 
     * @param name
     * @param score
     */
    protected AbstractTaskJob(String name, RatingScore score) {
    	this.name = name;
  
        this.score = score;
        this.score.applyRange(0, CAP);
    }

    @Override
    public RatingScore getScore() {
        return score;
    }

    @Override
    public String getName() {
        return name;
    }  

    /**
     * Default implementation throws an Unsupported operation exception.
     */
    @Override
    public Task createTask(Robot robot) {
        throw new UnsupportedOperationException("Robots cannot do " + name);
    }

    /**
     * Default implementation throws an Unsupported operation exception.
     * 
	 * @param person the person to perform the task.
	 * @return task instance.
     */
    @Override
    public Task createTask(Person person) {
        throw new UnsupportedOperationException("Persons cannot do " + name);
    }
    
    @Override
    public String toString() {
        return name;
    }
}
