/*
 * Mars Simulation Project
 * Worker.java
 * @date 2025-08-10
 * @author Barry Evans
 */

package com.mars_sim.core.person.ai.task.util;

import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitIdentifer;
import com.mars_sim.core.UnitListener;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.building.function.ActivitySpot;
import com.mars_sim.core.building.function.ActivitySpot.AllocatedSpot;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.SkillOwner;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.unit.MobileUnit;

public interface Worker extends UnitIdentifer, EquipmentOwner, SkillOwner, MobileUnit {

	/**
	 * Returns a reference to the Worker natural attribute manager
	 *
	 * @return the person's natural attribute manager
	 */
	public NaturalAttributeManager getNaturalAttributeManager();

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
	 * Is the worker inside a vehicle in a garage ?
	 * 
	 * @return
	 */
	public boolean isInVehicleInGarage();

	/**
	 * Is the worker outside of a settlement but within its vicinity ?
	 *
	 * @return true if the person is just right outside of a settlement
	 */
	public boolean isRightOutsideSettlement();

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
	 * Gets the unit type in string.
	 *
	 * @return
	 */
	public String getStringType();
	
	/**
	 * Assigns an activity spot to a Worker.
	 * Note: This will release any activity spot previously assigned.
	 * 
	 * @param spot Owned spot
	 * @see ActivitySpot#claim(Worker)
	 * @see ActivitySpot#release(Worker)
	 */
    public void setActivitySpot(AllocatedSpot spot);

	/**
	 * Get the activity spot allocated to a Worker
	 * @return
	 */
	public AllocatedSpot getActivitySpot();

	/**
	 * Fires a unit update event.
	 *
	 * @param updateType the update type.
	 * @param target     the event target object or null if none.
	 */
	public void fireUnitUpdate(UnitEventType updateType, Object target);
}
