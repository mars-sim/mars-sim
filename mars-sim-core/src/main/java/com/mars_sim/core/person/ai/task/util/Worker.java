/*
 * Mars Simulation Project
 * Worker.java
 * @date 2021-08-15
 * @author Barry Evans
 */

package com.mars_sim.core.person.ai.task.util;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitIdentifer;
import com.mars_sim.core.UnitListener;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.logging.Loggable;
import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.SkillManager;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.ActivitySpot;
import com.mars_sim.core.structure.building.function.ActivitySpot.AllocatedSpot;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.mapdata.location.Coordinates;
import com.mars_sim.mapdata.location.LocalPosition;

public interface Worker extends Loggable, UnitIdentifer, EquipmentOwner {

	/**
	 * Returns a reference to the Worker natural attribute manager
	 *
	 * @return the person's natural attribute manager
	 */
	public NaturalAttributeManager getNaturalAttributeManager();

	/**
	 * Returns a reference to the Person's skill manager
	 *
	 * @return the person's skill manager
	 */
	public SkillManager getSkillManager();

	/**
	 * Gets the workers name.
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * What the Worker is doing.
	 * 
	 * @return
	 */
	public String getTaskDescription();

	/**
	 * Where the the Worker positioned on the Mars Surface?
	 * 
	 * @return
	 */
	public Coordinates getCoordinates();

	/**
	 * How efficient is this Worker.
	 * 
	 * @return
	 */
	public double getPerformanceRating();

	/**
	 * What is the top level container of this worker; this will be a Unit that is on the MarsSurface,
	 * e.g. Vehicle or Settlement.
	 * 
	 * @return
	 */
	public Unit getTopContainerUnit();


	/**
	 * Is the worker in a vehicle ?
	 *
	 * @return true if the worker in a vehicle
	 */
	public boolean isInVehicle();

	/**
	 * Gets vehicle worker is in, null if member is not in vehicle/
	 *
	 * @return the worker's vehicle
	 */
	public Vehicle getVehicle();

	/**
	 * Is the worker inside a vehicle in a garage ?
	 * 
	 * @return
	 */
	public boolean isInVehicleInGarage();
	
	/**
	 * Is the worker in a settlement ?
	 *
	 * @return true if the worker in a settlement
	 */
	public boolean isInSettlement();

	/**
	 * Gets the current Settlement of the worker; may be different from the associated Settlement.
	 * 
	 * @return
	 */
	public Settlement getSettlement();
	
	/**
	 * Gets the Worker's building.
	 * 
	 * @return building
	 */
	public Building getBuildingLocation();
	
	/**
	 * Is the Worker outside ?
	 *
	 * @return true if the worker is on the MarsSurface
	 */
	public boolean isOutside();

	/**
	 * Is the worker outside of a settlement but within its vicinity ?
	 *
	 * @return true if the person is just right outside of a settlement
	 */
	public boolean isRightOutsideSettlement();

	/**
	 * Gets the settlement in vicinity. This is used assume the person is not at a settlement.
	 *
	 * @return the worker's settlement
	 */
	public Settlement getNearbySettlement();

	/**
	 * Gets the associated settlement.
	 *  
	 * @return the worker's associated settlement
	 */
	public Settlement getAssociatedSettlement();
	
	/**
	 * Adds a unit listener.
	 *
	 * @param newListener the listener to add.
	 */
	public void addUnitListener(UnitListener newListener);

	/**
	 * Removes a unit listener.
	 *
	 * @param oldListener the listener to remove.
	 */
	public void removeUnitListener(UnitListener oldListener);


	/**
	 * What is the Mission this Worker is performing.
	 * 
	 * @return
	 */
	public Mission getMission();

	/**
	 * Sets the person's current mission.
	 *
	 * @param newMission the new mission
	 */
	public void setMission(Mission newMission);

	/**
	 * Gets the Worker's position within the Settlement/Vehicle.
	 * 
	 * @return
	 */
	public LocalPosition getPosition();
	
	/**
	 * Sets the worker's position at a settlement.
	 *
	 * @param position
	 */
	public void setPosition(LocalPosition position);

	/**
	 * Gets the manager of the Worker's Tasks.
	 */
	public TaskManager getTaskManager();

	/**
	 * Gets the unit type.
	 *
	 * @return
	 */
	public UnitType getUnitType();
	
	/**
	 * An activity spot has been assigned to a Worker.
	 * This should release any activity spot already assigned
	 * @param spot Owned spot
	 * @see ActivitySpot#claim(Worker)
	 * @see ActivitySpot#release(Worker)
	 */
    public void setActivitySpot(AllocatedSpot spot);
}
