/*
 * Mars Simulation Project
 * AbstractTaskJob.java
 * @date 2022-11-11
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.util;

import org.mars_sim.msp.core.data.RatingScore;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;

/**
 * Abstract implement of a Task Job just holding the name & score.
 */
public abstract class AbstractTaskJob implements TaskJob {

	private static final long serialVersionUID = 1L;
	
	private static final double CAP = 1000D;
    private RatingScore score;
    private String name;

    /**
     * Create an abstract task job using the old simple scoring approach. 
     * THis should be replace with the RatingScore approach.
     * @param description
     * @param score
     * deprecated Use {@link #AbstractTaskJob(String, RatingScore)}
     */
    protected AbstractTaskJob(String description, double score) {
        this(description, new RatingScore(score));
    }

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
