/*
 * Mars Simulation Project
 * LightUtilityVehicle.java
 * @date 2021-10-16
 * @author Sebastien Venot
 */
package org.mars_sim.msp.core.vehicle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.ClockPulse;

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
	public static final double MAINTENANCE_WORK_TIME = 100D;
	
	// Data members.
	/** The LightUtilityVehicle's capacity for crewmembers. */
	private int crewCapacity = 0;
	private int robotCrewCapacity = 0;
	private int slotNumber = 0;
	
	/** A collections of attachment parts */
	private Collection<Part> attachments = null;
	/** The occupants. */
	private List<Person> occupants = new ArrayList<>();
	/** The robot occupants. */
	private List<Robot> robotOccupants = new ArrayList<>();
	
	public LightUtilityVehicle(String name, String type, Settlement settlement) {
		// Use GroundVehicle constructor.
		super(name, type, settlement, MAINTENANCE_WORK_TIME);
		
		VehicleConfig vehicleConfig = simulationConfig.getVehicleConfiguration();
		VehicleSpec spec = vehicleConfig.getVehicleSpec(type);
		if (spec.hasPartAttachments()) {
			attachments = spec.getAttachableParts();
			slotNumber = spec.getPartAttachmentSlotNumber();
		}

		crewCapacity = spec.getCrewSize();
		robotCrewCapacity = spec.getCrewSize();

		// Set rover terrain modifier
		setTerrainHandlingCapability(spec.getTerrainHandling());
	}

	@Override
	public int getFuelType() {
		return ResourceUtil.methaneID;
	}

	/**
	 * Gets the number of crewmembers the vehicle can carry.
	 * 
	 * @return capacity
	 */
	@Override
	public int getCrewCapacity() {
		return crewCapacity;
	}

	/**
	 * Gets the number of crewmembers the vehicle can carry.
	 * 
	 * @return capacity
	 */
	@Override
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
	 * Gets a list of the robot crewmembers.
	 * 
	 * @return robot crewmembers as Collection
	 */
	public List<Person> getCrew() {
		if (occupants == null || occupants.isEmpty())
			return new ArrayList<>();
		return occupants;
	}

	/**
	 * Gets a list of the robot crewmembers.
	 * 
	 * @return robot crewmembers as Collection
	 */
	public List<Robot> getRobotCrew() {
		if (robotOccupants == null || robotOccupants.isEmpty())
			return new ArrayList<>();
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
	 * Adds a person as crewmember
	 * 
	 * @param person
	 * @param true if the person can be added
	 */
	public boolean addPerson(Person person) {
		if (!isCrewmember(person) && occupants.add(person)) {
//			person.setContainerUnit(this);
			return true;
		}
		return false;
	}
	
	/**
	 * Removes a person as crewmember
	 * 
	 * @param person
	 * @param true if the person can be removed
	 */
	public boolean removePerson(Person person) {
		if (isCrewmember(person))
			return occupants.remove(person);
		return false;
	}
	
	/**
	 * Adds a robot as crewmember
	 * 
	 * @param robot
	 * @param true if the robot can be added
	 */
	public boolean addRobot(Robot robot) {
		if (!isRobotCrewmember(robot))
			return robotOccupants.add(robot);
		
		return false;
	}
	
	/**
	 * Removes a robot as crewmember
	 * 
	 * @param robot
	 * @param true if the robot can be removed
	 */
	public boolean removeRobot(Robot robot) {
		if (isRobotCrewmember(robot))
			return robotOccupants.remove(robot);
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
	public boolean timePassing(ClockPulse pulse) {
        return super.timePassing(pulse);
		// Add active time if crewed.
//		if (getCrewNum() > 0 || getRobotCrewNum() > 0)
//		if (getSpeed() > 0) {	
//			malfunctionManager.activeTimePassing(pulse.getElapsed());
//		}
    }

	@Override
	public String getNickName() {
		return getName();
	}


	public Vehicle getVehicle() {
		if (getContainerUnit() instanceof Vehicle)
			return (Vehicle) getContainerUnit();
		return null;
	}
	 
	
	@Override
	public void destroy() {
		super.destroy();

		attachments.clear();
		attachments = null;
	}
	 
}

