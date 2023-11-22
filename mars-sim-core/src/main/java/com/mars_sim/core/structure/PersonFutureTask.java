/*
 * Mars Simulation Project
 * PersonFutureTask.java
 * @date 2023-11-18
 * @author Barry Evans
 */
package com.mars_sim.core.structure;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.events.ScheduledEventHandler;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.BasicTaskJob;
import com.mars_sim.core.person.ai.task.util.MetaTaskUtil;
import com.mars_sim.core.person.ai.task.util.TaskFactory;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.time.MarsTime;

/**
 * This class represents a future event that will create a task for a person.
 * It acts as a bridge between the ScheduledEvent and the TaskFactory.
 */
public class PersonFutureTask implements ScheduledEventHandler{

    private static SimLogger logger = SimLogger.getLogger(PersonFutureTask.class.getName());

    private transient TaskFactory factory;
    private String mtID;
    private Person person;

    /**
     * Creates a future event that will create a task for a person.

     * @param p Person wanting the Task
     * @param mt Factory to create the task
     */
    public PersonFutureTask(Person p, TaskFactory mt) {
        this.person = p;
        this.factory = mt;
        this.mtID = mt.getID();
    }

    /**
     * Gets the task factory for this event. This supports retrieving the factory
     * after serialization.   
     * @return
     */
    private TaskFactory getFactory() {
        if (factory == null) {
            factory = (TaskFactory) MetaTaskUtil.getMetaTask(mtID);
        }

        return factory;
    }

    /**
     * Gets the name of the event
     * 
     * @return name of the event
     */
    @Override
    public String getEventDescription() {
        return person.getName() + " - Future task of " + getFactory().getName();
    }

    /**
     * When executed this will create a pending Task and add it to the person.
     * 
     * @param currentTime This time of the event fired but not needed
     * @return Returns zero as this is not rescheduled.
     */
    @Override
    public int execute(MarsTime currentTime) {

//        logger.info(person, "Executing future task to add " + getFactory().getName() + ".");
        TaskJob job = new BasicTaskJob(getFactory(), RatingScore.ZERO_RATING);
        person.getTaskManager().addPendingTask(job, false);
        
        // Do not reschedule this event
        return 0;
    }
}
