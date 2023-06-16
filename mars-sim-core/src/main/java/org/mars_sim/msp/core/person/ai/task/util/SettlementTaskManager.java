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

import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * This class is responsible for maintaining a list of sharable SettlementTasks that can be
 * assigned to Workers from a shared pool.
 */
public class SettlementTaskManager implements Serializable {

	private static final long serialVersionUID = 1L;
	
    /**
     * Acts as a proxy to a SettlementTask. The proxy ensures that the root Task which is shared
     * is removed from the parent SettlemetTaskmanager when it is created if there is no more demand.
     * This avoids it being re-used.
     */
    class SettlementTaskProxy extends AbstractTaskJob  {

		private static final long serialVersionUID = 1L;
		
        private SettlementTask source;
        private SettlementTaskManager manager;

        SettlementTaskProxy(SettlementTaskManager owner, SettlementTask source, double score) {
            super(source.getDescription(), score);
            this.source = source;
            this.manager = owner;
        }

        /**
         * A Worker has selected this to be work on so reduce the demand.
         */
        private void reduceDemand() {
            // One demand taken, if none left remove from shared pool
            if (source.reduceDemand()) {
                manager.removeTask(source);
            }
        }

        @Override
        public Task createTask(Person person) {
            reduceDemand();
            return source.createTask(person);
        }

        @Override
        public Task createTask(Robot robot) {
            reduceDemand();
            return source.createTask(robot);
        }
    }

//	Can add back private static final SimLogger logger = SimLogger.getLogger(SettlementTaskManager.class.getName())

    private Settlement owner;
    private transient List<SettlementTask> tasks;
    private boolean refreshTasks = true;

    private int callCount;
    private int buildCount = 0;
    private int executedCount = 0;

    public SettlementTaskManager(Settlement owner) {
        this.owner = owner;
    }
    
    /**
     * Removes a shared SettlementTask from the pool.
     * 
     * @param source Item to remove.
     */
    void removeTask(SettlementTask source) {
        executedCount++;
        if (tasks != null) {
            tasks.remove(source);
        }
    }

    /**
     * Gets the current cached Settlement Tasks. If there is no cache or marked as refresh then a list is created.
     */
    private List<SettlementTask> getTasks() {
        if (refreshTasks || (tasks == null)) {
            tasks = new ArrayList<>();
            for(SettlementMetaTask mt : MetaTaskUtil.getSettlementMetaTasks()) {
                tasks.addAll(mt.getSettlementTasks(owner));
            }
            refreshTasks = false;
            buildCount++;

            // Inform listeners
            owner.fireUnitUpdate(UnitEventType.BACKLOG_EVENT);
        }
        callCount++;
        return tasks;
    }

    /**
     * Gets the currently available SettlementTasks. This list maybe be null.
     * 
     * @return
     */
    public List<SettlementTask> getAvailableTasks() {
        return tasks;
    }
    
    /**
     * Gets a list of suitable TaskJob for a particular Robot. This acts as binding the shared SettlementTasks
     * to the particular worker by applying their personal modifier to the score.
     * 
     * @see SettlementMetaTask#getRobotSettlementModifier(Robot)
     * @return Custom list of jobs applicable
     */
    public List<TaskJob> getTasks(Robot r) {
        List<TaskJob> result = new ArrayList<>();
        for(SettlementTask st : getTasks()) {
            SettlementMetaTask mt = st.getMeta();

            // Check this type of Robot can do the Job
            if (mt.getPreferredRobot().contains(r.getRobotType())) {
                double factor = mt.getRobotSettlementModifier(st,r);
                if (factor > 0) {
                    double score = st.getScore() * factor;
                    result.add(new SettlementTaskProxy(this, st, score));
                }
            }
        }

        return result;
    }

    /**
     * Gets a list of suitable TaskJob for a particular Person. This acts as binding the shared SettlementTasks
     * to the particular worker by applying their personal modifier. 
     * 
     * @return Custom list of jobs applicable
     * @see SettlementMetaTask#getPersonSettlementModifier(Person)
     */
    public List<TaskJob> getTasks(Person p) {
        List<TaskJob> result = new ArrayList<>();
        for(SettlementTask st : getTasks()) {
            SettlementMetaTask mt = st.getMeta();
            double factor = mt.getPersonSettlementModifier(st, p);
            if (factor > 0) {
                double score = st.getScore() * factor;
                result.add(new SettlementTaskProxy(this, st, score));
            }
        }
        return result;
    }

    /**
     * How many tasks have been executed out of the shared pool?
     */
    public int getExecutedCount() {
        return executedCount;
    }
    
    /**
     * This is the reuse score of how many times a single Task list is reused by other Workers.
     */
    public double getReuseScore() {
        return (double)callCount/buildCount;
    }

    /**
     * Time has progressed so mark the tasks to be refresh on the next demand.
     */
    public void timePassing() {
        refreshTasks = true;
    }
}
