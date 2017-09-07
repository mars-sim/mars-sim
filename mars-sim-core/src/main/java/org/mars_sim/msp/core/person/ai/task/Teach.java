/**
 * Mars Simulation Project
 * Teach.java
 * @version 3.07 2015-01-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.social.Relationship;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * This is a task for teaching a student a task.
 */
public class Teach
extends Task
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.teach"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase TEACHING = new TaskPhase(Msg.getString(
            "Task.phase.teaching")); //$NON-NLS-1$

    // Static members
    /** The stress modified per millisol. */
    private static final double STRESS_MODIFIER = -.1D;

    /** The improvement in relationship opinion of the teacher from the student per millisol. */
    private static final double BASE_RELATIONSHIP_MODIFIER = .2D;

    // Data members
    private Person student;
    private Task teachingTask;

    /**
     * Constructor.
     * @param person the person performing the task.
     */
    public Teach(Person person) {
        super(NAME, person, false, false, STRESS_MODIFIER, false, 0D);

        // Randomly get a student.
        Collection<Person> students = getBestStudents(person);
        if (students.size() > 0) {
            Object[] array = students.toArray();
            int rand = RandomUtil.getRandomInt(students.size() - 1);
            student = (Person) array[rand];
            teachingTask = student.getMind().getTaskManager().getTask();
            teachingTask.setTeacher(person);
            setDescription(Msg.getString("Task.description.teach.detail", 
                    teachingTask.getName(false), student.getName())); //$NON-NLS-1$

            boolean walkToBuilding = false;
            // If in settlement, move teacher to building student is in.
            if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

                Building studentBuilding = BuildingManager.getBuilding(student);

                if (studentBuilding != null) {
                    FunctionType teachingBuildingFunction = teachingTask.getRelatedBuildingFunction();
                    if ((teachingBuildingFunction != null) && (studentBuilding.hasFunction(teachingBuildingFunction))) {
                        // Walk to relevant activity spot in student's building.
                        walkToActivitySpotInBuilding(studentBuilding, teachingBuildingFunction, false);
                    }
                    else {
                        // Walk to random location in student's building.
                        walkToRandomLocInBuilding(BuildingManager.getBuilding(student), false);
                    }
                    walkToBuilding = true;
                }
            }

            if (!walkToBuilding) {

                if (person.getLocationSituation() == LocationSituation.IN_VEHICLE) {
                    // If person is in rover, walk to passenger activity spot.
                    if (person.getVehicle() instanceof Rover) {
                        walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), false);
                    }
                }
                else {
                    // Walk to random location.
                    walkToRandomLocation(true);
                }
            }
        } 
        else {
            endTask();
        }

        // Initialize phase
        addPhase(TEACHING);
        setPhase(TEACHING);
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (TEACHING.equals(getPhase())) {
            return teachingPhase(time);
        }
        else {
            return time;
        }
    }

    /**
     * Performs the teaching phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double teachingPhase(double time) {

        // Check if task is finished.
        if (teachingTask.isDone()) {
            endTask();
        }

        // Check if student is in a different location situation than the teacher.
        if (!student.getLocationSituation().equals(
                person.getLocationSituation())) {
            endTask();
        }

        // Add relationship modifier for opinion of teacher from the student.
        addRelationshipModifier(time);

        return 0D;
    }

    /**
     * Adds a relationship modifier for the student's opinion of the teacher.
     * @param time the time teaching.
     */
    private void addRelationshipModifier(double time) {
        RelationshipManager manager = Simulation.instance()
                .getRelationshipManager();
        double currentOpinion = manager.getOpinionOfPerson(student, person);
        double newOpinion = currentOpinion
                + (BASE_RELATIONSHIP_MODIFIER * time);
        Relationship relationship = manager.getRelationship(student, person);
        if (relationship != null) {
            relationship.setPersonOpinion(student, newOpinion);
        }
    }

    @Override
    protected void addExperience(double time) {
        // This task adds no experience.
    }

    @Override
    public void endTask() {
        super.endTask();

        //teachingTask.setTeacher(null);
    }

    /**
     * Gets a collection of the best students the teacher can teach.
     * @param teacher the teacher looking for students.
     * @return collection of the best students
     */
    public static Collection<Person> getBestStudents(Person teacher) {
        Collection<Person> result = new ConcurrentLinkedQueue<Person>();
        Collection<Person> students = getTeachableStudents(teacher);

        // If teacher is in a settlement, best students are in least crowded buildings.
        Collection<Person> leastCrowded = new ConcurrentLinkedQueue<Person>();
        if (teacher.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            // Find the least crowded buildings that teachable students are in.
            int crowding = Integer.MAX_VALUE;
            Iterator<Person> i = students.iterator();
            while (i.hasNext()) {
                Person student = i.next();
                Building building = BuildingManager.getBuilding(student);
                if (building != null) {
                    LifeSupport lifeSupport = (LifeSupport) building.getFunction(FunctionType.LIFE_SUPPORT);
                    int buildingCrowding = lifeSupport.getOccupantNumber()
                            - lifeSupport.getOccupantCapacity() + 1;
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
                    LifeSupport lifeSupport = (LifeSupport) building.getFunction(FunctionType.LIFE_SUPPORT);
                    int buildingCrowding = lifeSupport.getOccupantNumber()
                            - lifeSupport.getOccupantCapacity() + 1;
                    if (buildingCrowding < -1) {
                        buildingCrowding = -1;
                    }
                    if (buildingCrowding == crowding) {
                        leastCrowded.add(student);
                    }
                }
            }
        } 
        else {
            leastCrowded = students;
        }

        // Get the teacher's favorite students.
        RelationshipManager relationshipManager = Simulation.instance()
                .getRelationshipManager();
        Collection<Person> favoriteStudents = new ConcurrentLinkedQueue<Person>();

        // Find favorite opinion.
        double favorite = Double.NEGATIVE_INFINITY;
        Iterator<Person> k = leastCrowded.iterator();
        while (k.hasNext()) {
            Person student = k.next();
            double opinion = relationshipManager.getOpinionOfPerson(teacher,
                    student);
            if (opinion > favorite) {
                favorite = opinion;
            }
        }

        // Get list of favorite students.
        k = leastCrowded.iterator();
        while (k.hasNext()) {
            Person student = k.next();
            double opinion = relationshipManager.getOpinionOfPerson(teacher,
                    student);
            if (opinion == favorite) {
                favoriteStudents.add(student);
            }
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
                Iterator<SkillType> j = task.getAssociatedSkills().iterator();
                while (j.hasNext()) {
                    SkillType taskSkill = j.next();
                    int studentSkill = student.getMind().getSkillManager().getSkillLevel(taskSkill);
                    int teacherSkill = teacher.getMind().getSkillManager().getSkillLevel(taskSkill);
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
     * Gets a collection of people in a person's settlement or rover. The resulting collection 
     * doesn't include the given person.
     * @param person the person checking
     * @return collection of people
     */
    private static Collection<Person> getLocalPeople(Person person) {
        Collection<Person> people = new ConcurrentLinkedQueue<Person>();

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            Iterator<Person> i = person.getSettlement().getInhabitants()
                    .iterator();
            while (i.hasNext()) {
                Person inhabitant = i.next();
                if (person != inhabitant) {
                    people.add(inhabitant);
                }
            }
        } 
        else if (person.getLocationSituation() == LocationSituation.IN_VEHICLE) {
            Crewable rover = (Crewable) person.getVehicle();
            Iterator<Person> i = rover.getCrew().iterator();
            while (i.hasNext()) {
                Person crewmember = i.next();
                if (person != crewmember) {
                    people.add(crewmember);
                }
            }
        }

        return people;
    }

    @Override
    public int getEffectiveSkillLevel() {
        return 0;
    }

    @Override
    public List<SkillType> getAssociatedSkills() {
        List<SkillType> results = new ArrayList<SkillType>(0);
        return results;
    }

    @Override
    public void destroy() {
        super.destroy();

        student = null;
        teachingTask = null;
    }
}