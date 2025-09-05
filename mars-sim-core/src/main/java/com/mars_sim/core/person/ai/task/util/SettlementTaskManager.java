/*
 * Mars Simulation Project
 * SettlementTaskManager.java
 * @date 2022-11-28
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.task.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.MetaTask.TaskScope;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;

/**
 * This class is responsible for maintaining a list of sharable SettlementTasks that can be
 * assigned to Workers from a shared pool.
 */
public class SettlementTaskManager implements Serializable {

	private static final long serialVersionUID = 1L;
    private static final Set<TaskScope> ON_DUTY_SCOPES = Set.of(TaskScope.ANY_HOUR, TaskScope.WORK_HOUR);
    private static final Set<TaskScope> OFF_DUTY_SCOPES = Set.of(TaskScope.ANY_HOUR, TaskScope.NONWORK_HOUR);
	
    /**
     * Acts as a proxy to a SettlementTask. The proxy ensures that the root Task which is shared
     * is removed from the parent SettlemetTaskmanager when it is created if there is no more demand.
     * This avoids it being re-used.
     */
    class SettlementTaskProxy extends AbstractTaskJob  {

		private static final long serialVersionUID = 1L;
        private SettlementTask source;
        private SettlementTaskManager manager;

        SettlementTaskProxy(SettlementTaskManager owner, SettlementTask source, RatingScore score) {
            super(source.getName(), score);
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


    private boolean refreshTasks = true;

    private int callCount;
    private int buildCount = 0;
    private int executedCount = 0;

    private Settlement owner;
    
    private transient List<SettlementTask> tasks;
    
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
     * Gets the MetaTasks that can be used for these Settlement manager.
     * By default this is the global list from MetaTaskUtil.
     * 
     * @return
     */
    protected List<SettlementMetaTask> getMetaTasks() {
        return MetaTaskUtil.getSettlementMetaTasks();
    }

    /**
     * Gets the current cached Settlement Tasks. 
     * If there is no cache or marked as refresh then a list is created.
     * 
     * @return
     */
    private List<SettlementTask> getTasks() {
        if (refreshTasks || (tasks == null)) {
            tasks = new ArrayList<>();
            for (SettlementMetaTask mt : getMetaTasks()) {
                tasks.addAll(mt.getSettlementTasks(owner));
            }
            refreshTasks = false;
            buildCount++;

            // Inform listeners
            owner.fireUnitUpdate(UnitEventType.BACKLOG_EVENT, owner);
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
    	if (tasks == null) {
    		return Collections.emptyList();
    	}
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
                RatingScore score = mt.assessRobotSuitability(st, r);
                if (score.getScore() >= 1) {
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
     * @see SettlementMetaTask#assessPersonSuitability(SettlementTask, Person)
     */
    public List<TaskJob> getTasks(Person p) {
        Set<TaskScope> acceptable = switch(p.getShiftSlot().getStatus()) {
            case OFF_DUTY, ON_LEAVE -> OFF_DUTY_SCOPES;
            case ON_CALL, ON_DUTY -> ON_DUTY_SCOPES;
        };

        List<TaskJob> result = new ArrayList<>();
        for(SettlementTask st : getTasks()) {
            // Check scope first to avoid scoring
            var scope = st.getScope();
            if (acceptable.contains(scope)) {
                SettlementMetaTask mt = st.getMeta();
                RatingScore score = mt.assessPersonSuitability(st, p);
                if (score.getScore() >= 1) {
                    result.add(new SettlementTaskProxy(this, st, score));
                }
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
