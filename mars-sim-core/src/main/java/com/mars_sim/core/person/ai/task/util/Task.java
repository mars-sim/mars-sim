/*
 * Mars Simulation Project
 * Task.java
 * @date 2023-11-20
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.construction.ConstructionConfig;
import com.mars_sim.core.building.function.Function;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.farming.CropConfig;
import com.mars_sim.core.environment.OrbitInfo;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.events.HistoricalEvent;

import com.mars_sim.core.events.HistoricalEventManager;
import com.mars_sim.core.events.HistoricalEventType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.map.location.LocalBoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PersonConfig;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionManager;
import com.mars_sim.core.person.ai.task.Walk;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact.PhysicalEffort;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.science.ScientificStudyManager;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Rover;

/**
 * The Task class is an abstract parent class for tasks that allow people to do
 * various things. A person's TaskManager keeps track of one current task for
 * the person, but a task may use other tasks internally to accomplish things.
 */
public abstract class Task implements Serializable, Comparable<Task> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(Task.class.getName());

	// Static members

	/** Level of top level Task */
	private static final int TOP_LEVEL = 1;
	/** The standard stress effect of a task within a person's job. */
	private static final double SKILL_STRESS_MODIFIER = .1D;
    /** The standard amount of millisols to be consumed in a phase. */
	private static double standardPulseTime = MasterClock.INITIAL_PULSE_WIDTH;

	// Data members
	/** True if task is finished. */
	private boolean done;
	/** True if task has a time duration. */
	protected boolean hasDuration;
	/** Is this task effort driven. */
	protected boolean effortDriven;
	/** Task should create Historical events. */
	private boolean createEvents;
	
	/** The level of this Task, one is top level Task **/
	private int level = TOP_LEVEL;
	/** The modified skill level. */
	private int effectiveSkillLevel = -1;
	
	/** Amount of time required to complete current phase (in millisols) */
	protected double phaseTimeRequired;
	/** Amount of time completed on the current phase (in millisols) */
	protected double phaseTimeCompleted;

	/** The time duration (in millisols) of the task. */
	private double duration;
	/** The current amount of time spent on the task (in millisols). */
	private double timeCompleted;
	
	/** The id of the person/robot. */
	protected Integer id;
	/** The id of the teacher. */
	protected Integer teacherID;
	
	/** The name of the task. */
	private String name = "";
	/** Description of the task. */
	private String description = "";

	/** The person performing the task. */
	protected transient Person person;
	/** The robot performing the task. */
	protected transient Robot robot;
	/** The worker performing the task */
	protected transient Worker worker;
	/** Unit for events distribution */
	private transient Unit eventTarget;
	
	/** The worker teaching this task if any. */
	private transient Worker teacher;

	/** The sub-task of this task. */
	protected Task subTask;
	/** The phase of this task. */
	private TaskPhase phase;

	// Impact of doing this Task
	private ExperienceImpact impact;


	/** The static instance of the master clock */
	protected static MasterClock masterClock;
	/** The static instance of the event manager */
	protected static HistoricalEventManager eventManager;
	/** The static instance of the UnitManager */
	protected static UnitManager unitManager;
	/** The static instance of the ScientificStudyManager */
	protected static ScientificStudyManager scientificStudyManager;
	/** The static instance of the SurfaceFeatures */
	protected static SurfaceFeatures surfaceFeatures;
	/** The static instance of the OrbitInfo */
	protected static OrbitInfo orbitInfo;
	/** The static instance of the MissionManager */
	protected static MissionManager missionManager;
	/** The static instance of the SimulationConfig */
	protected static SimulationConfig simulationConfig = SimulationConfig.instance();
	/** The static instance of the personConfig */
	protected static PersonConfig personConfig = simulationConfig.getPersonConfig();
	/** The static instance of the personConfig */
	protected static CropConfig cropConfig = simulationConfig.getCropConfiguration();
	/** The static instance of the constructionConfig */
	protected static ConstructionConfig constructionConfig = simulationConfig.getConstructionConfiguration();

	private static Simulation sim;
	
	/**
	 * Constructs a Task object that has a fixed duration.
	 * 
	 * @param name            the name of the task
	 * @param worker          the worker performing the task
	 * @param effort          Does this task require physical effort
	 * @param createEvents    Does this task create events?
	 * @param stressModifier  stress modified by person performing task per
	 *                        millisol.
	 * @param primarySkill    The main skill needed for this Task
	 * @param experienceRatio What is the ratio of time per experience points
	 * @param duration        the time duration (in millisols) of the task (or 0 if
	 *                        none)
	 */
	protected Task(String name, Worker worker, boolean effort, boolean createEvents, double stressModifier,
			SkillType primarySkill, double experienceRatio, double duration) {

		this(name, worker, effort, createEvents, stressModifier, primarySkill, experienceRatio);

		if ((duration <= 0D) || !Double.isFinite(duration)) {
			throw new IllegalArgumentException("Task duration must be positive :" + duration);
		}

		this.duration = duration;
		this.hasDuration = true;
	}

	/**
	 * Constructs a Task object that has an impact on the worker
	 * 
	 * @param name            the name of the task
	 * @param worker          the worker performing the task
	 * @param createEvents    Does this task create events?
	 * @param impact		  Impact on worker doing this Task
	 * @param duration        Fixed duration task
	 */
	protected Task(String name, Worker worker, boolean createEvents, 
					ExperienceImpact impact, double duration) {
		this.name = name;
		this.createEvents = createEvents;
		this.duration = duration;
		this.hasDuration = duration > 0D;
		this.impact = impact;

		init(worker);
	}

	
	/**
	 * Constructs a Task object.
	 * 
	 * @param name            the name of the task
	 * @param worker          the worker performing the task
	 * @param effort          Does this task require physical effort
	 * @param createEvents    Does this task create events?
	 * @param stressModifier  stress modified by person performing task per
	 *                        millisol.
	 * @param primarySkill    Main skill needed for task
	 * @param experienceRatio Ratio of work time per experience point
	 */
	protected Task(String name, Worker worker, boolean effort, boolean createEvents, double stressModifier,
			SkillType primarySkill, double experienceRatio) {
		this.name = name;
		this.effortDriven = effort;
		this.createEvents = createEvents;
		this.impact = new ExperienceImpact(experienceRatio, NaturalAttributeType.EXPERIENCE_APTITUDE, effort, stressModifier, primarySkill);
		this.hasDuration = false;

		init(worker);
	}

	private void init(Worker worker) { 
		if (worker == null) {
			throw new IllegalArgumentException("Worker can not be null");
		}
		this.worker = worker;

		if (worker instanceof Person p) {
			this.person = p;
			this.id = p.getIdentifier();
			this.eventTarget = p;
		}

		else {
			this.robot = (Robot) worker;
			this.id = robot.getIdentifier();
			this.eventTarget = robot;
		}

		// Call setDescription to record this task
		setDescription(name);
		
		done = false;
		timeCompleted = 0D;
		phase = null;

		// For sub task
		if (subTask != null) {
			subTask.setPhase(null);
			subTask = null;
		}
	}

	/**
	 * Constructs a basic Task object. It requires no skill and has a fixed
	 * duration.
	 * 
	 * @param name           the name of the task
	 * @param worker         the worker performing the task
	 * @param effort         Does this task require physical effort
	 * @param createEvents   Does this task create events?
	 * @param stressModifier stress modified by person performing task per millisol.
	 * @param duration       the time duration (in millisols) of the task (or 0 if
	 *                       none)
	 */
	protected Task(String name, Worker worker, boolean effort, boolean createEvents, double stressModifier,
			double duration) {
		this(name, worker, effort, createEvents, stressModifier, null, 0D, duration);
	}

	/**
	 * Ends a task because an internal condition is wrong.
	 * 
	 * @param reason Reason for the end.
	 */
	protected void clearTask(String reason) {
		logger.warning(worker, "Ended '" + name + "' early. Reason: " + reason);
		endTask();
	}

	/**
	 * Ends the task and performs any final actions.
	 * This has a lifecycle of invoking the callback clearDown method.
	 * This method cannot be overridden but the lifecycle callback
	 * method should be used to receive notification.
	 * 
	 * @see #clearDown()
	 */
	public void endTask() {
		// Check we haven't been here already ??
		if (!done) {
			// Set done to true first to catch any re-calls
			done = true;
			// Clear down the subclass task
			clearDown();
			// Set phase to null
			setPhase(null);
			// Set description to blank
			setDescription("");		
			// Fires task end event
			eventTarget.fireUnitUpdate(UnitEventType.TASK_ENDED_EVENT, this);
		}
	}

	/**
	 * This method is part of the Task Life Cycle. It is called once
	 * and only once per Task when it is ended.
	 * Subclasses should override to receive callback when the Task is ending.
	 */
	protected void clearDown() {
	}

	/**
	 * Helper method for Event subclasses to register historical events.
	 * 
	 * @param newEvent the new event
	 */
	public static void registerNewEvent(HistoricalEvent newEvent) {
		eventManager.registerNewEvent(newEvent);
	}
	
	/**
	 * Returns if the task requires physical effort.
	 * 
	 * @return Effort driven.
	 */
	public boolean isEffortDriven() {
		return getEffortRequired() != PhysicalEffort.NONE;
	}

	/**
	 * Returns the effort required for this task.
	 */
    public PhysicalEffort getEffortRequired() {
        return getImpact().getEffortRequired();
    }

	/**
	 * Returns the name of the task (not its subtask).
	 * 
	 * @return the task's name
	 */
	public String getName() {
		return getName(false);
	}

	/**
	 * Gets the name of the task.
	 * 
	 * @param allowSubtask true if subtask name should be used.
	 * @return the task's name.
	 */
	public String getName(boolean allowSubtask) {
		if (allowSubtask && (subTask != null) && !subTask.done) {
			return subTask.getName();
		} else {
			return name;
		}
	}

	/**
	 * Gets the task class simple name.
	 * 
	 * @return the task class simple name in String.
	 */
	public String getTaskSimpleName() {
		return this.getClass().getSimpleName();

	}

	/**
	 * Sets the task's name.
	 * 
	 * @param name the task name.
	 */
	protected void setName(String name) {
		this.name = name;
		this.eventTarget.fireUnitUpdate(UnitEventType.TASK_NAME_EVENT, name);
	}

	/**
	 * Returns a string that is a description of what the task is currently doing.
	 * This is mainly for user interface purposes. Derived tasks should extend this
	 * if necessary. Defaults to just the name of the task.
	 * 
	 * @return the description of what the task is currently doing
	 */
	public String getDescription() {
		if (description == null) {
			return "";
		}
		return description;
	}

	/**
	 * Gets the description of the task.
	 * 
	 * @param allowSubtask true if subtask description should be used.
	 * @return the task description.
	 */
	public String getDescription(boolean allowSubtask) {
		if (allowSubtask && subTask != null && !subTask.done) {
			return subTask.getDescription();
		} else {
			return getDescription();
		}
	}

	/**
	 * Sets the task's description.
	 * 
	 * @param des the task description.
	 */
	protected void setDescription(String des) {
		setDescription(des, true);
	}

	/**
	 * Sets the task's description without recording the task.
	 * 
	 * @param des the task description.
	 */
	protected void setDescriptionDone(String des) {
		if (!description.equalsIgnoreCase(des)) {
			description = des;
			eventTarget.fireUnitUpdate(UnitEventType.TASK_DESCRIPTION_EVENT, des);
		}
	}
		
	/**
	 * Sets the task's description.
	 * 
	 * @param des the task description.
	 * @param recordTask true if wanting to record
	 */
	protected void setDescription(String des, boolean recordTask) {
		if (!description.equalsIgnoreCase(des)) {
			description = des;
			eventTarget.fireUnitUpdate(UnitEventType.TASK_DESCRIPTION_EVENT, des);
			
			if (!des.equals("") && worker.getTaskManager().getTask() != null
				// Record the activity
				&& recordTask && canRecord()) {
					Mission ms = worker.getMission();
					worker.getTaskManager().recordTask(this, ms);
			}
		}
	}
	
	/**
	 * Returns a boolean whether this task should generate events.
	 * 
	 * @return boolean flag.
	 */
	protected boolean getCreateEvents() {
		return createEvents;
	}

	/**
	 * This is a helper method to produce a String for the state of this Task.
	 * Note the stroing may be long as it is the task name followed by the phase.
	 * @return
	 */
	public String getStatus() {
		String result = getName();
		if (done) {
			result += " (Done)";
		}
		else if (phase != null) {
			result += " (" + phase.getName() +")";
		}
		return result;
	}

	/**
	 * Gets a string of the current phase of the task.
	 * 
	 * @return the current phase of the task
	 */
	public TaskPhase getPhase() {
		return phase;
	}

	/**
	 * Sets the task's current phase.
	 * 
	 * @param newPhase the phase to set the task at.
	 */
	protected void setPhase(TaskPhase newPhase) {
		phase = newPhase;
	
		// Method is called via endTask with a null phase
		// TaskPhase should have isRecordable method to stop recording of minor phases
		if (newPhase != null) {
			eventTarget.fireUnitUpdate(UnitEventType.TASK_PHASE_EVENT, newPhase);
		}
		
		// Record the activity
		if (canRecord()) {
			Mission ms = worker.getMission();
			worker.getTaskManager().recordTask(this, ms);
		}
	}

	/**
	 * Does a change of Phase for this Task generate an entry in the Task Schedule ?
	 * 
	 * @return true by default
	 */
	protected boolean canRecord() {
		return true;
	}
	
	/**
	 * Adds a phase to the task's collection of phases.
	 * 
	 * @deprecated
	 * @param newPhase the new phase to add.
	 */
	protected void addPhase(TaskPhase newPhase) {
		// This is redundant and should be deprecated
	}

	/**
	 * Determines if task is still active.
	 * 
	 * @return true if task is completed
	 */
	public boolean isDone() {
		return done;
	}

	/**
	 * Adds a new subtask.
	 * 
	 * @param newSubTask the new sub-task to be added
	 * @return true if the subtask can be added
	 */
	public boolean addSubTask(Task newSubTask) {
		if (subTask != null) {
			// Already has a subtask
			if (subTask.done) {
				// The existing subtask is done
				createSubTask(newSubTask);
				return true;
			}
			
			// if the existing subtask is not done
			else if (!subTask.getName().equalsIgnoreCase(newSubTask.getName())
				|| !subTask.getDescription().equalsIgnoreCase(newSubTask.getDescription())) {
				return subTask.addSubTask(newSubTask);
			}
		}

		else {
			// doesn't have a subtask yet
			createSubTask(newSubTask);
			return true;
		}
		
		return false;
	}

	/**
	 * Creates a new subtask.
	 * 
	 * @param newSubTask the new subtask to be added
	 */
	private void createSubTask(Task newSubTask) {
		subTask = newSubTask;
		subTask.level = this.level + 1;
		eventTarget.fireUnitUpdate(UnitEventType.TASK_SUBTASK_EVENT, newSubTask);
	}

	/**
	 * Builds the stack of Task from this point downwards.
	 * 
	 * @param stack
	 */
    void buildStack(List<Task> stack) {
		stack.add(this);
		if ((subTask != null) && !subTask.isDone()) {
			subTask.buildStack(stack);
		}
    }

	/**
	 * Gets the task's subtask. Returns null if none.
	 * 
	 * @return subtask
	 */
	public Task getSubTask() {
		return subTask;
	}

	/**
	 * Performs the task for the given number of seconds. Children should override
	 * and implement this.
	 * 
	 * @param time amount of time (millisol) given to perform the task (in
	 *             millisols)
	 * @return amount of time (millisol) remaining after performing the task (in
	 *         millisols)
	 */
	public double performTask(double time) {
		double timeLeft = time;
		if (subTask != null) {
			if (subTask.isDone()) {
				subTask.setPhase(null);
				subTask.setDescription("");
			} else {
				timeLeft = subTask.performTask(timeLeft);
			}
		}

		// If no subtask, and still active perform this task (could be ended by the subTask).
		if (!done && ((subTask == null) || subTask.isDone())) {

			if (person != null) {
				// If task is effort-driven and person is incapacitated, end task.
				if (effortDriven && (person.getPerformanceRating() == 0D)) {
					// "Resurrect" him a little to give him a chance to make amend
					person.getPhysicalCondition().setPerformanceFactor(.1);
					endTask();
				} else {
					timeLeft = executeMappedPhase(timeLeft, time);
				}
			}

			else {
				// If task is effort-driven and robot is disabled, end task.
				if (effortDriven && (robot.getPerformanceRating() == 0D)) {
					endTask();
				} else {
					timeLeft = executeMappedPhase(timeLeft, time);
				}
			}
		}

		return timeLeft;
	}

	/**
	 * Executes the mapped phase repeatedly.
	 * 
	 * @param timeLeft the time left previously
	 * @param time the time pulse
	 * @return the time left currently
	 */
	private double executeMappedPhase(double timeLeft, double time) {
		// Perform phases of task until time is up or task is done.
		while ((timeLeft > 0D) && !done && getPhase() != null && ((subTask == null) || subTask.done)) {

			if (hasDuration) {
				// Keep track of the duration of the task.
				double timeRequired = duration - timeCompleted;
				if (timeLeft > timeRequired) {
					timeLeft = timeLeft - timeRequired;
					// No need to record consumed time as already know the duration
					performMappedPhase(timeRequired);
					timeCompleted = duration;
					// NOTE: does endTask() cause Sleep task to unnecessarily end and restart ?
					endTask();
				} else {
					double remainingTime = timeLeft;
					timeLeft = performMappedPhase(timeLeft);
					timeCompleted += remainingTime;
				}
			} else {
				timeLeft = performMappedPhase(timeLeft);
			}

			// Some Task return a percentage of the time which can produce a very small number
			if (timeLeft < 0.000001) {
				timeLeft = 0D;
			}
		}

		return timeLeft;
	}

	/**
	 * Performs the method mapped to the task's current phase.
	 * 
	 * @param time the amount of time (millisol) the phase is to be performed.
	 * @return the remaining time (millisol) after the phase has been performed.
	 */
	protected abstract double performMappedPhase(double time);

	/**
	 * Should the start of this task create an historical event ?
	 * 
	 * @param create New flag value.
	 */
	protected void setCreateEvents(boolean create) {
		createEvents = create;
	}

	/**
	 * Gets a string representation of this Task. It's content will consist of the
	 * description.
	 *
	 * @return Description of the task.
	 */
	public String toString() {
		return description;
	}

	/**
	 * Compares this object to another for an ordering. THe ordering is based on the
	 * alphabetic ordering of the Name attribute.
	 *
	 * @param other Object to compare against.
	 * @return integer comparison of the two objects.
	 * @throws ClassCastException if the object in not of a Task.
	 */
	@Override
	public int compareTo(Task other) {
		return name.compareTo(other.name);
	}

	/**
	 * Gets the effective skill level a worker has at this task.
	 * 
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		// This can be dropped once ExpereicneImpact is defined in constructor
		if (effectiveSkillLevel == -1) {
			effectiveSkillLevel = getImpact().getEffectiveSkillLevel(worker);
		}
		return effectiveSkillLevel;
	} 

	/**
	 * Gets the relevant Impact assessment of doing this Task. It is a temporary measure
	 * as eventually the impact will come via the constructor.
	 * 
	 * @return Impact assessment of doing this Task
	 */
	private ExperienceImpact getImpact() {
		var result = (phase != null ? phase.getImpact() : null);
		if (result == null) {
			// Use default impact
			result = impact;
		}
		return result;
	} 

	/**
	 * Gets a list of the skills associated with this task. May be empty list if no
	 * associated skills.
	 * 
	 * @return list of skills
	 */
	public Set<SkillType> getAssociatedSkills() {
		return getImpact().getImpactedSkills();
	}

	/**
	 * Checks if someone is teaching this task to the person performing it.
	 * 
	 * @return true if teacher.
	 */
	public boolean hasTeacher() {
		return (teacher != null);
	}

	/**
	 * Gets the worker teaching this task.
	 * 
	 * @return teacher or null if none.
	 */
	public Worker getTeacher() {
		return teacher;
	}

	/**
	 * Sets the worker teaching this task.
	 * 
	 * @param newTeacher the new teacher.
	 */
	public void setTeacher(Worker newTeacher) {
		this.teacher = newTeacher;
		teacherID = teacher.getIdentifier();
	}

	/**
	 * Gets the worker working on this task.
	 * 
	 * @return
	 */
	public Worker getWorker() {
		return worker;
	}
	
	/**
	 * Gets the experience modifier when being taught by a teacher.
	 * 
	 * @return modifier;
	 */
	protected double getTeachingExperienceModifier() {
		double result = 1D;

		if (hasTeacher()) {
			int teachingModifier = teacher.getNaturalAttributeManager()
					.getAttribute(NaturalAttributeType.TEACHING);
			int learningModifier = worker.getNaturalAttributeManager()
					.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);

			result += (teachingModifier + learningModifier) / 100D;
		}

		return result;
	}

	/**
	 * Adds experience to the worker's skills used in this task.
	 * 
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
		if (time > 0) {
			var i = getImpact();
			i.apply(worker, time,  getTeachingExperienceModifier(),
							i.getEffectiveSkillLevel(worker) * SKILL_STRESS_MODIFIER);
		}
	}

	/**
	 * Checks for a simple accident in entity. This will use the
	 * {@link #getEffectiveSkillLevel()} method in the risk calculation.
	 * 
	 * @param entity Entity that can malfunction
	 * @param time   the amount of time working (in millisols)
	 * @param chance Chance of an accident
	 */
	protected void checkForAccident(Malfunctionable entity, double time, double chance) {
		int skill = getEffectiveSkillLevel();
		checkForAccident(entity, time, chance, skill, null);
	}

	/**
	 * Checks for a complex accident in an Entity. This can cover inside & outside
	 * accidents
	 * 
	 * @param entity   Entity that can malfunction
	 * @param time     the amount of time working (in millisols)
	 * @param chance   Chance of an accident
	 * @param skill    Skill of the worker
	 * @param location Location of the accident; maybe null
	 */
	protected void checkForAccident(Malfunctionable entity, double time, double chance, int skill, String location) {

		if (skill <= 3) {
			chance *= (4 - skill);
		} else {
			chance /= (skill - 2);
		}

		// Modify based on the entity building's wear condition.
		chance *= entity.getMalfunctionManager().getAccidentModifier();

		if (RandomUtil.lessThanRandPercent(chance * time)) {
			entity.getMalfunctionManager().createASeriesOfMalfunctions(location, (Unit)worker);
		}
	}

	/**
	 * Gets the duration of the task or 0 if none.
	 * 
	 * @return duration (millisol)
	 */
	public double getDuration() {
		return duration;
	}

	/**
	 * Sets the duration of the task.
	 * 
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
	 * 
	 * @return time (in millisols)
	 */
	protected double getTimeCompleted() {
		return timeCompleted;
	}

	/**
	 * Gets the amount of time left.
	 * 
	 * @return
	 */
	public double getTimeLeft() {
		return duration - timeCompleted;
	}
	
	/**
	 * Walks to an available activity spot for a specific task in a building.
	 * 
	 * @param building  the destination building.
	 * @param function  Particular area within the building
	 * @param allowFail true if walking is allowed to fail.
	 * @return
	 */
	protected boolean walkToTaskSpecificActivitySpotInBuilding(Building building, FunctionType function, boolean allowFail) {
		return walkToActivitySpotInBuilding(building, function, allowFail);
	}

	/**
	 * Walks to an available research activity spot in a building.
	 * 
	 * @param building  the destination building.
	 * @param allowFail true if walking is allowed to fail.
	 */
	protected void walkToResearchSpotInBuilding(Building building, boolean allowFail) {
		
		boolean success = walkToActivitySpotInBuilding(building, FunctionType.RESEARCH, allowFail);
		
		if (!success) {
			success = walkToActivitySpotInBuilding(building, FunctionType.ADMINISTRATION, allowFail);
		} 
		if (!success) {
			success = walkToActivitySpotInBuilding(building, FunctionType.DINING, allowFail);
		} 
		if (!success) {
			success = walkToActivitySpotInBuilding(building, FunctionType.RECREATION, allowFail);			
		} 
		if (!success) {
			success = walkToActivitySpotInBuilding(building, FunctionType.LIVING_ACCOMMODATION, allowFail);			
		} 
		if (!success) {
			// If no available activity spot, go to an empty location in building
			success = walkToEmptyActivitySpotInBuilding(building, allowFail);
		}
	}

	/**
	 * Walks to the bed previously assigned for this person.
	 * 
	 * @param person the person who walks to the bed
	 * @param allowFail true if allowing the walk task to fail
	 */
	public boolean walkToBed(Person person, boolean allowFail) {
		boolean canWalk = false;
		
		var bed = person.getBed();
		if (bed == null) {
			logger.info(person, 10_000L, "I have no bed assigned to me.");
			return canWalk;
		}
		
		// Check my own position
		LocalPosition myLoc = person.getPosition();
		// Allocate it
		person.setActivitySpot(bed);

		LocalPosition bedLoc = bed.getAllocated().getPos();
		if (myLoc.equals(bedLoc)) {
			return canWalk;
		}

		// Create subtask for walking to destination.
		return createWalkingSubtask(bed.getOwner(), bedLoc, allowFail, true);
	}

	/**
	 * Walks to an available activity spot of a particular function type in a
	 * building.
	 * 
	 * @param building     the destination building.
	 * @param functionType the building function type for the activity.
	 * @param allowFail    true if walking is allowed to fail.
	 * @return
	 */
	protected boolean walkToActivitySpotInBuilding(Building building, FunctionType functionType, 
			boolean allowFail) {
		
		Function f = building.getFunction(functionType);
		if (f == null) {
			return false;
		}
		return walkToActivitySpotInFunction(building, f, allowFail);
	}

	/**
	 * Finds an EVA spot in this building and walk to this spot.
	 * 
	 * @param building
	 * @param newPos
	 * @param needEVA
	 * @return
	 */
	protected boolean walkToEVASpot(Building building, LocalPosition newPos, boolean needEVA) {
		return walkToActivitySpot(building, building.getFunction(FunctionType.EVA), newPos, false, needEVA);
	}

	/**
	 * Walks to an empty activity spot in a building.
	 * 
	 * @param building     the destination building.
	 * @param allowFail    true if walking is allowed to fail.
	 * @return
	 */
	protected boolean walkToEmptyActivitySpotInBuilding(Building building, boolean allowFail) {	
		Function f = building.getEmptyActivitySpotFunction();
		
		if (f == null) {
			return false;
		}

		return walkToActivitySpotInFunction(building, f, allowFail);
	}

	/**
	 * Walks to an activity spot in a function.
	 * 
	 * @param building
	 * @param f
	 * @param allowFail
	 * @return
	 */
	private boolean walkToActivitySpotInFunction(Building building, Function f, 
			boolean allowFail) {

		Building originBuilding = worker.getBuildingLocation();
		
		if (originBuilding != null && originBuilding.equals(building)
			// Check if this worker has already occupied a spot for this function
			&& f.checkWorkerActivitySpot(worker)) {
//			logger.info(worker, "Already at a spot for " + f.getFunctionType().getName() + ". No need to go further to claim one.");
				return true;
		}
		
		LocalPosition loc = f.getAvailableActivitySpot();
		
		if (loc == null) {
//			logger.info(worker, 10_000L, getDescription() + ". No available spots for " + f.getFunctionType().getName() + " in " + building +  ".");
			return false;
		}
			
		// Claim this activity spot
		boolean canClaim = f.claimActivitySpot(loc, worker);
		
		if (canClaim) {
			// Create subtask for walking to destination.
			// e.g. building can be an outside building that need Toggling on Resource Process
			boolean canWalk = createWalkingSubtask(building, loc, allowFail, true);
			
			if (canWalk) {
//					logger.info(worker, "Walking toward " + building + ".");

				// Q: how to make it the building only after it has arrived ?
				// Use setToBuilding() to both add to life support/robotic station and the building itself
				// BuildingManager.setToBuilding(worker, building);
				
//					logger.info(worker, "Claimed the spot. Walking toward " + building + ".");
				return true;
			}
			else {
				// Reverse the walk and go back to the original building
//				createWalkingSubtask(originBuilding, originLoc, allowFail, true);
//				logger.info(worker, 10_000L, "Failed to claim the spot. Walking back to " + originBuilding + ".");
				
				// Unclaim the activity spot.
				worker.leaveActivitySpot(true);
				
				logger.info(worker, 10_000L, "Unable to walk to " + loc + " in " + building + " " +  ".");
				return false;
			}
		}
		else {
			logger.info(worker, 10_000L, "Failed to claim the spot at " + building + ".");
			return false;
		}
	}
	
	/**
	 * Walks to a new pos in the function. 
	 * 
	 * @param building
	 * @param f
	 * @param newPos
	 * @param allowFail
	 * @param needEVA
	 * @return
	 */
	private boolean walkToActivitySpot(Building building, Function f, LocalPosition newPos, 
			boolean allowFail, boolean needEVA) {
		if (newPos != null) {
			// Create subtask for walking to destination.
			boolean canWalk = createWalkingSubtask(building, newPos, allowFail, needEVA);
			
			if (canWalk) {
				// Add to this activity spot
				return f.claimActivitySpot(newPos, worker);
			}
		} 

		return false;
	}
	
	/**
	 * Walks to a random location in a building.
	 * @apiNote This method should only be used when inspecting or repairing the building.
	 * 
	 * @param building  the destination building.
	 * @param allowFail true if walking is allowed to fail.
	 */
	protected boolean walkToRandomLocInBuilding(Building building, boolean allowFail) {
		// Gets a settlement wide location
		LocalPosition sPos = LocalAreaUtil.getRandomLocalPos(building);
		// Create subtask for walking to destination.
		return createWalkingSubtask(building, sPos, allowFail, false);
	}

	
	/**
	 * Walks to an available passenger activity spot in a rover.
	 * 
	 * @param rover     the rover.
	 * @param allowFail true if walking is allowed to fail.
	 * @return
	 */
	protected boolean walkToPassengerActivitySpotInRover(Rover rover, boolean allowFail) {
		return walkToActivitySpotInRover(rover, rover.getPassengerActivitySpots(), allowFail);
	}

	/**
	 * Walks to an available lab activity spot in a rover.
	 * 
	 * @param rover     the rover.
	 * @param allowFail true if walking is allowed to fail.
	 * @return
	 */
	protected boolean walkToLabActivitySpotInRover(Rover rover, boolean allowFail) {
		return walkToActivitySpotInRover(rover, rover.getLabActivitySpots(), allowFail);
	}

	/**
	 * Walks to an available sick bay activity spot in a rover.
	 * 
	 * @param rover     the rover.
	 * @param allowFail true if walking is allowed to fail.
     * @return
	 */
	protected boolean walkToSickBayActivitySpotInRover(Rover rover, boolean allowFail) {
		return walkToActivitySpotInRover(rover, rover.getSickBayActivitySpots(), allowFail);
	}

	/**
	 * Walks to an available activity spot in a rover from a list of activity spots.
	 * 
	 * @param rover         the rover.
	 * @param activitySpots list of activity spots.
	 * @param allowFail     true if walking is allowed to fail.
	 * @return
	 */
	protected boolean walkToActivitySpotInRover(Rover rover, List<LocalPosition> activitySpots, boolean allowFail) {
		boolean success = false;
		
		// Determine available operator activity spots.
		LocalPosition activitySpot = null;
		if (activitySpots != null && !activitySpots.isEmpty()) {

			List<LocalPosition> availableSpots = new ArrayList<>();
			Iterator<LocalPosition> i = activitySpots.iterator();
			while (i.hasNext()) {
				LocalPosition roverLocalLoc = i.next();
				LocalPosition settlementLoc = LocalAreaUtil.convert2SettlementPos(roverLocalLoc, rover);
				if (isActivitySpotAvailable(rover, settlementLoc)) {
					availableSpots.add(settlementLoc);
				}
			}

			// Randomly select an activity spot from available spots.
			if (!availableSpots.isEmpty()) {
				activitySpot = availableSpots.get(RandomUtil.getRandomInt(availableSpots.size() - 1));
			}
		}

		if (activitySpot != null)
			success = walkToActivitySpotInRover(rover, activitySpot, allowFail);
		
		return success;
	}

	/**
	 * Checks if an activity spot is available (unoccupied).
	 * 
	 * @param rover        the rover.
	 * @param settlementLoc the activity spot (local-relative)
	 * @return true if activity spot is unoccupied.
	 */
	private boolean isActivitySpotAvailable(Rover rover, LocalPosition settlementLoc) {

		boolean result = true;

		if (person != null) {
			// Check all crew members other than person doing task.
			for(Person crewmember: rover.getCrew()) {
				// Check if crew member's position is very close to settlementLoc
				if (!crewmember.equals(person) && settlementLoc.isClose(crewmember.getPosition())) {
					result = false;
				}
			}
		} else {
			// Check all crew members other than robot doing task.
			for(Robot crewmember : rover.getRobotCrew()) {
				// Check if crew member's position is very close to settlementLoc
				if (!crewmember.equals(robot) && settlementLoc.isClose(crewmember.getPosition())) {
						result = false;
				}
			}
		}

		return result;
	}

	/**
	 * Walks to an available activity spot in a rover.
	 * 
	 * @param rover        the destination rover.
	 * @param activitySpot the activity spot as a Point2D object.
	 * @param allowFail    true if walking is allowed to fail.
	 */
	private boolean walkToActivitySpotInRover(Rover rover, LocalPosition activitySpot, boolean allowFail) {
		boolean success = false;
		
		if (activitySpot != null) {
			// Create subtask for walking to destination.
			success = createWalkingSubtask(rover, activitySpot, allowFail, true);
		} 
		else {
			// Walk to a random location in the rover.
			success = walkToRandomLocInRover(rover, allowFail);
		}
		
		return success;
	}

	/**
	 * Walks to a random interior location in a rover.
	 * 
	 * @param rover     the destination rover.
	 * @param allowFail true if walking is allowed to fail.
	 * @return
	 */
	protected boolean walkToRandomLocInRover(Rover rover, boolean allowFail) {
		boolean success = false;
		
		LocalPosition sPos = LocalAreaUtil.getRandomLocalPos(rover);

		if (sPos != null)
			// Create subtask for walking to destination.
			success = createWalkingSubtask(rover, sPos, allowFail, true);
		
		return success;
	}

	/**
	 * Walks to a random location.
	 * 
	 * @param allowFail true if walking is allowed to fail.
	 */
	public void walkToRandomLocation(boolean allowFail) {

		if (person != null) {
			// If person is in a settlement, walk to random building.
			Settlement s = person.getSettlement();
			if (s != null) {
				Set<Building> buildingList = s.getBuildingManager()
						.getBuildingsWithoutFctNotAstro(FunctionType.EVA);
				for (Building b : buildingList) {
					FunctionType ft = b.getEmptyActivitySpotFunctionType();
					if (ft != null) {
						walkToEmptyActivitySpotInBuilding(b, allowFail);
						return;
					}
				}
			}
			// If person is in a vehicle, walk to random location within vehicle.
			else if (person.isInVehicle() && (person.getVehicle() instanceof Rover rover)) {
				walkToRandomLocInRover(rover, allowFail);
			}
		} 
		else {
			// If robot is in a settlement, walk to random building.
			if (robot.isInSettlement()) {
				walkToAssignedDutyLocation(robot, false);
			}
		}
	}

	/**
	 * Walks to the assigned duty location.
	 * 
	 * @param robot
	 * @param allowFail
	 */
	protected boolean walkToAssignedDutyLocation(Robot robot, boolean allowFail) {
		
		if (robot.isInSettlement()) {
			return walkToASpot(robot, allowFail, BuildingManager.getBuilding(robot), 
					FunctionType.getDefaultFunction(robot.getRobotType()));
		}
		
		return false;
	}

	/**
	 * Walks to a robotic station.
	 * 
	 * @param robot
	 * @param allowFail
	 * @return
	 */
	protected boolean walkToRoboticStation(Robot robot, boolean allowFail) {
	
		if (robot.isInSettlement()) {
			return walkToASpot(robot, allowFail, BuildingManager.getBuilding(robot), 
					FunctionType.ROBOTIC_STATION);
		}
		
		return false;
	}
	
	/**
	 * Walks to a spot.
	 * 
	 * @param robot
	 * @param allowFail
	 * @param currentBuilding
	 * @param functionType
	 * @return
	 */
	public boolean walkToASpot(Robot robot, boolean allowFail, Building currentBuilding, FunctionType functionType) {
		boolean canWalk = false;
		
		if (currentBuilding != null) {
			canWalk = walkToActivitySpotInBuilding(currentBuilding, functionType, allowFail);
		}
		
		if (!canWalk) {
			List<Building> buildingList = robot.getSettlement().getBuildingManager()
					.getBuildingsNoHallwayTunnelObservatory(functionType);

			for (Building b: buildingList) {
				canWalk = walkToActivitySpotInBuilding(b, functionType, allowFail);
				if (canWalk)
					return true;
			}
		}
		
		return canWalk;
	}
	
	/**
	 * Creates a walk to an interior position in a building or vehicle.
	 * 
	 * @Note: need to ensure releasing the old activity spot prior to calling this method
	 * and take in the new activity spot after this method.
	 * @param interiorObject the destination interior object.
	 * @param sLoc the settlement local position destination.
	 * @param allowFail true if walking is allowed to fail.
	 * @param needEVA
	 */
	public boolean createWalkingSubtask(LocalBoundedObject interiorObject, LocalPosition sLoc, 
			boolean allowFail, boolean needEVA) {
		// Check my own position
		LocalPosition myLoc = worker.getPosition();

		if (myLoc.equals(sLoc)) {
			// May add back checking: logger.info(worker, 4_000, "Already at the spot and no need to walk further.")
			return true;
		}
		
		Walk walkingTask = Walk.createWalkingTask(worker, sLoc, interiorObject, needEVA);

		if (walkingTask != null) {
			// Add subtask for walking to destination.
			addSubTask(walkingTask);
			
			return true;
		}
		else {
			if (!allowFail) {
				logger.log(worker, Level.INFO, 4_000, "Failed to walk to " + interiorObject + ".");
				// Does it still need to call endTask() ?
				endTask();
			} else {
				logger.log(worker, Level.INFO, 4_000, "Unable to walk to " + interiorObject + ".");
			}
		}

		return false;
	}
	

	
	/**
	 * Sets the standard pulse time.
	 * 
	 * @param value
	 */
	public static void setStandardPulseTime(double value) {
		 standardPulseTime = value;
	}

	/**
	 * Gets the standard pulse time.
	 * 
	 * @param value
	 */
	public static double getStandardPulseTime() {
		 return standardPulseTime;
	}
	
	/**
	 * Reinitializes instances.
	 */
	public void reinit() {
		person = unitManager.getPersonByID(id);
		robot = unitManager.getRobotByID(id);

		if (person != null) {
			worker = person;
			eventTarget = person;
		} else {
			worker = robot;
			eventTarget = robot;
		}

		if (teacherID != null && (!teacherID.equals(Integer.valueOf(-1)) || !teacherID.equals(Integer.valueOf(0))))
			teacher = unitManager.getPersonByID(teacherID);
		
		if (subTask != null)
			subTask.reinit();
	}

	/**
	 * Gets the simulation underpinning run.
	 */
	protected Simulation getSimulation() {
		return sim;
	}

	/**
	 * Gets the current Martian time.
	 */
	protected static MarsTime getMarsTime() {
		return masterClock.getMarsTime();
	}

	/**
	 * Is this Task interruptable? This method should be overridden by Task that can not
	 * be interrupted.
	 * 
	 * @return Returns true by default
	 */
	public boolean isInterruptable() {
        return true;
    }

	/**
	 * Reloads instances after loading from a saved sim.
	 * 
	 * @param s
	 * @param pc  {@link PersonConfig}
	 */
	static void initializeInstances(Simulation s, PersonConfig pc) {
		sim = s;
		masterClock = s.getMasterClock();
				
		// Set standard pulse time to a quarter of the value of the current pulse width
		setStandardPulseTime(Math.min(masterClock.geTaskPulseWidth(), masterClock.getLeadPulseTime()));
		
		eventManager = s.getEventManager();
		unitManager = s.getUnitManager();
		scientificStudyManager = s.getScientificStudyManager();
		surfaceFeatures = s.getSurfaceFeatures();
		orbitInfo = s.getOrbitInfo();
		missionManager = s.getMissionManager();
		personConfig = pc;
	}
	
	public void destroy() {
		teacher = null;
		person = null;
		robot = null;
		worker = null;
		eventTarget = null;
		subTask.destroy();
		subTask = null;
		phase = null;
	}

}

