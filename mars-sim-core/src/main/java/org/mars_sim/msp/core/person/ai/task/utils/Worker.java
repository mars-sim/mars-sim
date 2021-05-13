package org.mars_sim.msp.core.person.ai.task.utils;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.logging.Loggable;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

public interface Worker extends Loggable {

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
	 * Get the workers name
	 * @return
	 */
	public String getName();

	/**
	 * What is the Worker doing
	 * @return
	 */
	public String getTaskDescription();

	/**
	 * Where the the Worker positioned on the Mars Surface?
	 * @return
	 */
	public Coordinates getCoordinates();

	/**
	 * How efficient is this Worker
	 * @return
	 */
	public double getPerformanceRating();

	/**
	 * What is the top level container of this worker; this will be a Unit that is on the MarsSurface,
	 * e.g. Vehicle or Settlement.
	 * @return
	 */
	public Unit getTopContainerUnit();


	/**
	 * Is the worker in a vehicle
	 * 
	 * @return true if the worker in a vehicle
	 */
	public boolean isInVehicle();
	
	/**
	 * Get vehicle worker is in, null if member is not in vehicle
	 * 
	 * @return the worker's vehicle
	 */
	public Vehicle getVehicle();

	/**
	 * Is the worker in a settlement
	 * 
	 * @return true if the worker in a settlement
	 */
	public boolean isInSettlement();

	/**
	 * Get the current Settlement of the worker; may be different from the assoicated Settlement.
	 * @return
	 */
	public Settlement getSettlement();
	
	/**
	 * Is the Worker outside
	 * 
	 * @return true if the worker is on the MarsSurface
	 */
	public boolean isOutside();
	
	/**
	 * Is the worker outside of a settlement but within its vicinity
	 * 
	 * @return true if the person is just right outside of a settlement
	 */
	public boolean isRightOutsideSettlement();

	/**
	 * Get the settlement in vicinity. This is used assume the person is not at a settlement
	 *
	 * @return the worker's settlement
	 */
	public Settlement getNearbySettlement();
	
	/**
	 * Adds a unit listener
	 * 
	 * @param newListener the listener to add.
	 */
	public void addUnitListener(UnitListener newListener);

	/**
	 * Removes a unit listener
	 * 
	 * @param oldListener the listener to remove.
	 */
	public void removeUnitListener(UnitListener oldListener);


	/**
	 * What is the Mission this Worker is performing.
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
	 * Get the Worker's Inventory
	 * TODO Should come from the InventoryOwner interface
	 * @return
	 */
	public Inventory getInventory();

	// These methods below should be moved to separate Walker interface
	// Also should be converted into a single LocalCoordinate class
	// instead of 2DPoint which is a UI AWT class
	
	/**
	 * Gets the worker X location at a settlement.
	 *
	 * @return X distance (meters) from the settlement's center.
	 */
	public double getXLocation();

	/**
	 * Sets the worker's X location at a settlement.
	 *
	 * @param xLocation the X distance (meters) from the settlement's center.
	 */
	public void setXLocation(double xLocation);

	/**
	 * Gets the worker's Y location at a settlement.
	 *
	 * @return Y distance (meters) from the settlement's center.
	 */
	public double getYLocation();

	/**
	 * Sets the worker's Y location at a settlement.
	 *
	 * @param yLocation
	 */
	public void setYLocation(double yLocation);
	
	/**
	 * Get the manager of the Worker's Tasks
	 */
	public TaskManager getTaskManager();
}
