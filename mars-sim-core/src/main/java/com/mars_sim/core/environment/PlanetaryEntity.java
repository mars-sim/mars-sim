/*
 * Mars Simulation Project
 * PlanetaryEntity.java
 * @date 2023-07-02
 * @author Barry Evans
 */

package com.mars_sim.core.environment;

import java.util.Set;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.data.UnitSet;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * Planetary entity that just hold Units
 */
public abstract class PlanetaryEntity extends Unit {

	/** default serial id. */
	private static final long serialVersionUID = 123L;

	private Set<Unit> unitList;

	private UnitType objectType;

	protected PlanetaryEntity(String name, int id, UnitType objectType) {
		super(name, id);

		unitList = new UnitSet<>();
		this.objectType = objectType;				
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	@Override
	public UnitType getUnitType() {
		return objectType;
	}

	/**
	 * This context is empty because it is a top level Entity.
	 */
	@Override
	public String getContext() {
		return null;
	}

	/**
	 * Adds a person.
	 *
	 * @param person
	 * @param true if the person can be added
	 */
	public boolean addPerson(Person person) {
		synchronized (unitList) {
			return unitList.add(person);
		}
	}

	/**
	 * Removes a person.
	 *
	 * @param person
	 * @param true if the person can be removed
	 */
	public boolean removePerson(Person person) {
		synchronized (unitList) {
			return unitList.remove(person);
		}
	}

	/**
	 * Adds a robot.
	 *
	 * @param robot
	 * @param true if the robot can be added
	 */
	public boolean addRobot(Robot robot) {
		synchronized (unitList) {
			return unitList.add(robot);
		}
	}

	/**
	 * Removes a robot.
	 *
	 * @param robot
	 * @param true if the robot can be removed
	 */
	public boolean removeRobot(Robot robot) {
		synchronized (unitList) {
			return unitList.remove(robot);
		}
	}

	/**
	 * Adds a vehicle.
	 *
	 * @param vehicle
	 * @param true if the vehicle can be added
	 */
	public boolean addVehicle(Vehicle vehicle) {
		synchronized (unitList) {
			// There is a bug somewhere because Drones in delivery remains on the Surface
			if (unitList.contains(vehicle)) {
				return true;
			}
			return unitList.add(vehicle);
		}
	}

	/**
	 * Removes a vehicle.
	 *
	 * @param vehicle
	 * @param true if the vehicle can be removed
	 */
	public boolean removeVehicle(Vehicle vehicle) {
		synchronized (unitList) {
			return unitList.remove(vehicle);
		}
	}

	/**
	 * Gets the hash code for this object.
	 *
	 * @return hash code.
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
