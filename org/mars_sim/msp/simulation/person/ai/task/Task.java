/**
 * Mars Simulation Project
 * Task.java
 * @version 2.78 2005-08-14
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.person.ai.social.RelationshipManager;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.LifeSupport;

/** 
 * The Task class is an abstract parent class for tasks that allow people to do various things.
 * A person's TaskManager keeps track of one current task for the person, but a task may use other
 * tasks internally to accomplish things.
 */
public abstract class Task implements Serializable, Comparable {
	
	// Unit event types
	public static final String TASK_NAME_EVENT = "task name";
	public static final String TASK_DESC_EVENT = "task name";
	public static final String TASK_PHASE_EVENT = "task name";
	public static final String TASK_ENDED_EVENT = "task ended";
	public static final String TASK_SUBTASK_EVENT = "subtask";
	
	private static final double JOB_STRESS_MODIFIER = .5D;
	private static final double SKILL_STRESS_MODIFIER = .1D;

    // Data members
    private String name;            // The name of the task
    protected Person person;          // The person performing the task.
    private boolean done;             // True if task is finished
    protected boolean hasDuration;    // True if task has a time duration.
    private double duration;        // The time duration (in millisols) of the task.
    private double timeCompleted;     // The current amount of time spent on the task (in millisols)
    private String description;     // Description of the task
    protected Task subTask;           // Sub-task of the current task
    private String phase;             // Phase of task completion
    protected double phaseTimeRequired;  // Amount of time required to complete current phase. (in millisols)
    protected double phaseTimeCompleted; // Amount of time completed on the current phase. (in millisols)
    protected boolean effortDriven;     // Is this task effort driven
    private boolean createEvents;       // Task should create Historical events
    protected double stressModifier;  // Stress modified by person performing task per millisol.
    private Person teacher;           // The person teaching this task if any.
    private Collection phases;        // A collection of the task's phases.

    /** 
     * Constructs a Task object.
     * @param name the name of the task
     * @param person the person performing the task
     * @param effort Does this task require physical effort
     * @param createEvents Does this task create events?
     * @param stressModifier stress modified by person performing task per millisol.
     * @param hasDuration Does the task have a time duration?
     * @param duration the time duration (in millisols) of the task (or 0 if none)
     * @throws Exception if task could not be constructed.
     */
    public Task(String name, Person person, boolean effort, boolean createEvents, 
    		double stressModifier, boolean hasDuration, double duration) throws Exception {
        this.name = name;
        this.person = person;
		this.createEvents = createEvents;
		this.stressModifier = stressModifier;
		this.hasDuration = hasDuration;
		this.duration = duration;

        done = false;
        
        timeCompleted = 0D;
        description = name;
        subTask = null;
        phase = null;
        effortDriven = effort;
        phases = new ArrayList();
    }
    
    /**
     * Ends the task and performs any final actions.
     */
    public void endTask() {
        done = true;
        person.fireUnitUpdate(TASK_ENDED_EVENT);
        
        // Create ending task historical event if needed.
        if (getCreateEvents()) {
        	TaskEvent endingEvent = new TaskEvent(person, this, TaskEvent.FINISH, "");
			Simulation.instance().getEventManager().registerNewEvent(endingEvent);
        }
    }

    /**
     * Return the value of the effort driven flag.
     * @return Effort driven.
     */
    public boolean isEffortDriven() {
        return effortDriven;
    }

    /** Returns the name of the task.
     *  @return the task's name
     */
    public String getName() {
        if ((subTask != null) && !subTask.isDone()) return subTask.getName();
        else return name;
    }
    
    /**
     * Sets the task's name.
     * @param name the task name.
     */
    protected void setName(String name) {
    	this.name = name;
    	person.fireUnitUpdate(TASK_NAME_EVENT);
    }

    /** Returns a string that is a description of what the task is currently doing.
     *  This is mainly for user interface purposes.
     *  Derived tasks should extend this if necessary.
     *  Defaults to just the name of the task.
     *  @return the description of what the task is currently doing
     */
    public String getDescription() {
        if ((subTask != null) && !subTask.isDone()) return subTask.getDescription();
        else return description;
    }
    
    /**
     * Sets the task's description.
     * @param description the task description.
     */
    protected void setDescription(String description) {
    	this.description = description;
    	person.fireUnitUpdate(TASK_DESC_EVENT);
    }

    /** Returns a boolean whether this task should generate events
     *  @return boolean flag.
     */
    public boolean getCreateEvents() {
        return createEvents;
    }

    /** 
     * Gets a string of the current phase of the task.
     * @return the current phase of the task
     */
    public String getPhase() {
        if ((subTask != null) && !subTask.isDone()) return subTask.getPhase();
        return phase;
    }
    
    /**
     * Gets a string of the current phase of this task, ignoring subtasks.
     * @return the current phase of this task.
     */
    public String getTopPhase() {
    	return phase;
    }
    
    /**
     * Sets the task's current phase.
     * @param newPhase the phase to set the a task at.
     * @throws Exception if newPhase is not in the task's collection of phases.
     */
    protected void setPhase(String newPhase) throws Exception {
    	if (newPhase == null) throw new IllegalArgumentException("newPhase is null");
    	else if (phases.contains(newPhase)) {
    		phase = newPhase;
    		person.fireUnitUpdate(TASK_PHASE_EVENT);
    	}
    	else throw new Exception("newPhase: " + newPhase + " is not a valid phase for this task.");
    }
    
    /**
     * Adds a phase to the task's collection of phases.
     * @param newPhase the new phase to add.
     */
    protected void addPhase(String newPhase) {
    	if (newPhase == null) throw new IllegalArgumentException("newPhase is null");
    	else if (!phases.contains(newPhase)) phases.add(newPhase);
    }

    /** Determines if task is still active.
     *  @return true if task is completed
     */
    public boolean isDone() {
        return done;
    }

    /** Adds a new sub-task.
     *  @param newSubTask the new sub-task to be added
     */
    void addSubTask(Task newSubTask) {
        if (subTask != null) subTask.addSubTask(newSubTask);
        else {
        	subTask = newSubTask;
        	person.fireUnitUpdate(TASK_SUBTASK_EVENT);
        }
    }

    /**
     * Gets the task's subtask.
     * Returns null if none
     * @return subtask
     */
    public Task getSubTask() {
        return subTask;
    }

    /** Returns the weighted probability that a person might perform this task.
     *  It should return a 0 if there is no chance to perform this task given the person and the situation.
     *  @param person the person to perform the task
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) { return 0D; }

    /** 
     * Perform the task for the given number of seconds.
     * Children should override and implement this.
     * @param time amount of time (millisol) given to perform the task (in microsols)
     * @return amount of time (millisol) remaining after performing the task (in microsols)
     * @throws Exception if error peforming task.
     */
    double performTask(double time) throws Exception {
        double timeLeft = time;
        if (subTask != null) {
            if (subTask.isDone()) subTask = null;
            else timeLeft = subTask.performTask(timeLeft);
        }
        
        // If no subtask, perform this task.
        if ((subTask == null) || subTask.isDone()) {
            // If task is effort-driven and person is incapacitated, end task.
            if (effortDriven && (person.getPerformanceRating() == 0D)) endTask();
            
            // Perform phases of task until time is up or task is done.
            while ((timeLeft > 0D) && !isDone()) timeLeft = performMappedPhase(timeLeft);
            
            // Keep track of the duration of the task if necesary.
            timeCompleted += time;
            if (hasDuration && (timeCompleted >= duration)) {
            	timeLeft = timeCompleted - duration;
            	endTask();
            }
        }
        
        // Modify stress performing task.
        modifyStress(timeLeft);
        
        return timeLeft;
    }

    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time (millisol) the phase is to be performed.
     * @return the remaining time (millisol) after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected abstract double performMappedPhase(double time) throws Exception;
    
    /**
     * SHould the start of this task create an historical event.
     * @param create New flag value.
     */
    protected void setCreateEvents(boolean create) {
        createEvents = create;
    }

    /**
     * Get a string representation of this Task. It's content will consist
     * of the description.
     *
     * @return Description of the task.
     */
    public String toString() {
        return description;
    }

    /**
     * Compare this object to another for an ordering. THe ordering is based
     * on the alphabetic ordering of the Name attribute.
     *
     * @param other Object to compare against.
     * @return integer comparasion of the two objects.
     * @throws ClassCastException if the object in not of a Task.
     */
    public int compareTo(Object other) {
        return name.compareTo(((Task)other).name);
    }
    
    /**
     * Modify stress from performing task for given time.
     * @param time the time performing the task.
     */
    private void modifyStress(double time) {
    	PhysicalCondition condition = person.getPhysicalCondition();
    	
    	double effectiveStressModifier = stressModifier;
    	
    	if (stressModifier > 0D) {
    		
    		// Reduce stress modifier if task is in person's current job description.
    		Job job = person.getMind().getJob();
    		if ((job != null) && job.isJobRelatedTask(this.getClass())) {
    			// System.out.println("Job: " + job.getName() + " related to " + this.getName() + " task");
    			effectiveStressModifier*= JOB_STRESS_MODIFIER;
    		}
    		
    		// Reduce stress modifier for person's skill related to the task.
    		int skill = this.getEffectiveSkillLevel();
    		effectiveStressModifier-= (effectiveStressModifier * (double) skill * SKILL_STRESS_MODIFIER);
    		
    		// If effective stress modifier < 0, set it to 0.
    		if (effectiveStressModifier < 0D) effectiveStressModifier = 0D;
    	}
    	
    	condition.setStress(condition.getStress() + (effectiveStressModifier * time));
    }
    
    /**
     * Set the task's stress modifier.
     * Stress modifier can be positive (increase in stress) or negative (decrease in stress).
     * @param newStressModifier stress modification per millisol.
     */
    protected void setStressModifier(double newStressModifier) {
    	this.stressModifier = newStressModifier;
    }
    
    /**
     * Gets the probability modifier for a task if person needs to go to a new building.
     * @param person the person to perform the task.
     * @param newBuilding the building the person is to go to.
     * @return probability modifier
     * @throws BuildingException if current or new building doesn't have life support function.
     */
	protected static double getCrowdingProbabilityModifier(Person person, Building newBuilding) 
			throws BuildingException {
		double modifier = 1D;
		
		Building currentBuilding = BuildingManager.getBuilding(person);
		if ((currentBuilding != null) && (newBuilding != null) && (currentBuilding != newBuilding)) {
			
			// Increase probability if current building is overcrowded.
			LifeSupport currentLS = (LifeSupport) currentBuilding.getFunction(LifeSupport.NAME);
			int currentOverCrowding = currentLS.getOccupantNumber() - currentLS.getOccupantCapacity();
			if (currentOverCrowding > 0) modifier *= ((double) currentOverCrowding + 2);
			
			// Decrease probability if new building is overcrowded.
			LifeSupport newLS = (LifeSupport) newBuilding.getFunction(LifeSupport.NAME);
			int newOverCrowding = newLS.getOccupantNumber() - newLS.getOccupantCapacity();
			if (newOverCrowding > 0) modifier /= ((double) newOverCrowding + 2);
		}
		
		return modifier;
	}
	
	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public abstract int getEffectiveSkillLevel();
	
	/**
	 * Gets a list of the skills associated with this task.
	 * May be empty list if no associated skills.
	 * @return list of skills as strings
	 */
	public abstract List getAssociatedSkills();
	
	/**
	 * Checks if someone is teaching this task to the person performing it.
	 * @return true if teacher.
	 */
	public boolean hasTeacher() {
		return (teacher != null);
	}
	
	/**
	 * Gets the person teaching this task.
	 * @return teacher or null if none.
	 */
	public Person getTeacher() {
		return teacher;
	}
	
	/**
	 * Sets the person teaching this task.
	 * @param newTeacher the new teacher.
	 */
	public void setTeacher(Person newTeacher) {
		this.teacher = newTeacher;
	}
	
	/**
	 * Gets the experience modifier when being taught by a teacher.
	 * @return modifier;
	 */
	protected double getTeachingExperienceModifier() {
		double result = 1D;
		
		if (hasTeacher()) {
			int teachingModifier = teacher.getNaturalAttributeManager().getAttribute(NaturalAttributeManager.TEACHING);
			int learningModifier = person.getNaturalAttributeManager().getAttribute(NaturalAttributeManager.ACADEMIC_APTITUDE);
			result+= (double) (teachingModifier + learningModifier) / 100D;
		}
		
		return result;
	}
	
	/**
	 * Gets the probability modifier for a person performing a task based on his/her 
	 * relationships with the people in the room the task is to be performed in.
	 * @param person the person to check for.
	 * @param building the building the person will need to be in for the task.
	 * @return probability modifier
	 */
	protected static double getRelationshipModifier(Person person, Building building) throws BuildingException {
		double result = 1D;
		
		RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
		
		if ((person == null) || (building == null)) throw new IllegalArgumentException("Task.getRelationshipModifier(): null parameter.");
		else {
			if (building.hasFunction(LifeSupport.NAME)) {
				LifeSupport lifeSupport = (LifeSupport) building.getFunction(LifeSupport.NAME);
				double totalOpinion = 0D;
				PersonIterator i = lifeSupport.getOccupants().iterator();
				while (i.hasNext()) {
					Person occupant = i.next();
					if (person != occupant) totalOpinion+= ((relationshipManager.getOpinionOfPerson(person, occupant) - 50D) / 50D);
				}
				
				if (totalOpinion >= 0D) result*= (1D + totalOpinion);
				else result/= (1D - totalOpinion); 
			}
		}
		
		return result;
	}
	
	/**
	 * Adds experience to the person's skills used in this task.
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected abstract void addExperience(double time);
	
	/**
	 * Gets the duration of the task or 0 if none.
	 * @return duration (millisol)
	 */
	protected double getDuration() {
		return duration;
	}
	
	/**
	 * Sets the duration of the task
	 * @param newDuration the new duration (millisol)
	 */
	protected void setDuration(double newDuration) {
		if (newDuration < 0D) throw new IllegalArgumentException("newDuration less than 0");
		this.duration = newDuration;
	}
	
	/**
	 * Gets the amount of time the task has completed.
	 * @return time (in millisols)
	 */
	protected double getTimeCompleted() {
		return timeCompleted;
	}
}