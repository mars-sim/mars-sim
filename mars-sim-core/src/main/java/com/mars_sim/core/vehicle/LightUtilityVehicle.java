/*
 * Mars Simulation Project
 * LightUtilityVehicle.java
 * @date 2025-09-06
 * @author Sebastien Venot
 */
package com.mars_sim.core.vehicle;

import java.util.Collection;
import java.util.Set;

import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.data.UnitSet;
import com.mars_sim.core.location.LocationStateType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;

/**
 * A light utility vehicle that can be used for construction, loading and
 * mining.
 */
public class LightUtilityVehicle extends GroundVehicle implements Crewable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Vehicle name. */
	public static final String NAME = VehicleType.LUV.getName();

	/** The amount of work time to perform maintenance (millisols). */
	public static final double MAINTENANCE_WORK_TIME = 75D;
	
	// Data members.
	/** The LightUtilityVehicle's capacity for crewmembers. */
	private int crewCapacity = 0;
	private int robotCrewCapacity = 0;
	private int slotNumber = 0;
	
	/** A collections of attachment parts */
	private Collection<Part> attachments;
	/** The occupants. */
	private Set<Person> occupants;
	/** The robot occupants. */
	private Set<Robot> robotOccupants;
	
	public LightUtilityVehicle(String name, VehicleSpec spec, Settlement settlement) {
		// Use GroundVehicle constructor.
		super(name, spec, settlement, MAINTENANCE_WORK_TIME);
		
		if (spec.hasPartAttachments()) {
			attachments = spec.getAttachableParts();
			slotNumber = spec.getAttachmentSlots();
		}

		occupants = new UnitSet<>();
		robotOccupants = new UnitSet<>();
		
		// Set crew capacity
		crewCapacity = spec.getCrewSize();
		robotCrewCapacity = spec.getCrewSize();
	}

	/**
	 * Gets the number of crewmembers the vehicle can carry.
	 * 
	 * @return capacity
	 */
	public int getCrewCapacity() {
		return crewCapacity;
	}

	/**
	 * Gets the number of crewmembers the vehicle can carry.
	 * 
	 * @return capacity
	 */
	public int getRobotCrewCapacity() {
		return robotCrewCapacity;
	}

	/**
	 * Gets the current number of crewmembers.
	 * 
	 * @return number of crewmembers
	 */
	public int getCrewNum() {
		if (!getCrew().isEmpty())
			return occupants.size();
		return 0;
	}

	/**
	 * Gets the current number of crewmembers.
	 * 
	 * @return number of crewmembers
	 */
	public int getRobotCrewNum() {
		if (!getRobotCrew().isEmpty())
			return robotOccupants.size();
		return 0;
	}

	/**
	 * Gets a set of the robot crewmembers.
	 * 
	 * @return robot crewmembers as Collection
	 */
	public Set<Person> getCrew() {
		if (occupants == null || occupants.isEmpty())
			return new UnitSet<>();
		return occupants;
	}

	/**
	 * Gets a set of the robot crewmembers.
	 * 
	 * @return robot crewmembers as Collection
	 */
	public Set<Robot> getRobotCrew() {
		if (robotOccupants == null || robotOccupants.isEmpty())
			return new UnitSet<>();
		return robotOccupants;
	}

	/**
	 * Checks if person is a crewmember.
	 * 
	 * @param person the person to check
	 * @return true if person is a crewmember
	 */
	public boolean isCrewmember(Person person) {
		return occupants.contains(person);
	}

	/**
	 * Checks if robot is a crewmember.
	 * 
	 * @param robot the robot to check
	 * @return true if robot is a crewmember
	 */
	public boolean isRobotCrewmember(Robot robot) {
		return robotOccupants.contains(robot);
	}

	/**
	 * Adds a person as crewmember.
	 * 
	 * @param person
	 * @param true if the person can be added
	 */
	public boolean addPerson(Person person) {
		if (occupants.size() == 0 && robotOccupants.size() == 0) {
			person.setLocationStateType(LocationStateType.INSIDE_VEHICLE);
			// Fire the unit event type
			fireUnitUpdate(UnitEventType.INVENTORY_STORING_UNIT_EVENT, person);
			return true;
		}
		return false;
	}
	
	/**
	 * Removes a person as crewmember.
	 * 
	 * @param person
	 * @param true if the person can be removed
	 */
	public boolean removePerson(Person person) {
		if (isCrewmember(person)) {
			fireUnitUpdate(UnitEventType.INVENTORY_RETRIEVING_UNIT_EVENT, person);
			return occupants.remove(person);
		}
		return false;
	}
	
	/**
	 * Adds a robot as crewmember.
	 * 
	 * @param robot
	 * @param true if the robot can be added
	 */
	public boolean addRobot(Robot robot) {
		if (occupants.size() == 0 && robotOccupants.size() == 0) {
			fireUnitUpdate(UnitEventType.INVENTORY_STORING_UNIT_EVENT, robot);
			return robotOccupants.add(robot);
		}
		
		return false;
	}
	
	/**
	 * Removes a robot as crewmember.
	 * 
	 * @param robot
	 * @param true if the robot can be removed
	 */
	public boolean removeRobot(Robot robot) {
		if (isRobotCrewmember(robot)) {
			fireUnitUpdate(UnitEventType.INVENTORY_RETRIEVING_UNIT_EVENT, robot);
			return robotOccupants.remove(robot);
		}
		return false;
	}
	
	/**
	 * Gets a collection of parts that can be attached to this vehicle.
	 * 
	 * @return collection of parts.
	 */
	public Collection<Part> getPossibleAttachmentParts() {
		return attachments;
	}
	
	/**
	 * Gets the number of part slots in the vehicle.
	 * 
	 * @return number of part slots.
	 */
	public int getAtachmentSlotNumber() {
		return slotNumber;
	}

	@Override
	public void destroy() {
		super.destroy();

		attachments.clear();
		attachments = null;	

		occupants.clear();
		occupants = null;
		
		robotOccupants.clear();
		robotOccupants = null;
	}
	 
}

