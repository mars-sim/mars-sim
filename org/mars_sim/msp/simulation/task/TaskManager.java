/**
 * Mars Simulation Project
 * TaskManager.java
 * @version 2.72 2001-07-16
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.task;

import org.mars_sim.msp.simulation.*;
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
    public TaskManager(Person person, VirtualMars mars) {
        // Initialize data members
        this.person = person;
        this.mars = mars;
        currentTask = null;

        // Create an array of general task classes.
        // (Add additional general tasks as they are created)
        try {
            generalTasks = new Class[]{ TravelToSettlement.class, Relax.class, TendGreenhouse.class, MaintainVehicle.class };
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

    /** Returns the current task.
     *  Return null if there is no current task.
     *  @return the current task
     */
    public Task getCurrentTask() {
        return currentTask;
    }

    /** Sets a new task for the person, stopping any current tasks.
     * 
     */
    public void setCurrentTask(Task newTask) {
        currentTask = newTask;
    }
   
    /** Sets the current task to null.
     *
     */
    public void clearCurrentTask() {
        currentTask = null;
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

    /** Perform a task for a given amount of time.
     *  If person has no task or the current task is done, assign a new task to him/her.
     *  @param time amount of time to perform the action
     */
    public void takeAction(double time) {
        if ((currentTask == null) || currentTask.isDone()) {
            getNewTask();
        }
        currentTask.doTask(time);
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
                double weight = ((Double) probability.invoke(null, parametersForInvokingMethod)).doubleValue();

                if (weight > 0D) {
                    probableTasks.addElement(generalTasks[x]);
                    weights.addElement(new Double(weight));
                }
            } catch (Exception e) {
                System.out.println("TaskManager.getNewTask() (1): " + e.toString());
            }
        }
        
        // Total up the weights
        double totalWeight = 0;
        for (int x = 0; x < weights.size(); x++)
            totalWeight += ((Double) weights.elementAt(x)).doubleValue();

        // Get a random number from 0 to the total weight
        double r = RandomUtil.getRandomDouble(totalWeight);
        
        // Determine which task is selected
        double tempWeight = ((Double) weights.elementAt(0)).doubleValue();
        int taskNum = 0;
        while (tempWeight < r) {
            taskNum++;
            tempWeight += ((Double) weights.elementAt(taskNum)).doubleValue();
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
