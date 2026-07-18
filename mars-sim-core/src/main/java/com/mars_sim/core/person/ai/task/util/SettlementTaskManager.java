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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    static class SettlementTaskProxy extends AbstractTaskJob  {

		private static final long serialVersionUID = 1L;
        private SettlementTask source;

        SettlementTaskProxy(SettlementTask source, RatingScore score) {
            super(source.getName(), score);
            this.source = source;
        }

        @Override
        public Task createTask(Person person) {
            source.reduceDemand();
            return source.createTask(person);
        }

        @Override
        public Task createTask(Robot robot) {
            source.reduceDemand();
            return source.createTask(robot);
        }
    }

    private boolean tasksStale = true;

    private int callCount = 0;
    private int buildCount = 0;

    private Settlement owner;
    
    private transient Map<SettlementMetaTask, List<SettlementTask>> tasks = new HashMap<>();
    public static final String NEWTASK_EVENT = "new settlement task";
    public static final String UPDATETASK_EVENT = "update settlement task";
    public static final String REMOVETASK_EVENT = "remove settlement task";

    public SettlementTaskManager(Settlement owner) {
        this.owner = owner;
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
     */
    private Map<SettlementMetaTask, List<SettlementTask>> getLatestTasks() {
        if (tasksStale) {
            getMetaTasks().forEach(mt -> 
                calculateTasks(mt, tasks.computeIfAbsent(mt, k -> new ArrayList<>()))
            );
            tasksStale = false;
            buildCount++;
        }
        callCount++;
        return tasks;
    }

    /**
     * This is the main method to calculate the current list of SettlementTasks for a particular MetaTask.
     * It will add new tasks, update existing tasks and remove any that are no longer needed.
     * The changes trigger the correspnding entity event.
     * The existing tasks list is updated in place.
     * @param mt MetaTask to calculate the SettlementTasks for
     * @param existingTasks The current list of SettlementTasks for this MetaTask
     */
    private void calculateTasks(SettlementMetaTask mt, List<SettlementTask> existingTasks) {
        var newTasks = mt.getSettlementTasks(owner);

        // Update or add new tasks to the list
        for (SettlementTask newTask : newTasks) {
            int found = existingTasks.indexOf(newTask);
            if (found == -1) {
                existingTasks.add(newTask);
                owner.fireUnitUpdate(NEWTASK_EVENT, newTask);
            }
            else {
                var existing = existingTasks.get(found);
                if (existing.updateParameters(newTask)) {
                    // Update item
                    owner.fireUnitUpdate(UPDATETASK_EVENT, existing);
                }
            }
        }

        // Any to be removed
        List<SettlementTask> toRemove = new ArrayList<>();
        for (SettlementTask existing : existingTasks) {
            // Should not need to remove if the demand is  0, but just in case
            if (existing.getDemand() == 0 || !newTasks.contains(existing)) {
                toRemove.add(existing);
            }
        }

        if (!toRemove.isEmpty()) {
            existingTasks.removeAll(toRemove);
            toRemove.forEach(t -> owner.fireUnitUpdate(REMOVETASK_EVENT, t));
        }
    }

    /**
     * Gets the currently available SettlementTasks. This list maybe be null.
     */
    public List<SettlementTask> getAvailableTasks() {
    	if (tasks == null) {
    		return Collections.emptyList();
    	}
        return tasks.values().stream().flatMap(List::stream).toList();
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
        for(var stList : getLatestTasks().values()) {
            for(var st : stList) {
                SettlementMetaTask mt = st.getMeta();

                // Check this type of Robot can do the Job
                if (st.getDemand() > 0 && mt.getPreferredRobot().contains(r.getRobotType())) {
                    RatingScore score = mt.assessRobotSuitability(st, r);
                    if (score.getScore() > 0) {
                        result.add(new SettlementTaskProxy(st, score));
                    }
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
        for(var stList : getLatestTasks().values()) {
            for(var st : stList) {
                // Check scope first to avoid scoring
                var scope = st.getScope();
                if (st.getDemand() > 0 && acceptable.contains(scope)) {
                    SettlementMetaTask mt = st.getMeta();
                    RatingScore score = mt.assessPersonSuitability(st, p);
                    if (score.getScore() > 0) {
                        result.add(new SettlementTaskProxy(st, score));
                    }
                }
            }
        }
        return result;
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
        tasksStale = true;
    }

    /**
     * Add teh transient task map
     * @param in
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        tasks = new HashMap<>();
        tasksStale = true;
    }

    /**
     * Remove a task from the active list.
     * @param st Task to remove
     */
    void removeTask(SettlementTask st) {
        var meta = st.getMeta();
        var existing = tasks.get(meta);
        if (existing != null) {
            existing.remove(st);
            owner.fireUnitUpdate(REMOVETASK_EVENT, st);
        }
    }
}
