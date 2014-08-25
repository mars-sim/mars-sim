/**
 * Mars Simulation Project
 * TaskManager.java
 * @version 3.07 2014-08-15
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.task.meta.MetaTask;
import org.mars_sim.msp.core.person.ai.task.meta.MetaTaskUtil;
import org.mars_sim.msp.core.time.MarsClock;

/** 
 * The TaskManager class keeps track of a person's current task and can randomly
 * assign a new task to a person based on a list of possible tasks and that person's
 * current situation.
 *
 * There is one instance of TaskManager per person.
 */
public class TaskManager
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(TaskManager.class.getName());

	// Data members
	/** The current task the person is doing. */
	private Task currentTask;
	/** The mind of the person the task manager is responsible for. */
	private Mind mind;

	// Cache variables.
	private transient MarsClock timeCache;
	private transient double totalProbCache;
	private transient Map<MetaTask, Double> taskProbCache;

	/** 
	 * Constructor.
	 * @param mind the mind that uses this task manager.
	 */
	public TaskManager(Mind mind) {
        // Initialize data members
        this.mind = mind;
        currentTask = null;

        // Initialize cache values.
        timeCache = null;
        taskProbCache = new HashMap<MetaTask, Double>(MetaTaskUtil.getMetaTasks().size());
        totalProbCache = 0D;
    }

    /** Returns true if person has an active task.
     *  @return true if person has an active task
     */
    public boolean hasActiveTask() {
        return (currentTask != null) && !currentTask.isDone();
    }

    /** Returns true if person has a task (may be inactive).
     *  @return true if person has a task
     */
    public boolean hasTask() {
        return currentTask != null;
    }

    /** Returns the name of the current task for UI purposes.
     *  Returns a blank string if there is no current task.
     *  @return name of the current task
     */
    public String getTaskName() {
        if (currentTask != null) {
            return currentTask.getName();
        } else {
            return "";
        }
    }

    /** Returns a description of current task for UI purposes.
     *  Returns a blank string if there is no current task.
     *  @return a description of the current task
     */
    public String getTaskDescription() {
        if (currentTask != null) {
            return currentTask.getDescription();
        } else {
            return "";
        }
    }

    /** Returns the name of current task phase if there is one.
     *  Returns blank string if current task has no phase.
     *  Returns blank string if there is no current task.
     *  @return the name of the current task phase
     */
    public String getPhase() {
        if (currentTask != null) {
            return currentTask.getPhase();
        } else {
            return "";
        }
    }

    /** Returns the current task.
     *  Return null if there is no current task.
     *  @return the current task
     */
    public Task getTask() {
        return currentTask;
    }

    /**
     * Sets the current task to null.
     */
    public void clearTask() {
        currentTask = null;
        mind.getPerson().fireUnitUpdate(UnitEventType.TASK_EVENT);
    }

    /** Adds a task to the stack of tasks.
     *  @param newTask the task to be added
     */
    public void addTask(Task newTask) {
        if (hasActiveTask()) {
            currentTask.addSubTask(newTask);
        } else {
            currentTask = newTask;
        }
        mind.getPerson().fireUnitUpdate(UnitEventType.TASK_EVENT, newTask);
    }

    /** 
     * Perform the current task for a given amount of time.
     * @param time amount of time to perform the action
     * @param efficiency The performance rating of person performance task.
     * @return remaining time.
     * @throws Exception if error in performing task.
     */
    public double performTask(double time, double efficiency) {
        double remainingTime = 0D;

        if (currentTask != null) {
            // For effort driven task, reduce the effective time based on efficiency.
            if (efficiency < .1D) {
                efficiency = .1D;
            }
            if (currentTask.isEffortDriven()) {
                time *= efficiency;
            }
            checkForEmergency();

            remainingTime = currentTask.performTask(time);
        }

        return remainingTime;
    }

    /**
     * Checks if any emergencies are happening in the person's local.
     * Adds an emergency task if necessary.
     * @throws Exception if error checking for emergency.
     */
    private void checkForEmergency() {

        // Check for emergency malfunction.
        if (RepairEmergencyMalfunction.hasEmergencyMalfunction(mind.getPerson())) {
            boolean hasEmergencyRepair = ((currentTask != null) && (currentTask 
                    instanceof RepairEmergencyMalfunction));
            
            boolean hasAirlockTask = false;
            Task task = currentTask;
            while (task != null) {
                if ((task instanceof EnterAirlock) && (task instanceof ExitAirlock)) {
                    hasAirlockTask = true;
                }
                task = task.getSubTask();
            }

            if (!hasEmergencyRepair && !hasAirlockTask) {
                logger.fine(mind.getPerson() + " cancelling task " + currentTask + 
                        " due to emergency repairs.");
                clearTask();
                addTask(new RepairEmergencyMalfunction(mind.getPerson()));
            }
        }
    }
    
    /** 
     * Gets a new task for the person based on tasks available.
     * @return new task
     */
    public Task getNewTask() {

        Task result = null;

        // If cache is not current, calculate the probabilities.
        if (!useCache()) {
            calculateProbability();
        }
        
        // Get a random number from 0 to the total weight
        double totalProbability = getTotalTaskProbability(true);

        if (totalProbability == 0D) {
            throw new IllegalStateException(mind.getPerson() + 
                    " has zero total task probability weight.");
        }

        double r = RandomUtil.getRandomDouble(totalProbability);

        // Determine which task is selected.
        MetaTask selectedMetaTask = null;
        Iterator<MetaTask> i = taskProbCache.keySet().iterator();
        while (i.hasNext() && (selectedMetaTask == null)) {
            MetaTask metaTask = i.next();
            double probWeight = taskProbCache.get(metaTask);
            if (r <= probWeight) {
                selectedMetaTask = metaTask;
            } 
            else {
                r -= probWeight;
            }
        }

        if (selectedMetaTask == null) {
            throw new IllegalStateException(mind.getPerson() + 
                    " could not determine a new task.");
        }

        // Construct the task
        result = selectedMetaTask.constructInstance(mind.getPerson());

        // Clear time cache.
        timeCache = null;

        return result;
    }

    /** 
     * Determines the total probability weight for available tasks.
     * @return total probability weight
     */
    public double getTotalTaskProbability(boolean useCache) {

        // If cache is not current, calculate the probabilities.
        if (!useCache) {
            calculateProbability();
        }

        return totalProbCache;
    }
    
    /**
     * Calculates and caches the probabilities.
     */
    private void calculateProbability() {

        if (taskProbCache == null) {
            taskProbCache = new HashMap<MetaTask, Double>(MetaTaskUtil.getMetaTasks().size());
        }
        
        // Clear total probabilities.
        totalProbCache = 0D;

        // Determine probabilities.
        Iterator<MetaTask> i = MetaTaskUtil.getMetaTasks().iterator();
        while (i.hasNext()) {
            MetaTask metaTask = i.next();
            double probability = metaTask.getProbability(mind.getPerson());
            if ((probability >= 0D) && (!Double.isNaN(probability)) && (Double.isFinite(probability))) {
                taskProbCache.put(metaTask, probability);
                totalProbCache += probability;
            }
            else {
                taskProbCache.put(metaTask, 0D);
                logger.severe(mind.getPerson().getName() + " bad task probability: " +  metaTask.getName() + 
                        " probability: " + probability);
            }
        }

        // Set the time cache to the current time.
        timeCache = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
    }

    /**
     * Checks if task probability cache should be used.
     * @return true if cache should be used.
     */
    private boolean useCache() {
        MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
        return currentTime.equals(timeCache);
    }

    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {
        if (currentTask != null) {
            currentTask.destroy();
        }
        mind = null;
        timeCache = null;
        taskProbCache.clear();
        taskProbCache = null;
    }
}