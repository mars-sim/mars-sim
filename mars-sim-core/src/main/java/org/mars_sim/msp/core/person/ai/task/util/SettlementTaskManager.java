/*
 * Mars Simulation Project
 * SettlementTaskManager.java
 * @date 2022-11-28
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * This class is repsonsible for maintaining a list of sharable SettlementTasks that can be
 * assigned to Workers from a shared pool.
 */
public class SettlementTaskManager implements Serializable {
    /**
     * Acts as a Proxy to a SettlementTask. The proxy ensures that the root Task which is shared
     * is remvoed fromt he parent SettlemetnTaskmanager when it is created to avoid it being re-used.
     */
    class SettlementTaskProxy extends AbstractTaskJob  {
        
        private SettlementTask source;
        private SettlementTaskManager manager;

        SettlementTaskProxy(SettlementTaskManager owner, SettlementTask source, double score) {
            super(source.getDescription(), score);
            this.source = source;
            this.manager = owner;
        }

        @Override
        public Task createTask(Person person) {
            manager.removeTask(source);
            return source.createTask(person);
        }

        @Override
        public Task createTask(Robot robot) {
            manager.removeTask(source);
            return source.createTask(robot);
        }
    }

	private static final SimLogger logger = SimLogger.getLogger(Settlement.class.getName());

    private Settlement owner;
    private List<SettlementTask> tasks;

    private int callCount;

    public SettlementTaskManager(Settlement owner) {
        this.owner = owner;
    }
    
    /**
     * Remove a shared SettlementTask from the pool.
     * @param source Item to remove.
     */
    void removeTask(SettlementTask source) {
        if (tasks != null) {
            logger.info(owner, "Remove used Settlement Task " + source.getDescription());
            tasks.remove(source);
        }
    }

    /**
     * Get the current cached Settlement Tasks. If there is no cahce; then a lsi is created.
     */
    private List<SettlementTask> getTasks() {
        if (tasks == null) {
            tasks = new ArrayList<>();
            for(SettlementMetaTask mt : MetaTaskUtil.getSettlementMetaTasks()) {
                tasks.addAll(mt.getSettlementTasks(owner));
            }
        }
        callCount++;
        return tasks;
    }

    /**
     * Get the currently available SettlementTasks. This list maybe be null.
     * @return
     */
    public List<SettlementTask> getAvailableTasks() {
        return tasks;
    }
    
    /**
     * Get a list of suitable TaskJob for a particualr Robot. This acts as binding the shared SettlementTasks
     * to the particular worker by applying their personal modifier to the score.
     * @see SettlementMetaTask#getRobotSettlementModifier(Robot)
     * @return Custom list of jobs applicable
     */
    public List<TaskJob> getTasks(Robot r) {
        List<TaskJob> result = new ArrayList<>();
        for(SettlementTask st : getTasks()) {
            SettlementMetaTask mt = st.getMeta();
            double factor = mt.getRobotSettlementModifier(r);
            if (factor > 0) {
                double score = st.getScore() * factor;
                result.add(new SettlementTaskProxy(this, st, score));
            }
        }

        return result;
    }

    /**
     * Get a list of suitable TaskJob for a particular Person. This acts as binding the shared SettlementTasks
     * to the particular worker by applying their personal modifier. 
     * @return Custom list of jobs applicable
     * @see SettlementMetaTask#getPersonSettlementModifier(Person)
     */
    public List<TaskJob> getTasks(Person p) {
        List<TaskJob> result = new ArrayList<>();
        for(SettlementTask st : getTasks()) {
            SettlementMetaTask mt = st.getMeta();
            double factor = mt.getPersonSettlementModifier(p);
            if (factor > 0) {
                double score = st.getScore() * factor;
                result.add(new SettlementTaskProxy(this, st, score));
            }
        }
        return result;
    }

    /**
     * Time has progressed so clear any cached Tasks.
     */
    public void timePassing() {
        if (callCount > 0) {
            //logger.info(owner, "Called " + callCount);
        }
        tasks = null;
        callCount = 0;
    }
}
