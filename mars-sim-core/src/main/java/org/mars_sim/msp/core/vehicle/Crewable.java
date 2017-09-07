/**
 * Mars Simulation Project
 * Crewable.java
 * @version 3.1.0 2017-09-07
 * @author Scott Davis
 */
package org.mars_sim.msp.core.vehicle;

import java.util.Collection;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;

/**
 * The Crewable interface represents a vehicle that is capable
 * of having a crew of people and robots.
 */
public interface Crewable {

	/**
	 * Gets the number of crewmembers the vehicle can carry.
	 * @return capacity
	 */
	public int getCrewCapacity();

	public int getRobotCrewCapacity();

	/**
	 * Gets the current number of crewmembers.
	 * @return number of crewmembers
	 */
	public int getCrewNum();

	public int getRobotCrewNum();
	/**
	 * Gets a collection of the crewmembers.
	 * @return crewmembers as Collection
	 */
	public Collection<Person> getCrew();

	/**
	 * Gets a collection of the robots.
	 * @return robots as Collection
	 */
	public Collection<Robot> getRobotCrew();

	public Collection<Unit> getUnitCrew();

	
	/**
	 * Checks if person is a crewmember.
	 * @param person the person to check
	 * @return true if person is a crewmember
	 */
	public boolean isCrewmember(Person person);
	
	public boolean isRobotCrewmember(Robot robot);
}
