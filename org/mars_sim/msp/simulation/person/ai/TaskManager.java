/**
 * Mars Simulation Project
 * TaskManager.java
 * @version 2.74 2002-04-23
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import java.io.Serializable;
import java.util.Vector;
import java.lang.reflect.*;

/** The TaskManager class keeps track of a person's current task and can randomly
 *  assign a new task to a person based on a list of possible tasks and that person's
 *  current situation.
 *
 *  There is one instance of TaskManager per person.
 */
public class TaskManager implements Serializable {

    // Data members
    private Task currentTask; // The current task the person is doing.
    private Mind mind; // The mind of the person the task manager is responsible for.
    private Mars mars; // The virtual Mars

    // Array of available tasks
    private Class[] availableTasks = { Relax.class, TendGreenhouse.class,
                                       MaintainVehicle.class,
                                       MaintainSettlement.class,
				       MaintainEquipment.class,
                                       Sleep.class, EatMeal.class,
                                       MedicalAssistance.class,
                                       StudyRockSamples.class };

    /** Constructs a TaskManager object
     *  @param person the person the task manager is for
     *  @param mars the virtual Mars
     */
    public TaskManager(Mind mind, Mars mars) {
        // Initialize data members
        this.mind = mind;
        this.mars = mars;
        currentTask = null;
    }

    /** Returns true if person has an active task.
     *  @return true if person has an active task
     */
    public boolean hasActiveTask() {
        if ((currentTask != null) && !currentTask.isDone()) return true;
        else return false;
    }

    /** Returns true if perosn has a task (may be inactive).
     *  @return true if person has a task
     */
    public boolean hasTask() {
        if (currentTask != null) return true;
        else return false;
    }

    /** Returns the name of the current task for UI purposes.
     *  Returns a blank string if there is no current task.
     *  @return name of the current task
     */
    public String getTaskName() {
        if (currentTask != null) return currentTask.getName();
	else return "";
    }

    /** Returns a description of current task for UI purposes.
     *  Returns a blank string if there is no current task.
     *  @return a description of the current task
     */
    public String getTaskDescription() {
        if (currentTask != null) return currentTask.getDescription();
        else return "";
    }

    /** Returns the name of current task phase if there is one.
     *  Returns blank string if current task has no phase.
     *  Returns blank string if there is no current task.
     *  @return the name of the current task phase
     */
    public String getPhase() {
        if (currentTask != null) return currentTask.getPhase();
        else return "";
    }

    /** Returns the current task.
     *  Return null if there is no current task.
     *  @return the current task
     */
    public Task getTask() {
        return currentTask;
    }

    /** Sets the current task to null.
     *
     */
    public void clearTask() {
        currentTask = null;
    }

    /** Adds a task to the stack of tasks.
     *  @param Task the task to be added
     */
    void addTask(Task Task) {
        if (hasActiveTask()) currentTask.addSubTask(Task);
        else currentTask = Task;
    }

    /** Perform the current task for a given amount of time.
     *  @param time amount of time to perform the action
     *  @param efficiency The performance rating of person performance task.
     */
    public void performTask(double time, double efficiency) {
        if (currentTask != null) {
            // For effort driven task, reduce the effective time
            if (currentTask.isEffortDriven()) {
                time *= efficiency;
            }
            currentTask.performTask(time);
        }
    }

    /** Gets a new task for the person based on tasks available.
     *  @param totalProbabilityWeight the total of task probability weights
     *  @return new task
     */
    public Task getNewTask(double totalProbabilityWeight) {

        // Initialize parameters
        Class[] parametersForFindingMethod = { Person.class, Mars.class };
        Object[] parametersForInvokingMethod = { mind.getPerson(), mars };

        // Get a random number from 0 to the total weight
        double r = RandomUtil.getRandomDouble(totalProbabilityWeight);

        // Determine which task is selected.
        Class task = null;
        for (int x=0; x < availableTasks.length; x++) {
            try {
                Method probability = availableTasks[x].getMethod("getProbability", parametersForFindingMethod);
                double weight = ((Double) probability.invoke(null, parametersForInvokingMethod)).doubleValue();

                if (task == null) {
                    if (r < weight) task = availableTasks[x];
                    else r -= weight;
                }
            }
            catch (Exception e) {
                // System.out.println("TaskManager.getNewTask() (1): " + e.toString());
            }
        }

        // Construct the task
        try {
            Constructor construct = (task.getConstructor(parametersForFindingMethod));
            return (Task) construct.newInstance(parametersForInvokingMethod);
        } catch (Exception e) {
            // System.out.println("TaskManager.getNewTask() (2): " + e.toString());
            return null;
        }
    }

    /** Determines the total probability weight for available tasks.
     *  @return total probability weight
     */
    public double getTotalTaskProbability() {
        double result = 0D;

        // Initialize parameters
        Class[] parametersForFindingMethod = { Person.class, Mars.class };
        Object[] parametersForInvokingMethod = { mind.getPerson(), mars };

        // Sum the probable weights of each available task.
        for (int x = 0; x < availableTasks.length; x++) {
            try {
                Method probability = availableTasks[x].getMethod("getProbability", parametersForFindingMethod);
                result += ((Double) probability.invoke(null, parametersForInvokingMethod)).doubleValue();
            } catch (Exception e) {
                // System.out.println("TaskManager.getTotalTaskProbability(): " + e.toString());
            }
        }

        return result;
    }
}
