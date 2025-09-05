/*
 * Mars Simulation Project
 * Teach.java
 * @date 2022-09-01
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

import com.mars_sim.core.UnitType;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.LifeSupport;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.MetaTaskUtil;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Crewable;

/**
 * This is a task for teaching a student a task.
 */
public class Teach extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static SimLogger logger = SimLogger.getLogger(Teach.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.teach"); //$NON-NLS-1$
	
	/** Task phases. */
	private static final TaskPhase TEACHING = new TaskPhase(Msg.getString("Task.phase.teaching")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.1D;

	/**
	 * The improvement in relationship opinion of the teacher from the student per
	 * millisol.
	 */
	private static final double BASE_RELATIONSHIP_MODIFIER = .2D;

	// Data members
	private Person student;
	private Task teachingTask;
	private SkillType taskSkill;

	/**
	 * Constructor.
	 * 
	 * @param worker the worker performing the task.
	 */
	public Teach(Worker worker) {
		super(NAME, worker, false, false, STRESS_MODIFIER, null, 10, 10);

		setTeacher(worker);
		
		// Assume the student is a person.
		Collection<Person> candidates = getBestStudents(worker);
		List<Person> students = new ArrayList<>();

		Iterator<Person> i = candidates.iterator();
		while (i.hasNext()) {
			Person candidate = i.next();
			logger.log(worker, Level.FINE, 4_000, "Connecting with student " + candidate.getName() + ".");
			students.add(candidate);
		}
		
		if (!students.isEmpty()) {
			Iterator<Person> ii = students.iterator();
			while (ii.hasNext() && teachingTask == null && student == null) {
				Person candidate = ii.next();
			
				// Gets the task the student is doing
				Task candidateTask = candidate.getMind().getTaskManager().getTask();
				MetaTask metaTask = MetaTaskUtil.getMetaTypeFromTask(candidateTask);
				if (metaTask == null) {
					// Some tasks don't have a MetaTask because they are explicitly
					// created, e.g. Negotiate Trade
					continue;
				}
				if (worker.getUnitType() == UnitType.ROBOT) {
					Set<RobotType> robotTypes = metaTask.getPreferredRobot();
					RobotType rt = ((Robot)worker).getRobotType();
					if (!robotTypes.contains(rt)) {
						continue;
					}

				}
				else {
					JobType jobType = ((Person)worker).getMind().getJob();
					
					Set<JobType> jobs = metaTask.getPreferredJob();
					if (!jobs.contains(jobType)) {
						// this task is not a part of this person's job
						// Note: may need to relax on this criteria
						continue;
					}
				
				}
								
				Set<SkillType> taughtSkills = candidateTask.getAssociatedSkills();
		        if (taughtSkills == null) {
		        	logger.severe(worker, 20_000L, "No taught skills found.");
		        	continue;
		        }
		        
		        if (!taughtSkills.isEmpty()) {
		        	Iterator<SkillType> iii = taughtSkills.iterator();
					while (ii.hasNext() && teachingTask == null && student == null) {
						SkillType candidateSkill = iii.next();

						double teacherExp = worker.getSkillManager().getCumulativeExperience(candidateSkill);
						double studentExp = candidate.getSkillManager().getCumulativeExperience(candidateSkill);
						double diff = teacherExp - studentExp;
		
						if (diff > 0) {
							teachingTask = candidateTask;
							taskSkill = candidateSkill;
							student = candidate;
							
							setDescription(
								Msg.getString("Task.description.teach.detail", 
										teachingTask.getName(false), student.getName())); // $NON-NLS-1$								
						}
						else {
							// This person has more exp points than the teacher. Go to next 
						}							
					}
		        }
			}
		}

		if (teachingTask != null && student != null) {
			// Initialize phase
			setPhase(TEACHING);
		}
		else {
			logger.fine(worker, 10_000L, "Can't find a student.");
			endTask();
		}
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (TEACHING.equals(getPhase())) {
			return teachingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the teaching phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double teachingPhase(double time) {

		if (worker.isInSettlement()) {
			// If in settlement, move teacher to the building where student is in.
			Building studentBuilding = BuildingManager.getBuilding(student);

			if (studentBuilding != null && 
				studentBuilding.getCategory() != BuildingCategory.EVA) {
				// Walk to random location in student's building.
				walkToEmptyActivitySpotInBuilding(BuildingManager.getBuilding(student), false);
			}
		}

		// Check if task is finished.
		if (teachingTask != null && teachingTask.isDone())
			endTask();
		
    	if (getTimeCompleted() + time > getDuration())
        	endTask();
    	
    	if (worker.getUnitType() == UnitType.PERSON) {
//    		if (person.isSuperUnfit()) {
//    			endTask();
//    		}
    		// Add relationship modifier for opinion of teacher from the student.
    		addRelationshipModifier(time);
    	}

        // Add experience points
        addExperience(time);
        
		return 0D;
	}

	/**
	 * Adds a relationship modifier for the student's opinion of the teacher.
	 * 
	 * @param time the time teaching.
	 */
	private void addRelationshipModifier(double time) {
        RelationshipUtil.changeOpinion(student, person, BASE_RELATIONSHIP_MODIFIER * time);
	}

	@Override
	protected void addExperience(double time) {
        // Add experience to associated skill.
        // (1 base experience point per 100 millisols of time spent)
        double exp = time / 100;

        // Experience points adjusted by person's "Experience Aptitude" attribute.
        double mod = getTeachingExperienceModifier() * 90;
        exp *= mod;

        if (teachingTask == null)  {
        	logger.severe(worker, 20_000L, "teachingTask is null.");
        	return;
        }

        Set<SkillType> taughtSkills = teachingTask.getAssociatedSkills();
        if (taughtSkills == null) {
        	logger.severe(worker, 20_000L, "No taught skills found.");
        	return;
        }

        if (!taughtSkills.isEmpty()) {

			int studentSkill = student.getSkillManager().getSkillLevel(taskSkill);
			double studentExp = student.getSkillManager().getCumulativeExperience(taskSkill);

			int teacherSkill = worker.getSkillManager().getSkillLevel(taskSkill);
			double teacherExp = worker.getSkillManager().getCumulativeExperience(taskSkill);
	
			int points = teacherSkill - studentSkill;
			double reward = exp / 60 * RandomUtil.getRandomDouble(1);
			double learned = (.05 + points) * exp / 2 * RandomUtil.getRandomDouble(1);
			
			double diff = Math.round((teacherExp - studentExp)*10.0)/10.0;

	        // If the student has more experience points than the teacher, the teaching session ends.
	        if (diff < 0) {
	        	endTask();
	        }
	        else {
//				logger.info("On task " + taskSkill.getName() 
//						+ "   diff: " + diff
//						+ "   mod: " + mod
//						+ "   " + worker + " [Lvl " + teacherSkill + "]'s teaching reward: " + Math.round(reward*1000.0)/1000.0 
//						+ "   " + student + " [Lvl " + studentSkill + "]'s learned: " + Math.round(learned*1000.0)/1000.0 + ".");
				// Add exp to student
				student.getSkillManager().addExperience(taskSkill, learned, time);
				// Add exp to teacher
				worker.getSkillManager().addExperience(taskSkill, reward, time);
	        }
		}
	}

	/**
	 * Gets a collection of the best students the teacher can teach.
	 * 
	 * @param teacher the teacher looking for students.
	 * @return collection of the best students
	 */
	public static Collection<Person> getBestStudents(Worker teacher) {
		Collection<Person> result = new ConcurrentLinkedQueue<>();
		Collection<Person> students = getTeachableStudents(teacher);

		// If teacher is in a settlement, best students are in least crowded buildings.
		Collection<Person> leastCrowded = new ConcurrentLinkedQueue<>();
		if (teacher.isInSettlement()) {
			// Find the least crowded buildings that teachable students are in.
			int crowding = Integer.MAX_VALUE;
			Iterator<Person> i = students.iterator();
			while (i.hasNext()) {
				Person student = i.next();
				Building building = BuildingManager.getBuilding(student);
				if (building != null) {
					// If this is an EVA airlock
					if (building.getCategory() == BuildingCategory.EVA) {
						// Go to the next building
						continue;
					}
						
					// If this building/hallway is next to the observatory
					if (building.getSettlement().getBuildingManager().isObservatoryAttached(building)) {
						// Go to the next building
						continue;
					}
				
					LifeSupport lifeSupport = building.getLifeSupport();
					int buildingCrowding = lifeSupport.getOccupantNumber() - lifeSupport.getOccupantCapacity() + 1;
					if (buildingCrowding < -1) {
						buildingCrowding = -1;
					}
					if (buildingCrowding < crowding) {
						crowding = buildingCrowding;
					}
				}
			}

			// Add students in least crowded buildings to result.
			Iterator<Person> j = students.iterator();
			while (j.hasNext()) {
				Person student = j.next();
				Building building = BuildingManager.getBuilding(student);
				if (building != null) {
					// If this is an EVA airlock
					if (building.getCategory() == BuildingCategory.EVA) {
						// Go to the next building
						continue;
					}
						
					// If this building/hallway is next to the observatory
					if (building.getSettlement().getBuildingManager().isObservatoryAttached(building)) {
						// Go to the next building
						continue;
					}
					
					LifeSupport lifeSupport = building.getLifeSupport();
					int buildingCrowding = lifeSupport.getOccupantNumber() - lifeSupport.getOccupantCapacity() + 1;
					if (buildingCrowding < -1) {
						buildingCrowding = -1;
					}
					if (buildingCrowding == crowding) {
						leastCrowded.add(student);
					}
				}
			}
		} else {
			leastCrowded = students;
		}

		if (teacher instanceof Person person) {
			// Get the teacher's favorite students.
			Collection<Person> favoriteStudents = new ConcurrentLinkedQueue<>();
	
			// Find favorite opinion.
			double favorite = Double.NEGATIVE_INFINITY;
			Iterator<Person> k = leastCrowded.iterator();
			while (k.hasNext()) {
				Person student = k.next();
				double opinion = RelationshipUtil.getOpinionOfPerson(person, student);
				if (opinion > favorite) {
					favorite = opinion;
				}
			}
	
			// Get list of favorite students.
			k = leastCrowded.iterator();
			while (k.hasNext()) {
				Person student = k.next();
				double opinion = RelationshipUtil.getOpinionOfPerson(person, student);
				if (opinion == favorite) {
					favoriteStudents.add(student);
				}
			}
	
			result = favoriteStudents;
		}
		
		return result;
	}

	/**
	 * Get a collection of students the teacher can teach.
	 * 
	 * @param teacher the teacher looking for students.
	 * @return collection of students
	 */
	private static Collection<Person> getTeachableStudents(Worker teacher) {
		Collection<Person> result = new ConcurrentLinkedQueue<>();

		Iterator<Person> i = getLocalPeople(teacher).iterator();
		while (i.hasNext()) {
			Person student = i.next();
			boolean possibleStudent = false;
			Task task = student.getMind().getTaskManager().getTask();
			if (task != null && task.getAssociatedSkills() != null) {
				Iterator<SkillType> j = task.getAssociatedSkills().iterator();
				while (j.hasNext()) {
					SkillType taskSkill = j.next();
					int studentSkill = student.getSkillManager().getSkillLevel(taskSkill);
					int teacherSkill = teacher.getSkillManager().getSkillLevel(taskSkill);
					if ((teacherSkill >= (studentSkill + 1)) && !task.hasTeacher()) {
						possibleStudent = true;
					}
				}
				if (possibleStudent) {
					result.add(student);
				}
			}
		}

		return result;
	}
	
	/**
	 * Gets a collection of people in a worker's settlement or rover. The resulting
	 * collection doesn't include the given worker.
	 * 
	 * @param worker the worker checking
	 * @return collection of people
	 */
	private static Collection<Person> getLocalPeople(Worker worker) {
		Collection<Person> people = new ConcurrentLinkedQueue<>();

		if (worker.isInSettlement()) {
			Iterator<Person> i = worker.getSettlement().getIndoorPeople().iterator();
			while (i.hasNext()) {
				Person inhabitant = i.next();
				if (worker.equals(inhabitant)) {
					people.add(inhabitant);
				}
			}
		} else if (worker.isInVehicle()) {
			Crewable rover = (Crewable) worker.getVehicle();
			Iterator<Person> i = rover.getCrew().iterator();
			while (i.hasNext()) {
				Person crewmember = i.next();
				if (worker.equals(crewmember)) {
					people.add(crewmember);
				}
			}
		}

		return people;
	}

}
