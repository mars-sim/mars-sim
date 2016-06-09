/**
 * Mars Simulation Project
 * Task.java
 * @version 3.08 2015-06-15
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.NaturalAttribute;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RoboticAttribute;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * The Task class is an abstract parent class for tasks that allow people to do various things.
 * A person's TaskManager keeps track of one current task for the person, but a task may use other
 * tasks internally to accomplish things.
 */
public abstract class Task
implements Serializable, Comparable<Task> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
    private static Logger logger = Logger.getLogger(Task.class.getName());

	private static final double JOB_STRESS_MODIFIER = .5D;
	private static final double SKILL_STRESS_MODIFIER = .1D;

	// Data members
	/** The name of the task. */
	private String name;
	/** The person performing the task. */
	protected Person person = null;
	/** The robot performing the task. */
	protected Robot robot = null;

	/** True if task is finished. */
	private boolean done;
	/** True if task has a time duration. */
	protected boolean hasDuration;
	/** The time duration (in millisols) of the task. */
	private double duration;
	/** The current amount of time spent on the task (in millisols). */
	private double timeCompleted;
	/** Description of the task. */
	private String description;
	/** Sub-task of the current task. */
	protected Task subTask;
	/** Phase of task completion. */
	private TaskPhase phase;
	/** Amount of time required to complete current phase. (in millisols) */
	protected double phaseTimeRequired;
	/** Amount of time completed on the current phase. (in millisols) */
	protected double phaseTimeCompleted;
	/** Is this task effort driven. */
	protected boolean effortDriven;
	/** Task should create Historical events. */
	private boolean createEvents;
	/** Stress modified by person performing task per millisol. */
	protected double stressModifier;
	/** The person teaching this task if any. */
	private Person teacher;
	/** A collection of the task's phases. */
	private Collection<TaskPhase> phases;

	/**
	 * Constructs a Task object.
	 * @param name the name of the task
	 * @param person the person performing the task
	 * @param effort Does this task require physical effort
	 * @param createEvents Does this task create events?
	 * @param stressModifier stress modified by person performing task per millisol.
	 * @param hasDuration Does the task have a time duration?
	 * @param duration the time duration (in millisols) of the task (or 0 if none)
	 */
	public Task(
		String name, Unit unit, boolean effort, boolean createEvents,
		double stressModifier, boolean hasDuration, double duration) {

		this.name = name;

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
		phases = new ArrayList<TaskPhase>();

        Person person = null;
        Robot robot = null;

        if (unit instanceof Person) {
         	person = (Person) unit;
         	this.person = person;
        }
        else if (unit instanceof Robot) {
        	robot = (Robot) unit;
        	this.robot = robot;
        }
	}

    /**
     * Ends the task and performs any final actions.
     */
    public void endTask() {

        // End subtask.
        if ((getSubTask() != null) && (!getSubTask().isDone())) {
            getSubTask().endTask();
        }

        done = true;

		if (person != null) {
	        person.fireUnitUpdate(UnitEventType.TASK_ENDED_EVENT, this);
		}
		else if (robot != null) {
	        robot.fireUnitUpdate(UnitEventType.TASK_ENDED_EVENT, this);
		}


        // Create ending task historical event if needed.
        if (createEvents) {

        	TaskEvent endingEvent = null;

			if (person != null) {
	            endingEvent = new TaskEvent(person, this, EventType.TASK_FINISH, "");
			}
			else if (robot != null) {
	            endingEvent = new TaskEvent(robot, this, EventType.TASK_FINISH, "");
			}

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
        return getName(true);
    }

    /**
     * Gets the name of the task.
     * @param allowSubtask true if subtask name should be used.
     * @return the task's name.
     */
    public String getName(boolean allowSubtask) {
        if (allowSubtask && (subTask != null) && !subTask.done) {
            return subTask.getName();
        }
        else {
            return name;
        }
    }


    /**
     * Gets the task name
     * @return the task's name in String.
     */
    public String getTaskName() {
        return this.getClass().getSimpleName();

    }

    /**
     * Sets the task's name.
     * @param name the task name.
     */
    protected void setName(String name) {
        this.name = name;
        if (person != null) {
            person.fireUnitUpdate(UnitEventType.TASK_NAME_EVENT, name);
        }
        else if (robot != null) {
            robot.fireUnitUpdate(UnitEventType.TASK_NAME_EVENT, name);
        }
    }

    /** Returns a string that is a description of what the task is currently doing.
     *  This is mainly for user interface purposes.
     *  Derived tasks should extend this if necessary.
     *  Defaults to just the name of the task.
     *  @return the description of what the task is currently doing
     */
    public String getDescription() {
        if ((subTask != null) && !subTask.done) {
            return subTask.getDescription();
        }
        else {
            return description;
        }
    }

    /**
     * Gets the description of the task.
     * @param allowSubtask true if subtask description should be used.
     * @return the task description.
     */
    public String getDescription(boolean allowSubtask) {
        if (allowSubtask && (subTask != null) && !subTask.done) {
            return subTask.getDescription();
        }
        else {
            return description;
        }
    }

    /**
     * Sets the task's description.
     * @param description the task description.
     */
    protected void setDescription(String description) {
        if (!this.description.equals(description)) {
            this.description = description;
            if (person != null) {
                person.fireUnitUpdate(UnitEventType.TASK_DESC_EVENT, description);
            }
            else if (robot != null) {
                robot.fireUnitUpdate(UnitEventType.TASK_DESC_EVENT, description);
            }
        }
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
    public TaskPhase getPhase() {
        if ((subTask != null) && !subTask.done) {
            return subTask.getPhase();
        }
        return phase;
    }

    /**
     * Gets a string of the current phase of this task, ignoring subtasks.
     * @return the current phase of this task.
     */
    public TaskPhase getTopPhase() {
        return phase;
    }

    /**
     * Sets the task's current phase.
     * @param newPhase the phase to set the a task at.
     * @throws Exception if newPhase is not in the task's collection of phases.
     */
    protected void setPhase(TaskPhase newPhase) {
        if (newPhase == null) {
            throw new IllegalArgumentException("newPhase is null");
        }
        else if (phases.contains(newPhase)) {
            phase = newPhase;
			if (person != null) {
	            person.fireUnitUpdate(UnitEventType.TASK_PHASE_EVENT, newPhase);
			}
			else if (robot != null) {
	            robot.fireUnitUpdate(UnitEventType.TASK_PHASE_EVENT, newPhase);
			}

        }
        else {
            throw new IllegalStateException("newPhase: " + newPhase +
                    " is not a valid phase for this task.");
        }
    }

    /**
     * Adds a phase to the task's collection of phases.
     * @param newPhase the new phase to add.
     */
    protected void addPhase(TaskPhase newPhase) {
        if (newPhase == null) {
            throw new IllegalArgumentException("newPhase is null");
        }
        else if (!phases.contains(newPhase)) {
            phases.add(newPhase);
        }
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
        if (subTask != null) {
            if (subTask.done) {
                subTask.destroy();
                subTask = newSubTask;
				if (person != null) {
	                person.fireUnitUpdate(UnitEventType.TASK_SUBTASK_EVENT, newSubTask);
				}
				else if (robot != null) {
					robot.fireUnitUpdate(UnitEventType.TASK_SUBTASK_EVENT, newSubTask);
				}

            }
            else {
                subTask.addSubTask(newSubTask);
            }
        }
        else {
            subTask = newSubTask;
			if (person != null) {
	            person.fireUnitUpdate(UnitEventType.TASK_SUBTASK_EVENT, newSubTask);
			}
			else if (robot != null) {
	            robot.fireUnitUpdate(UnitEventType.TASK_SUBTASK_EVENT, newSubTask);
			}
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

    /**
     * Perform the task for the given number of seconds.
     * Children should override and implement this.
     * @param time amount of time (millisol) given to perform the task (in millisols)
     * @return amount of time (millisol) remaining after performing the task (in millisols)
     * @throws Exception if error performing task.
     */
    double performTask(double time) {
        double timeLeft = time;
        if (subTask != null) {
            if (subTask.isDone()) {
                subTask.destroy();
                subTask = null;
            }
            else {
                timeLeft = subTask.performTask(timeLeft);
            }
        }

        // If no subtask, perform this task.
        if ((subTask == null) || subTask.isDone()) {

			if (person != null) {

	        	// If task is effort-driven and person is incapacitated, end task.
			    if (effortDriven && (person.getPerformanceRating() == 0D)) {
			    	endTask();

	            } else {

	                // Perform phases of task until time is up or task is done.
	                while ((timeLeft > 0D) && !isDone() && ((subTask == null) || subTask.isDone())) {
	                    if (hasDuration) {

	                        // Keep track of the duration of the task.
	                        if ((timeCompleted + timeLeft) >= duration) {
	                            double performTime = duration - timeCompleted;
	                            double extraTime = timeCompleted + timeLeft - duration;
	                            timeLeft = performMappedPhase(performTime) + extraTime;
	                            timeCompleted = duration;
	                            endTask();
	                        }
	                        else {
	                            double remainingTime = timeLeft;
	                            timeLeft = performMappedPhase(timeLeft);
	                            timeCompleted += remainingTime;
	                        }
	                    } else {
	                        timeLeft = performMappedPhase(timeLeft);
	                    }
	                }
	            }
	        }

			else if (robot != null) {

	        	// If task is effort-driven and person is incapacitated, end task.
			    if (effortDriven && (robot.getPerformanceRating() == 0D)) {
			    	endTask();

	            } else {

	                // Perform phases of task until time is up or task is done.
	                while ((timeLeft > 0D) && !done && ((subTask == null) || subTask.done)) {
	                    if (hasDuration) {

	                        // Keep track of the duration of the task.
	                        if ((timeCompleted + timeLeft) >= duration) {
	                            double performTime = duration - timeCompleted;
	                            double extraTime = timeCompleted + timeLeft - duration;
	                            timeLeft = performMappedPhase(performTime) + extraTime;
	                            timeCompleted = duration;
	                            endTask();
	                        }
	                        else {
	                            double remainingTime = timeLeft;
	                            timeLeft = performMappedPhase(timeLeft);
	                            timeCompleted += remainingTime;
	                        }
	                    } else {
	                        timeLeft = performMappedPhase(timeLeft);
	                    }
	                }
	            }
			}
        }

		if (person != null)
			// Modify stress performing task.
			modifyStress(time - timeLeft);

        return timeLeft;
    }

    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time (millisol) the phase is to be performed.
     * @return the remaining time (millisol) after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected abstract double performMappedPhase(double time);

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
     * @return integer comparison of the two objects.
     * @throws ClassCastException if the object in not of a Task.
     */
    public int compareTo(Task other) {
        return name.compareTo(other.name);
    }

    /**
     * Modify stress from performing task for given time.
     * @param time the time performing the task.
     */
    private void modifyStress(double time) {

    	 PhysicalCondition condition = person.getPhysicalCondition();

		if (person != null) {
	        double effectiveStressModifier = stressModifier;

	        if (stressModifier > 0D) {

	        	Job job = person.getMind().getJob();


		            if ((job != null) && job.isJobRelatedTask(this.getClass())) {
		                // logger.info("Job: " + job.getName() + " related to " + this.getName() + " task");
		                effectiveStressModifier*= JOB_STRESS_MODIFIER;
		            }

		            // Reduce stress modifier for person's skill related to the task.
		            int skill = this.getEffectiveSkillLevel();
		            effectiveStressModifier-= (effectiveStressModifier * (double) skill * SKILL_STRESS_MODIFIER);

		            // If effective stress modifier < 0, set it to 0.
		            if (effectiveStressModifier < 0D) {
		                effectiveStressModifier = 0D;
		            }

	        }

	        condition.setStress(condition.getStress() + (effectiveStressModifier * time));
	    }
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
    protected static double getCrowdingProbabilityModifier(Person person, Building newBuilding) {
        double modifier = 1D;

	        Building currentBuilding = BuildingManager.getBuilding(person);

	        if ((currentBuilding != null) && (newBuilding != null) && (currentBuilding != newBuilding)) {

	            // Increase probability if current building is overcrowded.
	            LifeSupport currentLS = (LifeSupport) currentBuilding.getFunction(BuildingFunction.LIFE_SUPPORT);
	            int currentOverCrowding = currentLS.getOccupantNumber() - currentLS.getOccupantCapacity();
	            if (currentOverCrowding > 0) {
	                modifier *= ((double) currentOverCrowding + 2);
	            }

	            // Decrease probability if new building is overcrowded.
	            LifeSupport newLS = (LifeSupport) newBuilding.getFunction(BuildingFunction.LIFE_SUPPORT);
	            int newOverCrowding = newLS.getOccupantNumber() - newLS.getOccupantCapacity();
	            if (newOverCrowding > 0) {
	                modifier /= ((double) newOverCrowding + 2);
	            }
	        }

        return modifier;
    }

    protected static double getCrowdingProbabilityModifier(Robot robot, Building newBuilding) {
        double modifier = 1D;

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
     * @return list of skills
     */
    public abstract List<SkillType> getAssociatedSkills();

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
            int teachingModifier = teacher.getNaturalAttributeManager().getAttribute(NaturalAttribute.TEACHING);
            int learningModifier = 0;
            if (person != null) {
                learningModifier = person.getNaturalAttributeManager().getAttribute(NaturalAttribute.ACADEMIC_APTITUDE);
			}
			else if (robot != null) {
	            learningModifier = 0;//robot.getRoboticAttributeManager().getAttribute(RoboticAttribute.ACADEMIC_APTITUDE);
			}

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
    protected static double getRelationshipModifier(Person person, Building building) {
        double result = 1D;

        RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();

        if ((person == null) || (building == null)) {
            throw new IllegalArgumentException("Task.getRelationshipModifier(): null parameter.");
        }
        else {
            if (building.hasFunction(BuildingFunction.LIFE_SUPPORT)) {
                LifeSupport lifeSupport = (LifeSupport) building.getFunction(BuildingFunction.LIFE_SUPPORT);
                double totalOpinion = 0D;
                Iterator<Person> i = lifeSupport.getOccupants().iterator();
                while (i.hasNext()) {
                    Person occupant = i.next();
                    if (person != occupant) {
                        totalOpinion+= ((relationshipManager.getOpinionOfPerson(person, occupant) - 50D) / 50D);
                    }
                }

                if (totalOpinion >= 0D) {
                    result*= (1D + totalOpinion);
                }
                else {
                    result/= (1D - totalOpinion);
                }
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
        if (newDuration < 0D) {
            throw new IllegalArgumentException("newDuration less than 0");
        }
        this.duration = newDuration;
    }

    /**
     * Gets the amount of time the task has completed.
     * @return time (in millisols)
     */
    protected double getTimeCompleted() {
        return timeCompleted;
    }

    /**
     * Gets the related building function for this task.
     * Override as necessary.
     * @return building function or null if none.
     */
    protected BuildingFunction getRelatedBuildingFunction() {
        return null;
    }

    protected BuildingFunction getRelatedBuildingRoboticFunction() {
        return null;
    }

    /**
     * Walk to an available activity spot in a building.
     * @param building the destination building.
     * @param allowFail true if walking is allowed to fail.
     */
    protected void walkToActivitySpotInBuilding(Building building, boolean allowFail) {
    	BuildingFunction functionType = null;

		if (person != null)
	        functionType = getRelatedBuildingFunction();
		else if (robot != null)
			functionType = getRelatedBuildingRoboticFunction();

        if ((functionType != null) && (building.hasFunction(functionType))) {
            walkToActivitySpotInBuilding(building, functionType, allowFail);
        }
        else {
            // If no available activity spot, go to random location in building.
            walkToRandomLocInBuilding(building, allowFail);
        }
    }

    /**
     * Walks to the bed assigned for this person
     * @param accommodations
     * @param person
     * @param allowFail
     */
    // 2015-01-09 Added walkToBed()
    protected void walkToBed(LivingAccommodations accommodations, Person person, boolean allowFail) { // Building quarters,
    	Point2D bed = person.getBed();
    	Building building = accommodations.getBuilding();
    	Point2D spot = LocalAreaUtil.getLocalRelativeLocation(
                bed.getX(), bed.getY(), building);
    	
        if (bed != null) {
        	//System.out.println("Task : " + person + "'s bed is at (" + bed.getX() + ", " + bed.getY() + ") in " + building.getNickName());
            // Create subtask for walking to destination.
            createWalkingSubtask(building, spot, allowFail);
        }
        else {
        	System.out.println("Task : walkToBed() : " + person + " has no designated bed in " + building.getNickName());
        	// If no available activity spot, go to random location in building.
            walkToRandomLocInBuilding(building, allowFail);
        }
    }
    
    /**
     * Walk to an available activity spot in a building.
     * @param building the destination building.
     * @param functionType the building function type for the activity.
     * @param allowFail true if walking is allowed to fail.
     */
    protected void walkToActivitySpotInBuilding(Building building, BuildingFunction functionType,
            boolean allowFail) {

        Function buildingFunction = building.getFunction(functionType);
        Point2D settlementLoc = null;
		if (person != null) {
	        // Find available activity spot in building.
	        settlementLoc = buildingFunction.getAvailableActivitySpot(person);
		}
		else if (robot != null) {
	        // Find available activity spot in building.
	        settlementLoc = buildingFunction.getAvailableActivitySpot(robot);
		}

        if (settlementLoc != null) {

            // Create subtask for walking to destination.
            createWalkingSubtask(building, settlementLoc, allowFail);
        }
        else {

            // If no available activity spot, go to random location in building.
            walkToRandomLocInBuilding(building, allowFail);
        }
    }

    /**
     * Walk to a random interior location in a building.
     * @param building the destination building.
     * @param allowFail true if walking is allowed to fail.
     */
    protected void walkToRandomLocInBuilding(Building building, boolean allowFail) {

        Point2D interiorPos = LocalAreaUtil.getRandomInteriorLocation(building);
        Point2D adjustedInteriorPos = LocalAreaUtil.getLocalRelativeLocation(
                interiorPos.getX(), interiorPos.getY(), building);

        // Create subtask for walking to destination.
        createWalkingSubtask(building, adjustedInteriorPos, allowFail);
    }

    /**
     * Walk to an available operator activity spot in a rover.
     * @param rover the rover.
     * @param allowFail true if walking is allowed to fail.
     */
    protected void walkToOperatorActivitySpotInRover(Rover rover, boolean allowFail) {
        walkToActivitySpotInRover(rover, rover.getOperatorActivitySpots(), allowFail);
    }

    /**
     * Walk to an available passenger activity spot in a rover.
     * @param rover the rover.
     * @param allowFail true if walking is allowed to fail.
     */
    protected void walkToPassengerActivitySpotInRover(Rover rover, boolean allowFail) {
        walkToActivitySpotInRover(rover, rover.getPassengerActivitySpots(), allowFail);
    }

    /**
     * Walk to an available lab activity spot in a rover.
     * @param rover the rover.
     * @param allowFail true if walking is allowed to fail.
     */
    protected void walkToLabActivitySpotInRover(Rover rover, boolean allowFail) {
        walkToActivitySpotInRover(rover, rover.getLabActivitySpots(), allowFail);
    }

    /**
     * Walk to an available sick bay activity spot in a rover.
     * @param rover the rover.
     * @param allowFail true if walking is allowed to fail.
     */
    protected void walkToSickBayActivitySpotInRover(Rover rover, boolean allowFail) {
        walkToActivitySpotInRover(rover, rover.getSickBayActivitySpots(), allowFail);
    }

    /**
     * Walk to an available activity spot in a rover from a list of activity spots.
     * @param rover the rover.
     * @param activitySpots list of activity spots.
     * @param allowFail true if walking is allowed to fail.
     */
    private void walkToActivitySpotInRover(Rover rover, List<Point2D> activitySpots, boolean allowFail) {

        // Determine available operator activity spots.
        Point2D activitySpot = null;
        if ((activitySpots != null) && (activitySpots.size() > 0)) {

            List<Point2D> availableSpots = new ArrayList<Point2D>();
            Iterator<Point2D> i = activitySpots.iterator();
            while (i.hasNext()) {
                Point2D spot = i.next();
                Point2D localSpot = LocalAreaUtil.getLocalRelativeLocation(spot.getX(), spot.getY(), rover);
                if (isActivitySpotAvailable(rover, localSpot)) {
                    availableSpots.add(localSpot);
                }
            }

            // Randomly select an activity spot from available spots.
            if (availableSpots.size() > 0) {
                activitySpot = availableSpots.get(RandomUtil.getRandomInt(availableSpots.size() - 1));
            }
        }

        walkToActivitySpotInRover(rover, activitySpot, allowFail);
    }

    /**
     * Checks if an activity spot is available (unoccupied).
     * @param rover the rover.
     * @param activitySpot the activity spot (local-relative)
     * @return true if activity spot is unoccupied.
     */
    private boolean isActivitySpotAvailable(Rover rover, Point2D activitySpot) {

        boolean result = true;

		if (person != null) {
			  // Check all crew members other than person doing task.
	        Iterator<Person> i = rover.getCrew().iterator();
	        while (i.hasNext()) {
	            Person crewmember = i.next();
	            if (!crewmember.equals(person)) {

	                // Check if crew member's location is very close to activity spot.
	                Point2D crewmemberLoc = new Point2D.Double(crewmember.getXLocation(), crewmember.getYLocation());
	                if (LocalAreaUtil.areLocationsClose(activitySpot, crewmemberLoc)) {
	                    result = false;
	                }
	            }
	        }
		}
		else if (robot != null) {
			  // Check all crew members other than robot doing task.
	        Iterator<Robot> i = rover.getRobotCrew().iterator();
	        while (i.hasNext()) {
	        	Robot crewmember = i.next();
	            if (!crewmember.equals(robot)) {

	                // Check if crew member's location is very close to activity spot.
	                Point2D crewmemberLoc = new Point2D.Double(crewmember.getXLocation(), crewmember.getYLocation());
	                if (LocalAreaUtil.areLocationsClose(activitySpot, crewmemberLoc)) {
	                    result = false;
	                }
	            }
	        }
		}



        return result;
    }

    /**
     * Walk to an available activity spot in a rover.
     * @param rover the destination rover.
     * @param activitySpot the activity spot as a Point2D object.
     * @param allowFail true if walking is allowed to fail.
     */
    private void walkToActivitySpotInRover(Rover rover, Point2D activitySpot, boolean allowFail) {

        if (activitySpot != null) {

            // Create subtask for walking to destination.
            createWalkingSubtask(rover, activitySpot, allowFail);
        }
        else {

            // Walk to a random location in the rover.
            walkToRandomLocInRover(rover, allowFail);
        }
    }

    /**
     * Walk to a random interior location in a rover.
     * @param rover the destination rover.
     * @param allowFail true if walking is allowed to fail.
     */
    protected void walkToRandomLocInRover(Rover rover, boolean allowFail) {

        Point2D interiorPos = LocalAreaUtil.getRandomInteriorLocation(rover);
        Point2D adjustedInteriorPos = LocalAreaUtil.getLocalRelativeLocation(
                interiorPos.getX(), interiorPos.getY(), rover);

        // Create subtask for walking to destination.
        createWalkingSubtask(rover, adjustedInteriorPos, allowFail);
    }

    /**
     * Walk to a random location.
     * @param allowFail true if walking is allowed to fail.
     */
    protected void walkToRandomLocation(boolean allowFail) {

		if (person != null) {
		       // If person is in a settlement, walk to random building.
	        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

	            //Building currentBuilding = BuildingManager.getBuilding(person);
	            //List<Building> buildingList = currentBuilding.getBuildingManager().getBuildings(BuildingFunction.LIFE_SUPPORT);
	            List<Building> buildingList = person.getSettlement().getBuildingManager().getBuildings(BuildingFunction.LIFE_SUPPORT);

	            if (buildingList.size() > 0) {
	                int buildingIndex = RandomUtil.getRandomInt(buildingList.size() - 1);
	                Building building = buildingList.get(buildingIndex);

	                walkToRandomLocInBuilding(building, allowFail);
	            }
	        }
	        // If person is in a vehicle, walk to random location within vehicle.
	        else if (person.getLocationSituation() == LocationSituation.IN_VEHICLE) {

	            // Walk to a random location within rover if possible.
	            if (person.getVehicle() instanceof Rover) {
	                walkToRandomLocInRover((Rover) person.getVehicle(), allowFail);
	            }
	        }
		}
		else if (robot != null) {
		       // If robot is in a settlement, walk to random building.
	        if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

	            //Building currentBuilding = BuildingManager.getBuilding(robot);
	            //TODO: determine why the below results in java.lang.NullPointerException
	            //List<Building> buildingList = currentBuilding.getBuildingManager().getBuildings(BuildingFunction.ROBOTIC_STATION);
	        	List<Building> buildingList = robot.getSettlement().getBuildingManager().getBuildings(BuildingFunction.ROBOTIC_STATION);

	            if (buildingList.size() > 0) {
	                int buildingIndex = RandomUtil.getRandomInt(buildingList.size() - 1);
	                Building building = buildingList.get(buildingIndex);
	                // do not stay blocking the hallway
	                //if (currentBuilding.getBuildingType().equals("Hallway"))
	                	//walkToRandomLocInBuilding(building, allowFail);
	                	//walkToRandomLocation(allowFail);
	                //else
	                	walkToRandomLocInBuilding(building, allowFail);
	            }
	        }
	        // If robot is in a vehicle, walk to random location within vehicle.
	        else if (robot.getLocationSituation() == LocationSituation.IN_VEHICLE) {

	            // Walk to a random location within rover if possible.
	            if (robot.getVehicle() instanceof Rover) {
	                walkToRandomLocInRover((Rover) robot.getVehicle(), allowFail);
	            }
	        }
		}
    }

    protected void walkToAssignedDutyLocation(Robot robot, boolean allowFail) {
    	if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
    		Building currentBuilding = BuildingManager.getBuilding(robot);

       		//if (currentBuilding == null)
             //   throw new IllegalStateException("currentBuilding is null");

    		if (currentBuilding != null) {
	    		String type = robot.getRobotType().getName();
	    		//List<Building> buildingList;
	    		BuildingFunction fct = null;

	    		if (type.equals("CHEFBOT"))
	    			fct = BuildingFunction.COOKING;
	    		else if (type.equals("CONSTRUCTIONBOT"))
	    			fct = BuildingFunction.ROBOTIC_STATION;
	    		else if (type.equals("DELIVERYBOT"))
	    			fct = BuildingFunction.ROBOTIC_STATION;
	    		else if (type.equals("GARDENBOT"))
	    			fct = BuildingFunction.FARMING;
	    		else if (type.equals("MAKERBOT"))
	    			fct = BuildingFunction.MANUFACTURE;
	    		else if (type.equals("MEDICBOT"))
	    			fct = BuildingFunction.MEDICAL_CARE;
	    		else if (type.equals("REPAIRBOT"))
	    			fct = BuildingFunction.ROBOTIC_STATION;

	    		if (fct == null)
	    			fct = BuildingFunction.LIVING_ACCOMODATIONS;

	       		if (fct == null)
	    			fct = BuildingFunction.LIFE_SUPPORT;

	       		// Added debugging statement below
	            if (currentBuilding.getBuildingManager() == null)
	                throw new IllegalStateException("currentBuilding.getBuildingManager() is null");
	            if (currentBuilding.getBuildingManager().getBuildings(fct) == null)
	                throw new IllegalStateException("currentBuilding.getBuildingManager().getBuildings(fct) is null");


	            List<Building> buildingList = currentBuilding.getBuildingManager().getBuildings(fct);

	            if (buildingList.size() > 0) {
	                int buildingIndex = RandomUtil.getRandomInt(buildingList.size() - 1);
	                Building building = buildingList.get(buildingIndex);
	                walkToActivitySpotInBuilding(building, fct, true);
	            }
    		}
    	}
    }


    /**
     * Create a walk to an interior position in a building or vehicle.
     * @param interiorObject the destination interior object.
     * @param settlementPos the settlement local position destination.
     * @param allowFail true if walking is allowed to fail.
     */
    private void createWalkingSubtask(LocalBoundedObject interiorObject, Point2D settlementPos, boolean allowFail) {

		if (person != null) {
		       if (Walk.canWalkAllSteps(person, settlementPos.getX(), settlementPos.getY(),
		                interiorObject)) {

		            // Add subtask for walking to destination.
		            addSubTask(new Walk(person, settlementPos.getX(), settlementPos.getY(),
		                    interiorObject));
		        }
		        else {
		            logger.fine(person.getName() + " unable to walk to " + interiorObject);

		            if (!allowFail) {
		                endTask();
		            }
		        }
		}
		else if (robot != null) {
		       if (Walk.canWalkAllSteps(robot, settlementPos.getX(), settlementPos.getY(),
		                interiorObject)) {

		            // Add subtask for walking to destination.
		            addSubTask(new Walk(robot, settlementPos.getX(), settlementPos.getY(),
		                    interiorObject));
		        }
		        else {
		            logger.fine(robot.getName() + " unable to walk to " + interiorObject);

		            if (!allowFail) {
		                endTask();
		            }
		        }
		}

    }

    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {
        name = null;
        person = null;
        robot = null;
        description = null;
        if (subTask != null) {
            subTask.destroy();
        }
        subTask = null;
        phase = null;
        teacher = null;
        phases.clear();
        phases = null;
    }
}