/**
 * Mars Simulation Project
 * Teach.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.*;
import org.mars_sim.msp.core.person.*;
import org.mars_sim.msp.core.person.ai.social.Relationship;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.structure.building.*;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.vehicle.Crewable;

/**
 * This is a task for teaching a student a task.
 */
public class Teach extends Task implements Serializable {
    
    private static String CLASS_NAME = "org.mars_sim.msp.simulation.person.ai.task.Teach";
	
    private static Logger logger = Logger.getLogger(CLASS_NAME);
	
	// Task phase
	private static final String TEACHING = "Teaching";

	//	Static members
	private static final double STRESS_MODIFIER = -.1D; // The stress modified per millisol.
    
    // The improvement in relationship opinion of the teacher from the student per millisol.
    private static final double BASE_RELATIONSHIP_MODIFIER = .2D;

	private Person student;
	private Task teachingTask;

	/**
	 * Constructor
	 * @param person the person performing the task.
	 * @throws Exception if error constructing task.
	 */
	public Teach(Person person) throws Exception {
		super("Teaching", person, false, false, STRESS_MODIFIER, false, 0D);
		
		// Randomly get a student.
		Collection students = getBestStudents(person);
		if (students.size() > 0) {
		    	Object[] array = students.toArray();
			int rand = RandomUtil.getRandomInt(students.size() - 1);
			student = (Person) array[rand];
			teachingTask = student.getMind().getTaskManager().getTask();
			teachingTask.setTeacher(person);
			setDescription("Teaching " + teachingTask.getName() + " to " + student.getName());
			
			// If in settlement, move teacher to building student is in.
			if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
				try {
					Building teacherBuilding = BuildingManager.getBuilding(person);
					Building studentBuilding = BuildingManager.getBuilding(student);
					if (teacherBuilding != studentBuilding) 
						BuildingManager.addPersonToBuilding(person, studentBuilding);
				}
				catch (BuildingException e) {
					logger.log(Level.SEVERE,"Teach.constructor(): " + e.getMessage());
					endTask();
				}
			}
		}
		else endTask();
		
		// Initialize phase
		addPhase(TEACHING);
		setPhase(TEACHING);
		
		// logger.info(person.getName() + " " + description + " - Experience modifier: " + 
		// teachingTask.getTeachingExperienceModifier());
	}

	/** 
	 * Gets the weighted probability that a person might perform this task.
	 * It should return a 0 if there is no chance to perform this task given the person and his/her situation.
	 * @param person the person to perform the task
	 * @return the weighted probability that a person might perform this task
	 */
	public static double getProbability(Person person) {
		double result = 0D;
		
		// Find potential students.
		Collection potentialStudents = getBestStudents(person);
		if (potentialStudents.size() > 0) {
			result = 50D; 
		
			// If teacher is in a settlement, use crowding modifier.
			if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
				Person student = (Person) potentialStudents.toArray()[0];
				try {
					Building building = BuildingManager.getBuilding(student);
					if (building != null) {
						result *= Task.getCrowdingProbabilityModifier(person, building);
						result *= Task.getRelationshipModifier(person, building);
					}
					else result = 0D;
				}
				catch (BuildingException e) {
					logger.log(Level.SEVERE,"Teach.getProbability(): " + e.getMessage());
				}
			}
		}
		
		return result;
	}
	
    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time (millisol) the phase is to be performed.
     * @return the remaining time (millisol) after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) throws Exception {
    	if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
    	if (TEACHING.equals(getPhase())) return teachingPhase(time);
    	else return time;
    }
    
    /**
     * Performs the teaching phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     * @throws Exception if error performing the phase.
     */
    private double teachingPhase(double time) throws Exception {
    	
		// Check if task is finished.
		if (teachingTask.isDone()) endTask();
		
		// Check if student is in a different location situation than the teacher.
		if (!student.getLocationSituation().equals(person.getLocationSituation())) endTask();
		
        // Add relationship modifier for opinion of teacher from the student.
        addRelationshipModifier(time);
        
		return 0D;
    }
    
    /**
     * Adds a relationship modifier for the student's opinion of the teacher.
     * @param time the time teaching.
     */
    private void addRelationshipModifier(double time) {
        RelationshipManager manager = Simulation.instance().getRelationshipManager();
        double currentOpinion = manager.getOpinionOfPerson(student, person);
        double newOpinion = currentOpinion + (BASE_RELATIONSHIP_MODIFIER * time);
        Relationship relationship = manager.getRelationship(student, person);
        if (relationship != null) relationship.setPersonOpinion(student, newOpinion);
    }
	
	/**
	 * Adds experience to the person's skills used in this task.
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
		// This task adds no experience.
	}
	
	/**
	 * Ends the task and performs any final actions.
	 */
	 public void endTask() {
	 	super.endTask();
	 	
	 	teachingTask.setTeacher(null);
	 }
	
	/**
	 * Gets a collection of the best students the teacher can teach.
	 * @param teacher the teacher looking for students.
	 * @return collection of the best students
	 */
	private static Collection<Person> getBestStudents(Person teacher) {
		Collection<Person> result = new ConcurrentLinkedQueue<Person>();
		Collection<Person> students = getTeachableStudents(teacher);
		
		// If teacher is in a settlement, best students are in least crowded buildings.
		Collection<Person> leastCrowded = new ConcurrentLinkedQueue<Person>();
		if (teacher.getLocationSituation().equals(Person.INSETTLEMENT)) {
			try {
				// Find the least crowded buildings that teachable students are in.
				int crowding = Integer.MAX_VALUE;
				Iterator<Person> i = students.iterator();
				while (i.hasNext()) {
					Person student = i.next();
					Building building = BuildingManager.getBuilding(student);
					if (building != null) {
						LifeSupport lifeSupport = (LifeSupport) building.getFunction(LifeSupport.NAME);
						int buildingCrowding = lifeSupport.getOccupantNumber() - lifeSupport.getOccupantCapacity() + 1;
						if (buildingCrowding < -1) buildingCrowding = -1;
						if (buildingCrowding < crowding) crowding = buildingCrowding;
					}
				}
				
				// Add students in least crowded buildings to result.
				Iterator<Person> j = students.iterator();
				while (j.hasNext()) {
					Person student = j.next();
					Building building = BuildingManager.getBuilding(student);
					if (building != null) {
						LifeSupport lifeSupport = (LifeSupport) building.getFunction(LifeSupport.NAME);
						int buildingCrowding = lifeSupport.getOccupantNumber() - lifeSupport.getOccupantCapacity() + 1;
						if (buildingCrowding < -1) buildingCrowding = -1;
						if (buildingCrowding == crowding) leastCrowded.add(student);
					}
				}
			}
			catch (BuildingException e) {}
		}
		else leastCrowded = students;
		
		// Get the teacher's favorite students.
		RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
		Collection<Person> favoriteStudents = new ConcurrentLinkedQueue<Person>();
		
		// Find favorite opinion.
		double favorite = Double.NEGATIVE_INFINITY;
		Iterator<Person> k = leastCrowded.iterator();
		while (k.hasNext()) {
			Person student = k.next();
			double opinion = relationshipManager.getOpinionOfPerson(teacher, student);
			if (opinion > favorite) favorite = opinion;
		}
		
		// Get list of favorite students.
		k = leastCrowded.iterator();
		while (k.hasNext()) {
			Person student = k.next();
			double opinion = relationshipManager.getOpinionOfPerson(teacher, student);
			if (opinion == favorite) favoriteStudents.add(student);
		}
		
		result = favoriteStudents;
		
		return result;
	}
	
	/**
	 * Get a collection of students the teacher can teach.
	 * @param teacher the teacher looking for students.
	 * @return collection of students
	 */
	private static Collection<Person> getTeachableStudents(Person teacher) {
		Collection<Person> result = new ConcurrentLinkedQueue<Person>();
		
		Iterator<Person> i = getLocalPeople(teacher).iterator();
		while (i.hasNext()) {
			Person student = i.next();
			boolean possibleStudent = false;
			Task task = student.getMind().getTaskManager().getTask();
			if (task != null) {
				Iterator j = task.getAssociatedSkills().iterator();
				while (j.hasNext()) {
					String taskSkillName = (String) j.next();
					int studentSkill = student.getMind().getSkillManager().getSkillLevel(taskSkillName);
					int teacherSkill = teacher.getMind().getSkillManager().getSkillLevel(taskSkillName);
					if ((teacherSkill >= (studentSkill + 1)) && !task.hasTeacher()) possibleStudent = true; 
				}
				if (possibleStudent) result.add(student);
			}
		}
		
		return result;
	}
	
	/**
	 * Gets a collection of people in a person's settlement or rover.
	 * The resulting collection doesn't include the given person.
	 * @param person the person checking
	 * @return collection of people
	 */
	private static Collection<Person> getLocalPeople(Person person) {
		Collection<Person> people = new ConcurrentLinkedQueue<Person>();
		
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			Iterator<Person> i = person.getSettlement().getInhabitants().iterator();
			while (i.hasNext()) {
				Person inhabitant = i.next();
				if (person != inhabitant) people.add(inhabitant);
			}
		}
		else if (person.getLocationSituation().equals(Person.INVEHICLE)) {
			Crewable rover = (Crewable) person.getVehicle();
			Iterator<Person> i = rover.getCrew().iterator();
			while (i.hasNext()) {
				Person crewmember = i.next();
				if (person != crewmember) people.add(crewmember);
			}
		}
		
		return people;
	}

	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		return 0;
	}   
	
	/**
	 * Gets a list of the skills associated with this task.
	 * May be empty list if no associated skills.
	 * @return list of skills as strings
	 */
	public List<String> getAssociatedSkills() {
		List<String> results = new ArrayList<String>(0);
		return results;
	}
}