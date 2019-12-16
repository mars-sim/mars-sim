/**
 * Mars Simulation Project
 * Task.java
 * @version 3.1.0 2018-01-01
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.utils;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.mars.TerrainElevation;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * The Task class is an abstract parent class for tasks that allow people to do
 * various things. A person's TaskManager keeps track of one current task for
 * the person, but a task may use other tasks internally to accomplish things.
 */
public abstract class Task implements Serializable, Comparable<Task> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(Task.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());
	
	private static final double JOB_STRESS_MODIFIER = .5D;
	// if that task is an a.i. task within a person's job, then the stress effect is
	// 1/2
	private static final double SKILL_STRESS_MODIFIER = .1D;

	protected static final int FIRST_ITEM_RESOURCE_ID = ResourceUtil.FIRST_ITEM_RESOURCE_ID;

	protected static final int FIRST_EQUIPMENT_RESOURCE_ID = ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID;

	private static final double SMALL_AMOUNT_OF_TIME = 0.00111D;
	
	// Data members
	/** True if task is finished. */
	private boolean done;
	/** True if task has a time duration. */
	protected boolean hasDuration;
	/** Is this task effort driven. */
	protected boolean effortDriven;
	/** Task should create Historical events. */
	private boolean createEvents;
	/** Amount of time required to complete current phase. (in millisols) */
	protected double phaseTimeRequired;
	/** Amount of time completed on the current phase. (in millisols) */
	protected double phaseTimeCompleted;
	/** Stress modified by person performing task per millisol. */
	protected double stressModifier;
	/** The time duration (in millisols) of the task. */
	private double duration;
	/** The current amount of time spent on the task (in millisols). */
	private double timeCompleted;

	/** The name of the task. */
	private String name = "";
	/** Description of the task. */
	private String description = "";

	/** The person teaching this task if any. */
	private Person teacher;
	/** The person performing the task. */
	protected Person person;
	/** The robot performing the task. */
	protected Robot robot;
	/** Sub-task of the sub task. */
	protected Task subTask;
	/** Phase of task completion. */
	private TaskPhase phase;
	/** The person's physical condition. */
	private PhysicalCondition condition;
//	/** FunctionType of the task. */
	// private FunctionType functionType;

	/** A collection of the task's phases. */
	private Collection<TaskPhase> phases;

	public static Simulation sim = Simulation.instance();
	/** The static instance of the mars clock*/	
	protected static MarsClock marsClock;
	/** The static instance of the event manager */
	public static HistoricalEventManager eventManager;
	/** The static instance of the relationship manager */
	public static RelationshipManager relationshipManager;// = sim.getRelationshipManager();
	/** The static instance of the UnitManager */	
	protected static UnitManager unitManager;// = sim.getUnitManager();
	/** The static instance of the ScientificStudyManager */
	protected static ScientificStudyManager scientificStudyManager;// = sim.getScientificStudyManager();
	/** The static instance of the SurfaceFeatures */
	protected static SurfaceFeatures surfaceFeatures;// = sim.getMars().getSurfaceFeatures();
	/** The static instance of the MissionManager */
	protected static MissionManager missionManager;// = sim.getMissionManager();
	/** The static instance of the personConfig */
	protected static PersonConfig personConfig = SimulationConfig.instance().getPersonConfig();
	/** The static instance of the TerrainElevation */
	protected static TerrainElevation terrainElevation;
	
	/**
	 * Constructs a Task object.
	 * 
	 * @param name           the name of the task
	 * @param person         the person performing the task
	 * @param effort         Does this task require physical effort
	 * @param createEvents   Does this task create events?
	 * @param stressModifier stress modified by person performing task per millisol.
	 * @param hasDuration    Does the task have a time duration?
	 * @param duration       the time duration (in millisols) of the task (or 0 if
	 *                       none)
	 */
	public Task(String name, Unit unit, boolean effort, boolean createEvents, double stressModifier,
			boolean hasDuration, double duration) {

		this.name = name;
		this.description = name;
		this.effortDriven = effort;
		this.createEvents = createEvents;
		this.stressModifier = stressModifier;
		this.hasDuration = hasDuration;
		this.duration = duration;

//		eventManager = sim.getEventManager();
//		relationshipManager = sim.getRelationshipManager();

		Person person = null;
		Robot robot = null;

		if (unit instanceof Person) {
			person = (Person) unit;
			this.person = person;
			condition = person.getPhysicalCondition();
		} else if (unit instanceof Robot) {
			robot = (Robot) unit;
			this.robot = robot;
		}

		done = false;

		timeCompleted = 0D;
		
		phase = null;
		phases = new ArrayList<TaskPhase>();
		
		// For sub task
		setSubTaskPhase(null);
		if (subTask != null)  {
//			subTask.setDescription("");
			subTask = null;
		}
	}

	/**
	 * Ends the task and performs any final actions.
	 */
	public void endTask() {
//    	if (!toString().contains("Walk"))
//    		logger.info("Called " + this + "'s super.endTask().");
		// End subtask.
		if (subTask != null && !subTask.isDone()) {
			setSubTaskPhase(null);
//			subTask.setDescription("");
			subTask.destroy();		
			subTask = null;
		}
	
		// Set done to true
		done = true;

		if (person != null) { 
			// Note: need to avoid java.lang.StackOverflowError when calling PersonTableModel.unitUpdate()
	        person.fireUnitUpdate(UnitEventType.TASK_ENDED_EVENT, this); 
		}
		else if (robot != null) {
			// Note: need to avoid java.lang.StackOverflowError when calling PersonTableModel.unitUpdate()
			robot.fireUnitUpdate(UnitEventType.TASK_ENDED_EVENT, this);
		}

		// Create ending task historical event if needed.
		if (createEvents) {

			TaskEvent endingEvent = null;

			if (person != null) {
				endingEvent = new TaskEvent(person, this, person, EventType.TASK_FINISH,
						person.getLocationTag().getExtendedLocations(), "");
			} else if (robot != null) {
				endingEvent = new TaskEvent(robot, this, robot, EventType.TASK_FINISH,
						robot.getLocationTag().getExtendedLocations(), "");
			}

			eventManager.registerNewEvent(endingEvent);
		}
		
//		if (person != null) { 
//			person.getMind().getTaskManager().endCurrentTask();
//		}
	}

	/**
	 * Return the value of the effort driven flag.
	 * 
	 * @return Effort driven.
	 */
	public boolean isEffortDriven() {
		return effortDriven;
	}

	/**
	 * Returns the name of the task.
	 * 
	 * @return the task's name
	 */
	public String getName() {
		return getName(true);
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
	 * Gets the task name
	 * 
	 * @return the task's name in String.
	 */
	public String getTaskName() {
		return this.getClass().getSimpleName();

	}

	/**
	 * Sets the task's name.
	 * 
	 * @param name the task name.
	 */
	protected void setName(String name) {
		this.name = name;
		if (person != null) {
			person.fireUnitUpdate(UnitEventType.TASK_NAME_EVENT, name);
		} else if (robot != null) {
			robot.fireUnitUpdate(UnitEventType.TASK_NAME_EVENT, name);
		}
	}

	/**
	 * Returns a string that is a description of what the task is currently doing.
	 * This is mainly for user interface purposes. Derived tasks should extend this
	 * if necessary. Defaults to just the name of the task.
	 * 
	 * @return the description of what the task is currently doing
	 */
	public String getDescription() {
//		if ((subTask != null) && !subTask.done) {
//			return subTask.getDescription();
//		} else {
			return description;
//		}
	}

	/**
	 * Gets the description of the task.
	 * 
	 * @param allowSubtask true if subtask description should be used.
	 * @return the task description.
	 */
	public String getDescription(boolean allowSubtask) {
		if (allowSubtask && (subTask != null) && !subTask.done) {
			return subTask.getDescription();
		} else {
			return description;
		}
	}

	/**
	 * Sets the task's description.
	 * 
	 * @param description the task description.
	 */
	protected void setDescription(String des) {
//		if (des != null && !des.equals("") && !des.equals(description)) {
			description = des;
			if (person != null) {
				person.fireUnitUpdate(UnitEventType.TASK_DESCRIPTION_EVENT, des);
			} else if (robot != null) {
				robot.fireUnitUpdate(UnitEventType.TASK_DESCRIPTION_EVENT, des);
			}
//		}
	}

//    public FunctionType getFunction() {
//        if (subTask != null && !subTask.done) {// && subTask.getFunction() != FunctionType.UNKNOWN) {
//            return subTask.getFunction();
//        }
//        else {
//            return functionType;
//        }
//    }
//    
//    public FunctionType getFunction(boolean allowSubtask) {
//        if (allowSubtask && subTask != null && !subTask.done) { // && subTask.getFunction() != FunctionType.UNKNOWN) {
//            return subTask.getFunction();
//        }
//        else {
//            return functionType;
//        }
//    }
//    
//    protected void setFunction(FunctionType type) {
//        if (!this.functionType.equals(type)) {
//            this.functionType = type;
//            if (person != null) {
//                person.fireUnitUpdate(UnitEventType.TASK_DESCRIPTION_EVENT, type);
//            }
//            else if (robot != null) {
//                robot.fireUnitUpdate(UnitEventType.TASK_DESCRIPTION_EVENT, type);
//            }
//        }
//    }

	/**
	 * Returns a boolean whether this task should generate events
	 * 
	 * @return boolean flag.
	 */
	public boolean getCreateEvents() {
		return createEvents;
	}

	/**
	 * Gets a string of the current phase of the task.
	 * 
	 * @return the current phase of the task
	 */
	public TaskPhase getPhase() {
		// TODO: should it checks for subtask's phase first ?
//		if ((subTask != null) && !subTask.done && subTask.getPhase() != null) {
//			return subTask.getPhase();
//		}
		return phase;
	}

	public void setSubTaskPhase(TaskPhase newPhase) {
		if (subTask != null) {
			subTask.setPhase(newPhase);
//			if (person != null) {
//				person.fireUnitUpdate(UnitEventType.TASK_SUBTASK_EVENT, newPhase);
//			} else if (robot != null) {
//				robot.fireUnitUpdate(UnitEventType.TASK_SUBTASK_EVENT, newPhase);
//			}
		}
	}

	/**
	 * Sets the task's current phase.
	 * 
	 * @param newPhase the phase to set the a task at.
	 * @throws Exception if newPhase is not in the task's collection of phases.
	 */
	protected void setPhase(TaskPhase newPhase) {
		phase = newPhase;
//		System.out.println("phases is " + phases);
		// e.g. phases is [Walking inside a Settlement, Walking inside a Rover, Walking outside, Exiting Airlock, Entering Airlock, Exiting Rover In Garage, Entering Rover In Garage]
//		if (newPhase == null) {
//			throw new IllegalArgumentException("newPhase is null");
//			endTask();
//		} 		
		if (newPhase != null && phases != null && !phases.isEmpty() && phases.contains(newPhase)) {

			if (person != null) {
				// Note: need to avoid java.lang.StackOverflowError when calling
				// PersonTableModel.unitUpdate()
				person.fireUnitUpdate(UnitEventType.TASK_PHASE_EVENT, newPhase);
			} else if (robot != null) {
				// Note: need to avoid java.lang.StackOverflowError when calling
				// PersonTableModel.unitUpdate()
				robot.fireUnitUpdate(UnitEventType.TASK_PHASE_EVENT, newPhase);
			}
		} 
		
//		else {
//			throw new IllegalStateException("newPhase: " + newPhase + " is not a valid phase for this task.");
//		}
	}

	
	/**
	 * Gets a string of the current phase of the task.
	 * 
	 * @return the current phase of the task
	 */
	public TaskPhase getMainTaskPhase() {
		return phase;
	}

	/**
	 * Gets a string of the current phase of this task, ignoring subtasks.
	 * 
	 * @return the current phase of this task.
	 */
	public TaskPhase getTopPhase() {
		return phase;
	}

	/**
	 * Adds a phase to the task's collection of phases.
	 * 
	 * @param newPhase the new phase to add.
	 */
	protected void addPhase(TaskPhase newPhase) {
		if (newPhase == null) {
			throw new IllegalArgumentException("newPhase is null");
		} else if ((phases != null && (phases.isEmpty() || !phases.contains(newPhase)))) {
			phases.add(newPhase);
		}
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
	 * Adds a new sub-task.
	 * 
	 * @param newSubTask the new sub-task to be added
	 */
	public void addSubTask(Task newSubTask) {
		if (subTask != null) {
//			subTask.setDescription("");
			setSubTaskPhase(null);
			subTask.destroy();
			subTask = null;
			createSubTask(newSubTask);
			
		} else {
			createSubTask(newSubTask);
		}
	}

	/**
	 * Create a new sub-task.
	 * 
	 * @param newSubTask the new sub-task to be added
	 */
	public void createSubTask(Task newSubTask) {
		subTask = newSubTask;
//		subTask.setDescription(newSubTask.getDescription());
		if (person != null) {
			person.fireUnitUpdate(UnitEventType.TASK_SUBTASK_EVENT, newSubTask);
		} else if (robot != null) {
			robot.fireUnitUpdate(UnitEventType.TASK_SUBTASK_EVENT, newSubTask);
		}
	}
	
	/**
	 * Gets the task's subtask. Returns null if none
	 * 
	 * @return subtask
	 */
	public Task getSubTask() {
		return subTask;
	}

	/**
	 * Perform the task for the given number of seconds. Children should override
	 * and implement this.
	 * 
	 * @param time amount of time (millisol) given to perform the task (in
	 *             millisols)
	 * @return amount of time (millisol) remaining after performing the task (in
	 *         millisols)
	 * @throws Exception if error performing task.
	 */
	public double performTask(double time) {
		double timeLeft = time;
		if (subTask != null) {
			if (subTask.isDone()) {
				setSubTaskPhase(null);
//				subTask.setDescription("");
				subTask.destroy();
				subTask = null;
			} else {
				timeLeft = subTask.performTask(timeLeft);
			}
		}

		// If no subtask, perform this task.
		if ((subTask == null) || subTask.isDone()) {

			if (person != null) {
				// If task is effort-driven and person is incapacitated, end task.
				if (effortDriven && (person.getPerformanceRating() == 0D)) {
					// "Resurrect" him a little to give him a chance to make amend
					condition.setPerformanceFactor(.1);
					endTask();
				} else {
					timeLeft = executeMappedPhase(timeLeft, time);
				}
				
				if (time - timeLeft > time / 8D) //SMALL_AMOUNT_OF_TIME)
					// Modify stress performing task.
					modifyStress(time - timeLeft);
				
			}

			else if (robot != null) {
				// If task is effort-driven and person is incapacitated, end task.
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
	 * Execute the mapped phase repeatedly 
	 * 
	 * @param timeLeft
	 * @param time
	 * @return
	 */
	private double executeMappedPhase(double timeLeft, double time) {
		// Perform phases of task until time is up or task is done.
		while ((timeLeft > 0D) && !done
			&& getPhase() != null
			&& ((subTask == null) || subTask.done)) {
			
			if (hasDuration) {
				// Keep track of the duration of the task.
				if ((timeCompleted + timeLeft) > duration) {
					double performTime = duration - timeCompleted;
					double extraTime = timeCompleted + timeLeft - duration;
//					logger.info("performTime: " + Math.round(performTime*1000.0)/1000.0
//								+ "  duration: " + Math.round(duration*1000.0)/1000.0
//								+ "  timeCompleted: " + Math.round(timeCompleted*1000.0)/1000.0
//								+ "  extraTime: " + Math.round(extraTime*1000.0)/1000.0
//								);
					timeLeft = performMappedPhase(performTime) + extraTime;
					timeCompleted = duration;
					// NOTE: does endTask() cause Sleep task to unncessarily end and restart ? 
					endTask();
				} else {
					double remainingTime = timeLeft;
//					if (getPhase() != null)
					timeLeft = performMappedPhase(timeLeft);
					timeCompleted += remainingTime;
				}
			} else {
				timeLeft = performMappedPhase(timeLeft);
			}
		}
		
		
		return timeLeft;
	}
	
	/**
	 * Performs the method mapped to the task's current phase.
	 * 
	 * @param time the amount of time (millisol) the phase is to be performed.
	 * @return the remaining time (millisol) after the phase has been performed.
	 * @throws Exception if error in performing phase or if phase cannot be found.
	 */
	protected abstract double performMappedPhase(double time);

	/**
	 * SHould the start of this task create an historical event.
	 * 
	 * @param create New flag value.
	 */
	protected void setCreateEvents(boolean create) {
		createEvents = create;
	}

	/**
	 * Get a string representation of this Task. It's content will consist of the
	 * description.
	 *
	 * @return Description of the task.
	 */
	public String toString() {
		return description;
	}

	/**
	 * Compare this object to another for an ordering. THe ordering is based on the
	 * alphabetic ordering of the Name attribute.
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
	 * 
	 * @param time the time performing the task.
	 */
	private void modifyStress(double time) {

		if (person != null) {
			double effectiveStressModifier = stressModifier;

			if (stressModifier > 0D) {

				Job job = person.getMind().getJob();

				if ((job != null) && job.isJobRelatedTask(getClass())) {
					effectiveStressModifier *= JOB_STRESS_MODIFIER;
				}

				// Reduce stress modifier for person's skill related to the task.
				int skill = getEffectiveSkillLevel();
				effectiveStressModifier -= (effectiveStressModifier * (double) skill * SKILL_STRESS_MODIFIER);

				// If effective stress modifier < 0, set it to 0.
				if (effectiveStressModifier < 0D) {
					effectiveStressModifier = 0D;
				}
			}

			condition.setStress(condition.getStress() + (effectiveStressModifier * time));
		}
	}

	/**
	 * Set the task's stress modifier. Stress modifier can be positive (increase in
	 * stress) or negative (decrease in stress).
	 * 
	 * @param newStressModifier stress modification per millisol.
	 */
	protected void setStressModifier(double newStressModifier) {
		this.stressModifier = newStressModifier;
	}

	/**
	 * Gets the probability modifier for a task if person needs to go to a new
	 * building.
	 * 
	 * @param person      the person to perform the task.
	 * @param newBuilding the building the person is to go to.
	 * @return probability modifier
	 * @throws BuildingException if current or new building doesn't have life
	 *                           support function.
	 */
	protected static double getCrowdingProbabilityModifier(Person person, Building newBuilding) {
		double modifier = 1D;

		Building currentBuilding = BuildingManager.getBuilding(person);

		if ((currentBuilding != null) && (newBuilding != null) && (currentBuilding != newBuilding)) {

			// Increase probability if current building is overcrowded.
			LifeSupport currentLS = currentBuilding.getLifeSupport();
			int currentOverCrowding = currentLS.getOccupantNumber() - currentLS.getOccupantCapacity();
			if (currentOverCrowding > 0) {
				modifier *= ((double) currentOverCrowding + 2);
			}

			// Decrease probability if new building is overcrowded.
			LifeSupport newLS = newBuilding.getLifeSupport();
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
	 * 
	 * @return effective skill level
	 */
	public abstract int getEffectiveSkillLevel();

	/**
	 * Gets a list of the skills associated with this task. May be empty list if no
	 * associated skills.
	 * 
	 * @return list of skills
	 */
	public abstract List<SkillType> getAssociatedSkills();

	/**
	 * Checks if someone is teaching this task to the person performing it.
	 * 
	 * @return true if teacher.
	 */
	public boolean hasTeacher() {
		return (teacher != null);
	}

	/**
	 * Gets the person teaching this task.
	 * 
	 * @return teacher or null if none.
	 */
	public Person getTeacher() {
		return teacher;
	}

	/**
	 * Sets the person teaching this task.
	 * 
	 * @param newTeacher the new teacher.
	 */
	public void setTeacher(Person newTeacher) {
		this.teacher = newTeacher;
	}

	/**
	 * Gets the experience modifier when being taught by a teacher.
	 * 
	 * @return modifier;
	 */
	protected double getTeachingExperienceModifier() {
		double result = 1D;

		if (hasTeacher()) {
			int teachingModifier = teacher.getNaturalAttributeManager().getAttribute(NaturalAttributeType.TEACHING);
			int learningModifier = 0;
			if (person != null) {
				learningModifier = person.getNaturalAttributeManager()
						.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
			} else if (robot != null) {
				learningModifier = 0;// robot.getRoboticAttributeManager().getAttribute(RoboticAttribute.ACADEMIC_APTITUDE);
			}

			result += (double) (teachingModifier + learningModifier) / 100D;
		}

		return result;
	}

	/**
	 * Gets the probability modifier for a person performing a task based on his/her
	 * relationships with the people in the room the task is to be performed in.
	 * 
	 * @param person   the person to check for.
	 * @param building the building the person will need to be in for the task.
	 * @return probability modifier
	 */
	protected static double getRelationshipModifier(Person person, Building building) {
		double result = 1D;

		if ((person == null) || (building == null)) {
			throw new IllegalArgumentException("Task.getRelationshipModifier(): null parameter.");
		} else {
			if (building.hasFunction(FunctionType.LIFE_SUPPORT)) {
				LifeSupport lifeSupport = building.getLifeSupport();
				double totalOpinion = 0D;
				Iterator<Person> i = lifeSupport.getOccupants().iterator();
				while (i.hasNext()) {
					Person occupant = i.next();
					if (person != occupant) {
						totalOpinion += ((relationshipManager.getOpinionOfPerson(person, occupant) - 50D) / 50D);
					}
				}

				if (totalOpinion >= 0D) {
					result *= (1D + totalOpinion);
				} else {
					result /= (1D - totalOpinion);
				}
			}
		}

		return result;
	}

	/**
	 * Adds experience to the person's skills used in this task.
	 * 
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected abstract void addExperience(double time);

	/**
	 * Gets the duration of the task or 0 if none.
	 * 
	 * @return duration (millisol)
	 */
	protected double getDuration() {
		return duration;
	}

	/**
	 * Sets the duration of the task
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
	 * Gets the related building function for this task. Override as necessary.
	 * 
	 * @return building function or null if none.
	 */
	public FunctionType getLivingFunction() {
		return null;
	}

	/**
	 * Gets the related building function for this task. Override as necessary.
	 * 
	 * @return building function or null if none.
	 */
	public FunctionType getRoboticFunction() {
		return null;
	}

	/**
	 * Walk to an available activity spot in a building.
	 * 
	 * @param building  the destination building.
	 * @param allowFail true if walking is allowed to fail.
	 */
	public void walkToActivitySpotInBuilding(Building building, boolean allowFail) {
		FunctionType functionType = null;

		if (person != null)
			functionType = getLivingFunction();
		else if (robot != null)
			functionType = getRoboticFunction();

		if ((functionType != null) && (building.hasFunction(functionType))) {
			walkToActivitySpotInBuilding(building, functionType, allowFail);
		} else {
			// If no available activity spot, go to random location in building.
			walkToRandomLocInBuilding(building, allowFail);
		}
	}

	/**
	 * Walks to the bed assigned for this person
	 * 
	 * @param accommodations
	 * @param person
	 * @param allowFail
	 */
	public void walkToBed(LivingAccommodations accommodations, Person person, boolean allowFail) {
		Point2D bed = person.getBed();
		Building building = accommodations.getBuilding();
		Point2D spot = LocalAreaUtil.getLocalRelativeLocation(bed.getX(), bed.getY(), building);

		if (bed != null) {
			// Create subtask for walking to destination.
			createWalkingSubtask(building, spot, allowFail);
			// Update phase description
		}
//        else {// Note ; why is it a dead code according to eclipse ?
//        	// If no available activity spot, go to random location in building.
//        	walkToActivitySpotInBuilding(building, FunctionType.LIVING_ACCOMODATIONS, allowFail);
//            //walkToRandomLocInBuilding(building, allowFail);
//        }
	}

	/**
	 * Walk to an available activity spot in a building.
	 * 
	 * @param building     the destination building.
	 * @param functionType the building function type for the activity.
	 * @param allowFail    true if walking is allowed to fail.
	 */
	public void walkToActivitySpotInBuilding(Building building, FunctionType functionType, boolean allowFail) {

		Function f = building.getFunction(functionType);
		if (f == null) {
			// If the functionType does not exist in this building, go to random location in
			// building.
//			walkToRandomLocInBuilding(building, allowFail);
			return;
		}

		Point2D settlementLoc = null;
		if (person != null) {
			// Find available activity spot in building.
			settlementLoc = f.getAvailableActivitySpot(person);
		} else if (robot != null) {
			// Find available activity spot in building.
			settlementLoc = f.getAvailableActivitySpot(robot);
		}

		if (settlementLoc != null) {
			// Create subtask for walking to destination.
			createWalkingSubtask(building, settlementLoc, allowFail);
		} 
//		else {
//			// If no available activity spot, go to random location in building.
//			walkToRandomLocInBuilding(building, allowFail);
//		}
	}

	/**
	 * Walk to a random interior location in a building.
	 * 
	 * @param building  the destination building.
	 * @param allowFail true if walking is allowed to fail.
	 */
	protected void walkToRandomLocInBuilding(Building building, boolean allowFail) {

		Point2D interiorPos = LocalAreaUtil.getRandomInteriorLocation(building);
		Point2D adjustedInteriorPos = LocalAreaUtil.getLocalRelativeLocation(interiorPos.getX(), interiorPos.getY(),
				building);

		// Create subtask for walking to destination.
		createWalkingSubtask(building, adjustedInteriorPos, allowFail);
	}

	/**
	 * Walk to an available operator activity spot in a rover.
	 * 
	 * @param rover     the rover.
	 * @param allowFail true if walking is allowed to fail.
	 */
	protected void walkToOperatorActivitySpotInRover(Rover rover, boolean allowFail) {
		walkToActivitySpotInRover(rover, rover.getOperatorActivitySpots(), allowFail);
	}

	/**
	 * Walk to an available passenger activity spot in a rover.
	 * 
	 * @param rover     the rover.
	 * @param allowFail true if walking is allowed to fail.
	 */
	protected void walkToPassengerActivitySpotInRover(Rover rover, boolean allowFail) {
		walkToActivitySpotInRover(rover, rover.getPassengerActivitySpots(), allowFail);
	}

	/**
	 * Walk to an available lab activity spot in a rover.
	 * 
	 * @param rover     the rover.
	 * @param allowFail true if walking is allowed to fail.
	 */
	protected void walkToLabActivitySpotInRover(Rover rover, boolean allowFail) {
		walkToActivitySpotInRover(rover, rover.getLabActivitySpots(), allowFail);
	}

	/**
	 * Walk to an available sick bay activity spot in a rover.
	 * 
	 * @param rover     the rover.
	 * @param allowFail true if walking is allowed to fail.
	 */
	protected void walkToSickBayActivitySpotInRover(Rover rover, boolean allowFail) {
		walkToActivitySpotInRover(rover, rover.getSickBayActivitySpots(), allowFail);
	}

	/**
	 * Walk to an available activity spot in a rover from a list of activity spots.
	 * 
	 * @param rover         the rover.
	 * @param activitySpots list of activity spots.
	 * @param allowFail     true if walking is allowed to fail.
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
	 * 
	 * @param rover        the rover.
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
		} else if (robot != null) {
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
	 * 
	 * @param rover        the destination rover.
	 * @param activitySpot the activity spot as a Point2D object.
	 * @param allowFail    true if walking is allowed to fail.
	 */
	private void walkToActivitySpotInRover(Rover rover, Point2D activitySpot, boolean allowFail) {

		if (activitySpot != null) {

			// Create subtask for walking to destination.
			createWalkingSubtask(rover, activitySpot, allowFail);
		} else {

			// Walk to a random location in the rover.
			walkToRandomLocInRover(rover, allowFail);
		}
	}

	/**
	 * Walk to a random interior location in a rover.
	 * 
	 * @param rover     the destination rover.
	 * @param allowFail true if walking is allowed to fail.
	 */
	protected void walkToRandomLocInRover(Rover rover, boolean allowFail) {

		Point2D interiorPos = LocalAreaUtil.getRandomInteriorLocation(rover);
		Point2D adjustedInteriorPos = LocalAreaUtil.getLocalRelativeLocation(interiorPos.getX(), interiorPos.getY(),
				rover);

		// Create subtask for walking to destination.
		createWalkingSubtask(rover, adjustedInteriorPos, allowFail);
	}

	/**
	 * Walk to a random location.
	 * 
	 * @param allowFail true if walking is allowed to fail.
	 */
	protected void walkToRandomLocation(boolean allowFail) {

		if (person != null) {
			// If person is in a settlement, walk to random building.
			if (person.isInSettlement()) {

				List<Building> buildingList = person.getSettlement().getBuildingManager()
						.getBuildings(FunctionType.LIFE_SUPPORT);

				if (buildingList.size() > 0) {
					int buildingIndex = RandomUtil.getRandomInt(buildingList.size() - 1);
					Building building = buildingList.get(buildingIndex);

					walkToRandomLocInBuilding(building, allowFail);
				}
			}
			// If person is in a vehicle, walk to random location within vehicle.
			else if (person.isInVehicle()) {

				// Walk to a random location within rover if possible.
				if (person.getVehicle() instanceof Rover) {
					walkToRandomLocInRover((Rover) person.getVehicle(), allowFail);
				}
			}
		} else if (robot != null) {
			// If robot is in a settlement, walk to random building.
			if (robot.isInSettlement()) {

//	        	List<Building> buildingList = robot.getSettlement().getBuildingManager().getBuildings(FunctionType.ROBOTIC_STATION);
//
//	            if (buildingList.size() > 0) {
//	                int buildingIndex = RandomUtil.getRandomInt(buildingList.size() - 1);
//	                Building building = buildingList.get(buildingIndex);
//	                // do not stay blocking the hallway
//	                //if (currentBuilding.getBuildingType().equals("Hallway"))
//	                	//walkToRandomLocInBuilding(building, allowFail);
//	                	//walkToRandomLocation(allowFail);
				// else
//	                	walkToRandomLocInBuilding(building, allowFail);

				walkToAssignedDutyLocation(robot, false);
//	            }
			}
			// If robot is in a vehicle, walk to random location within vehicle.
//	        else if (robot.isInVehicle()) {

			// Walk to a random location within rover if possible.
//	            if (robot.getVehicle() instanceof Rover) {
//	                walkToRandomLocInRover((Rover) robot.getVehicle(), allowFail);
//	            }
//	        }
		}
	}

	protected void walkToAssignedDutyLocation(Robot robot, boolean allowFail) {
		if (robot.isInSettlement()) {
			Building currentBuilding = BuildingManager.getBuilding(robot);

			if (currentBuilding != null) {
				RobotType type = robot.getRobotType();// .getName();
				// List<Building> buildingList;
				FunctionType fct = null;

				if (type == RobotType.CHEFBOT)// type.equals("ChefBot"))
					fct = FunctionType.COOKING;
				else if (type == RobotType.CONSTRUCTIONBOT)// type.equals("ConstructionBot"))
					fct = FunctionType.MANUFACTURE;
				else if (type == RobotType.DELIVERYBOT)// type.equals("DeliveryBot"))
					fct = FunctionType.ROBOTIC_STATION;
				else if (type == RobotType.GARDENBOT)// type.equals("GardenBot"))
					fct = FunctionType.FARMING;
				else if (type == RobotType.MAKERBOT)// type.equals("MakerBot"))
					fct = FunctionType.MANUFACTURE;
				else if (type == RobotType.MEDICBOT)// type.equals("MedicBot"))
					fct = FunctionType.MEDICAL_CARE;
				else if (type == RobotType.REPAIRBOT)// type.equals("RepairBot"))
					fct = FunctionType.ROBOTIC_STATION;
				else
					fct = FunctionType.ROBOTIC_STATION;
//	    		if (fct == null)
//	    			fct = FunctionType.LIVING_ACCOMODATIONS;

//	       		if (fct == null)
//	    			fct = FunctionType.LIFE_SUPPORT;

				// Added debugging statement below
				if (currentBuilding.getBuildingManager() == null)
					throw new IllegalStateException("currentBuilding.getBuildingManager() is null");
				if (currentBuilding.getBuildingManager().getBuildings(fct) == null)
					throw new IllegalStateException("currentBuilding.getBuildingManager().getBuildings(fct) is null");

				List<Building> buildingList = currentBuilding.getBuildingManager().getBuildings(fct);

				// Filter off hallways and tunnels
				buildingList = buildingList.stream().filter(b -> !b.getBuildingType().toLowerCase().equals("hallway")
						&& !b.getBuildingType().toLowerCase().equals("tunnel")).collect(Collectors.toList());

				if (buildingList.size() > 0) {
					int buildingIndex = RandomUtil.getRandomInt(buildingList.size() - 1);

					Building building = buildingList.get(buildingIndex);

					if (building.getNickName().toLowerCase().contains("astronomy")) {
						if (robot.getSettlement().getBuildingConnectors(building).size() > 0) {
							LogConsolidated.log(Level.FINER, 5000, sourceName,
									"[" + robot.getLocationTag().getLocale() + "] " 
											+ robot.getName() + " is walking toward " + building.getNickName());
							walkToActivitySpotInBuilding(building, fct, allowFail);
						}
					} else {
//		                logger.info(robot.getNickName() + " is walking toward " + building.getNickName());
						walkToActivitySpotInBuilding(building, fct, allowFail);
					}
				}
			}
		}
	}

	/**
	 * Create a walk to an interior position in a building or vehicle.
	 * 
	 * @param interiorObject the destination interior object.
	 * @param settlementPos  the settlement local position destination.
	 * @param allowFail      true if walking is allowed to fail.
	 */
	private void createWalkingSubtask(LocalBoundedObject interiorObject, Point2D settlementPos, boolean allowFail) {

		if (person != null) {
			if (Walk.canWalkAllSteps(person, settlementPos.getX(), settlementPos.getY(), 0, interiorObject)) {

				// Add subtask for walking to destination.
				addSubTask(new Walk(person, settlementPos.getX(), settlementPos.getY(), 0, interiorObject));
			} else {

				if (!allowFail) {
					LogConsolidated.log(Level.INFO, 0, sourceName,
							"[" + person.getLocationTag().getLocale() + "] " 
									+ person.getName() + " ended the task of walking to " + interiorObject);
					endTask();
				}
				else {
					LogConsolidated.log(Level.INFO, 0, sourceName,
							"[" + person.getLocationTag().getLocale() + "] " 
									+ person.getName() + " was unable to walk to " + interiorObject);
				}
			}
		} else if (robot != null) {
			if (Walk.canWalkAllSteps(robot, settlementPos.getX(), settlementPos.getY(), 0, interiorObject)) {
				// Add subtask for walking to destination.
				addSubTask(new Walk(robot, settlementPos.getX(), settlementPos.getY(), 0, interiorObject));
			} else {
				if (!allowFail) {
					LogConsolidated.log(Level.INFO, 0, sourceName,
							"[" + robot.getLocationTag().getLocale() + "] " 
									+ robot.getName() + " ended the task of walking to " + interiorObject);
					endTask();
				}
				else {
					LogConsolidated.log(Level.INFO, 0, sourceName,
							"[" + robot.getLocationTag().getLocale() + "] " 
									+ robot.getName() + " was unable to walk to " + interiorObject);
				}
			}
		}
	}
	
	/**
	 * Gets the hash code for this object.
	 * 
	 * @return hash code.
	 */
	public int hashCode() {
		return name.hashCode();
	}
	
    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof Task) &&
                ((Task)obj).getName().equals(name)) {
            return true;
        }
        return false;
    }
    
	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param c {@link MarsClock}
	 * @param e {@link HistoricalEventManager}
	 * @param r {@link RelationshipManager}
	 * @param u {@link UnitManager}
	 * @param s {@link ScientificStudyManager}
	 * @param sf {@link SurfaceFeatures}
	 * @param m {@link MissionManager}
	 */
	public static void initializeInstances(MarsClock c, HistoricalEventManager e, RelationshipManager r, 
			UnitManager u, ScientificStudyManager s, SurfaceFeatures sf, MissionManager m, PersonConfig pc) {
		sim = Simulation.instance();
		marsClock = c;
		eventManager = e;
		relationshipManager = r;
		unitManager = u;
		scientificStudyManager = s;
		surfaceFeatures = sf;
		missionManager = m;
		personConfig = pc;
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
//		phases.clear();
		condition = null;
		phases = null;
	}
}