/**
 * Mars Simulation Project
 * TaskManager.java
 * @version 2.71 2000-10-18
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.util.Vector;
import java.lang.reflect.*;

/** The TaskManager class keeps track of a person's current task and can randomly
 *  assign a new task to a person based on a list of possible tasks and that person's
 *  current situation.
 *
 *  There is one instance of TaskManager per person.
 */
public class TaskManager {

    // Data members
    private Task currentTask; // The current task the person is doing.
    private Person person; // The person the task manager is responsible for.
    private VirtualMars mars; // The virtual Mars
    private Class[] generalTasks; // A collection of general tasks a person can do.

    /** Constructs a TaskManager object
     *  @param person the person the task manager is for
     *  @param mars the virtual Mars
     */
    TaskManager(Person person, VirtualMars mars) {
        // Initialize data members
        this.person = person;
        this.mars = mars;
        currentTask = null;

        // Create an array of general task classes.
        // (Add additional general tasks as they are created)
        try {
            generalTasks = new Class[]{ TaskRelax.class, TaskDrive.class, TaskTendGreenhouse.class,
            TaskMechanic.class };
        } catch (Exception e) {
            System.out.println("TaskManager.constructor(): " + e.toString());
        }
    }

    /** Returns true if person has a current task. 
     *  @return true if person has a current task
     */
    public boolean hasCurrentTask() {
        if (currentTask != null)
            return true;
        else
            return false;
    }

    /** Returns a description of current task for UI purposes.
     *  Returns null if there is no current task.
     *  @return a description of the current task
     */
    public String getCurrentTaskDescription() {
        if (currentTask != null)
            return currentTask.getDescription();
        else
            return null;
    }

    /** Returns the name of current task phase if there is one.
      *  Returns black string if current task has no phase.
      *  Returns null if there is no current task.
      *  @return the name of the current task phase
      */
    public String getCurrentPhase() {
        if (currentTask != null)
            return currentTask.getPhase();
        else
            return null;
    }

    /** Returns the name of current task sub-phase if there is one.
      *  Returns black string if current task has no sub-phase.
      *  Returns null if there is no current task.
      *  @return the name of the current task sub-phase
      */
    public String getCurrentSubPhase() {
        if (currentTask != null)
            return currentTask.getSubPhase();
        else
            return null;
    }

    /** Returns the current task.
     *  Return null if there is no current task.
     *  @return the current task
     */
    public Task getCurrentTask() {
        return currentTask;
    }

    /** Adds a sub-task to the stack of tasks. 
     *  @param subTask the sub-task to be added
     */
    void addSubTask(Task subTask) {
        if (currentTask != null)
            currentTask.addSubTask(subTask);
        else
            currentTask = subTask;
    }

    /** Perform a task for a given number of seconds.
     *  If person has no task or the current task is done, assign a new task to him/her.
     *  @param seconds the amount of time to perform a task (in seconds)
     */
    void takeAction(int seconds) {
        if ((currentTask == null) || currentTask.isDone()) {
            getNewTask();
        }
        currentTask.doTask(seconds);
    }

    /** Assigns a new task to a person based on general tasks available.
     *  (Add support for role-based tasks later)
     */
    public void getNewTask() {
        // Initialize variables
        Vector probableTasks = new Vector();
        Vector weights = new Vector();
        Class[] parametersForFindingMethod = { Person.class, VirtualMars.class };
        Object[] parametersForInvokingMethod = { person, mars };
        
        // Find the probable weights of each possible general task.
        for (int x = 0; x < generalTasks.length; x++) {
            try {
                Method probability =
                        generalTasks[x].getMethod("getProbability", parametersForFindingMethod);
                int weight = ((Integer) probability.invoke(null, parametersForInvokingMethod)).intValue();

                if (weight > 0) {
                    probableTasks.addElement(generalTasks[x]);
                    weights.addElement(new Integer(weight));
                }
            } catch (Exception e) {
                System.out.println("TaskManager.getNewTask() (1): " + e.toString());
            }
        }
        
        // Total up the weights
        int totalWeight = 0;
        for (int x = 0; x < weights.size(); x++)
            totalWeight += ((Integer) weights.elementAt(x)).intValue();

        // Get a random number from 0 to the total weight
        int r = (int) Math.round(Math.random() * (double) totalWeight);
        
        // Determine which task is selected
        int tempWeight = ((Integer) weights.elementAt(0)).intValue();
        int taskNum = 0;
        while (tempWeight < r) {
            taskNum++;
            tempWeight += ((Integer) weights.elementAt(taskNum)).intValue();
        }
        
        // Create an instance of that task and set it as the current task
        try {
            Constructor construct = ((Class) probableTasks.elementAt(taskNum)).getConstructor(
                    parametersForFindingMethod);
            currentTask = (Task) construct.newInstance(parametersForInvokingMethod);
        } catch (Exception e) {
            System.out.println("TaskManager.getNewTask() (2): " + e.toString());
        }
        
    }
}
