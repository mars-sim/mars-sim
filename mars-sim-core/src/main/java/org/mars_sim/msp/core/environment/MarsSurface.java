/**
 * Mars Simulation Project
 * MarsSurface.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.environment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * MarsSurface is the object unit that represents the surface of Mars
 */
public class MarsSurface extends Unit implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 123L;
	
	private static final String NAME = "Mars Surface";
	
	private List<Person> personList = new ArrayList<>();

	private List<Robot> robotList = new ArrayList<>();
	
	private List<Vehicle> vehicleList = new ArrayList<>();

	public MarsSurface() {
		super(NAME, null);
		
		setContainerUnit(null);
		
		setContainerID(Unit.OUTER_SPACE_UNIT_ID);

		// This is hack playing on how the identifiers are created
		if (getIdentifier() != Unit.MARS_SURFACE_UNIT_ID) {
			throw new IllegalStateException("MarsSurface has wrong ID: " + getIdentifier());
		}
	}
	
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		Unit u = (Unit) obj;
		return this.getName().equals(u.getName())
				&& this.getIdentifier() == ((Unit) obj).getIdentifier() ;
	}
	
	@Override
	public UnitType getUnitType() {
		return UnitType.PLANET;
	}
	
	@Override
	public Settlement getSettlement() {
		return null;
	}
	
	public List<Person> getPersonList() {
		return personList;
	}

	public List<Vehicle> getVehicleList() {
		return vehicleList;
	}

	/**
	 * Adds a person
	 * 
	 * @param person
	 * @param true if the person can be added
	 */
	public boolean addPerson(Person person) {
		if (!personList.contains(person) && personList.add(person)) {
			person.setContainerUnit(this);
			return true;
		}
		return false;
	}
	
	/**
	 * Removes a person
	 * 
	 * @param person
	 * @param true if the person can be removed
	 */
	public boolean removePerson(Person person) {
		if (personList.contains(person))
			return personList.remove(person);
		return false;
	}
	
	/**
	 * Adds a robot
	 * 
	 * @param robot
	 * @param true if the robot can be added
	 */
	public boolean addRobot(Robot robot) {
		if (!robotList.contains(robot) && robotList.add(robot)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Removes a robot
	 * 
	 * @param robot
	 * @param true if the robot can be removed
	 */
	public boolean removeRobot(Robot robot) {
		if (robotList.contains(robot))
			return robotList.remove(robot);
		return false;
	}
	
	/**
	 * Adds a vehicle
	 * 
	 * @param vehicle
	 * @param true if the vehicle can be added
	 */
	public boolean addVehicle(Vehicle vehicle) {
		if (!vehicleList.contains(vehicle) && vehicleList.add(vehicle)) {
			vehicle.setContainerUnit(this);
			return true;
		}
		return false;
	}
	
	/**
	 * Removes a vehicle
	 * 
	 * @param vehicle
	 * @param true if the vehicle can be removed
	 */
	public boolean removeVehicle(Vehicle vehicle) {
		if (vehicleList.contains(vehicle))
			return vehicleList.remove(vehicle);
		return false;
	}
	
	/**
	 * Gets the unit's container unit. Returns null if unit has no container unit.
	 * 
	 * @return the unit's container unit
	 */
	@Override
	public Unit getContainerUnit() {
		if (unitManager == null) // for maven test
			return null;
		// Note: there is no outer space unit
		return this;
	}
	
	/**
	 * Gets the hash code for this object.
	 * 
	 * @return hash code.
	 */
	public int hashCode() {
		int hashCode = (int) ( (1.0 + getName().hashCode()) * (1.0 + getIdentifier()));
		return hashCode;
	}
}
