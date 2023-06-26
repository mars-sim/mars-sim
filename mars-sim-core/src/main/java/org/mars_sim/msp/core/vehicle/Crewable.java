/*
 * Mars Simulation Project
 * Crewable.java
 * @date 2021-10-16
 * @author Scott Davis
 */
package org.mars_sim.msp.core.vehicle;

import java.util.Collection;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;

/**
 * The Crewable interface represents a vehicle that is capable
 * of having a crew of people and robots.
 */
public interface Crewable {

	/**
	 * Gets the number of crewmembers the vehicle can carry.
	 * 
	 * @return capacity
	 */
	public int getCrewCapacity();

	/**
	 * Gets the number of robot crewmembers the vehicle can carry.
	 * 
	 * @return capacity
	 */
	public int getRobotCrewCapacity();

	/**
	 * Gets the current number of crewmembers.
	 * 
	 * @return number of crewmembers
	 */
	public int getCrewNum();

	/**
	 * Gets the current number of robot crewmembers.
	 * 
	 * @return number of robot crewmembers
	 */
	public int getRobotCrewNum();
	
	/**
	 * Gets a collection of the crewmembers.
	 * 
	 * @return crewmembers as Collection
	 */
	public Collection<Person> getCrew();

	/**
	 * Gets a collection of the robots.
	 * 
	 * @return robots as Collection
	 */
	public Collection<Robot> getRobotCrew();

	/**
	 * Checks if person is a crewmember.
	 * 
	 * @param person the person to check
	 * @return true if person is a crewmember
	 */
	public boolean isCrewmember(Person person);
	
	/**
	 * Checks if this robot is a crewmember.
	 * 
	 * @param robot the robot to check
	 * @return true if robot is a crewmember
	 */
	public boolean isRobotCrewmember(Robot robot);
	
	/**
	 * Removes a person as crewmember.
	 * 
	 * @param person
	 * @param true if the person can be removed
	 */
	public boolean removePerson(Person person);
	
	/**
	 * Adds a person as crewmember.
	 * 
	 * @param person
	 * @param true if the person can be added
	 */
	public boolean addPerson(Person person);
	
	/**
	 * Removes a robot as crewmember.
	 * 
	 * @param robot
	 * @param true if the robot can be removed
	 */
	public boolean removeRobot(Robot robot);
	
	/**
	 * Adds a robot as crewmember.
	 * 
	 * @param robot
	 * @param true if the robot can be added
	 */
	public boolean addRobot(Robot robot);
}
