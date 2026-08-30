/*
 * Mars Simulation Project
 * Worker.java
 * @date 2025-08-10
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.task.util;

import com.mars_sim.core.UnitIdentifer;
import com.mars_sim.core.MonitorableEntity;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.building.function.ActivitySpot;
import com.mars_sim.core.building.function.ActivitySpot.AllocatedSpot;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.SkillOwner;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.unit.MobileUnit;

/**
 * Represents a Worker that can execute Tasks & Missions.
 */
public interface Worker extends UnitIdentifer, MonitorableEntity, SkillOwner, MobileUnit {

	/**
	 * Returns a reference to the Worker natural attribute manager
	 *
	 * @return the person's natural attribute manager
	 */
	NaturalAttributeManager getNaturalAttributeManager();

	/**
	 * Gets the workers name.
	 * 
	 * @return
	 */
	String getName();

	/**
	 * What the Worker is doing.
	 * 
	 * @return
	 */
	String getTaskDescription();

	/**
	 * Is the worker inside a vehicle in a garage ?
	 * 
	 * @return
	 */
	boolean isInVehicleInGarage();

	/**
	 * Is the worker outside of a settlement but within its vicinity ?
	 *
	 * @return true if the person is just right outside of a settlement
	 */
	boolean isRightOutsideSettlement();

	/**
	 * Gets the associated settlement.
	 *  
	 * @return the worker's associated settlement
	 */
	Settlement getAssociatedSettlement();

	/**
	 * What is the Mission this Worker is performing.
	 * 
	 * @return
	 */
	Mission getMission();

	/**
	 * Sets the person's current mission.
	 *
	 * @param newMission the new mission
	 */
	void setMission(Mission newMission);

	/**
	 * Gets the manager of the Worker's Tasks.
	 */
	TaskManager getTaskManager();

	/**
	 * Gets the unit type.
	 *
	 * @return
	 */
	UnitType getUnitType();


	/**
	 * Gets the unit type in string.
	 *
	 * @return
	 */
	String getStringType();
	
	/**
	 * Assigns an activity spot to a Worker.
	 * Note: This will release any activity spot previously assigned.
	 * 
	 * @param spot Owned spot
	 * @see ActivitySpot#claim(Worker)
	 * @see ActivitySpot#release(Worker)
	 */
	void setActivitySpot(AllocatedSpot spot);

	/**
	 * Get the activity spot allocated to a Worker
	 * @return
	 */
	AllocatedSpot getActivitySpot();
	
	/**
	 * Leaves an activity spot.
	 * 
	 * @apiNote This method is for leaving an existing activity spot in 
	 * order to go to a medical bed since medical beds are not characterized 
	 * as standard activity spots just yet. Therefore calling setActivitySpot()
	 * 
	 * @param release
	 */
	void leaveActivitySpot(boolean release);
	
	/**
	 * Fires a unit update event.
	 *
	 * @param updateType the update type.
	 * @param target     the event target object or null if none.
	 */
	void fireUnitUpdate(String updateType, Object target);

	/**
	 * Gets the equipment inventory for this Worker.
	 * 
	 * @return the equipment inventory
	 */
	EquipmentOwner getEquipmentInventory();
}
